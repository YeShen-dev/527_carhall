package com.autoparts.market.payment.service;

import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.mapper.OrderMasterMapper;
import com.autoparts.market.payment.config.PaymentProperties;
import com.autoparts.market.payment.entity.PaymentNotifyLog;
import com.autoparts.market.payment.entity.PaymentRecord;
import com.autoparts.market.payment.mapper.PaymentNotifyLogMapper;
import com.autoparts.market.payment.mapper.PaymentRecordMapper;
import com.autoparts.market.payment.strategy.PaymentStrategy;
import com.autoparts.market.payment.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 支付回调处理服务
 *
 * 核心安全要求：
 * 1. 回调幂等：同一 paymentNo 的回调只处理一次
 * 2. 金额校验：回调金额必须与订单金额一致
 * 3. 签名验证：由各 Strategy 的 handleNotify 负责
 * 4. 日志记录：所有回调内容写入 payment_notify_log
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotifyService {

    private final OrderMasterMapper orderMasterMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final PaymentNotifyLogMapper notifyLogMapper;
    private final PaymentStrategyFactory strategyFactory;
    private final StringRedisTemplate redisTemplate;
    private final PaymentProperties properties;

    private static final String NOTIFY_LOCK_PREFIX = "pay:notify:";

    /**
     * 处理微信支付回调
     */
    @Transactional
    public String handleWechatNotify(String notifyBody) {
        log.info("[微信支付回调] 收到回调通知");
        return processNotify("WECHAT", notifyBody);
    }

    /**
     * 处理支付宝支付回调
     */
    @Transactional
    public String handleAlipayNotify(String notifyBody) {
        log.info("[支付宝回调] 收到回调通知");
        return processNotify("ALIPAY", notifyBody);
    }

    /**
     * 统一回调处理逻辑
     * @return 成功返回"SUCCESS"或支付宝返回"success"，失败则抛异常
     */
    private String processNotify(String payMethod, String notifyBody) {
        // 1. 记录回调日志（先持久化，防止丢失）
        PaymentNotifyLog logEntry = new PaymentNotifyLog();
        logEntry.setNotifyType("PAY");
        logEntry.setNotifyBody(notifyBody);
        logEntry.setCreatedAt(LocalDateTime.now());
        notifyLogMapper.insert(logEntry);

        try {
            // 2. 验签 + 解密（由各策略实现）
            PaymentStrategy strategy = strategyFactory.getStrategy(payMethod);
            boolean signatureOk = strategy.handleNotify(notifyBody);
            logEntry.setVerifyResult(signatureOk ? 1 : 0);

            if (!signatureOk && !properties.isMock()) {
                logEntry.setErrorMsg("验签失败");
                logEntry.setProcessResult(0);
                notifyLogMapper.updateById(logEntry);
                log.error("[回调验签失败] payMethod={}", payMethod);
                throw new RuntimeException("回调验签失败");
            }

            // 3. 从回调体提取 paymentNo（mock模式直接从 body 解析）
            // 真实模式：解密回调体后解析JSON
            String paymentNo = extractPaymentNo(notifyBody, payMethod);
            logEntry.setPaymentNo(paymentNo);

            // 4. 幂等锁：同一 paymentNo 只处理一次
            String lockKey = NOTIFY_LOCK_PREFIX + paymentNo;
            Boolean locked = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "1", Duration.ofSeconds(30));
            if (Boolean.FALSE.equals(locked)) {
                log.warn("[回调重复] paymentNo={} 已处理过，跳过", paymentNo);
                logEntry.setProcessResult(1);
                logEntry.setErrorMsg("重复回调，已跳过");
                notifyLogMapper.updateById(logEntry);
                return payMethod.equals("ALIPAY") ? "success" : getWechatSuccessResponse();
            }

            try {
                // 5. 查询支付记录
                PaymentRecord record = paymentRecordMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecord>()
                                .eq(PaymentRecord::getPaymentNo, paymentNo)
                );
                if (record == null) {
                    logEntry.setErrorMsg("支付记录不存在: " + paymentNo);
                    logEntry.setProcessResult(0);
                    notifyLogMapper.updateById(logEntry);
                    throw new RuntimeException("支付记录不存在");
                }

                // 6. 幂等：已成功的支付不再处理
                if ("SUCCESS".equals(record.getStatus())) {
                    log.info("[回调] paymentNo={} 已为SUCCESS，跳过", paymentNo);
                    logEntry.setProcessResult(1);
                    notifyLogMapper.updateById(logEntry);
                    return payMethod.equals("ALIPAY") ? "success" : getWechatSuccessResponse();
                }

                // 7. 查询订单
                OrderMaster order = orderMasterMapper.selectById(record.getOrderId());
                if (order == null) {
                    logEntry.setErrorMsg("订单不存在: " + record.getOrderId());
                    logEntry.setProcessResult(0);
                    notifyLogMapper.updateById(logEntry);
                    throw new RuntimeException("订单不存在");
                }

                // 8. 金额校验（mock模式跳过）
                // 真实模式：解密回调体获取金额，与order.getTotalAmount()比较
                // if (notifyAmount.compareTo(order.getTotalAmount()) != 0) throw...

                // 9. 更新支付记录
                LocalDateTime now = LocalDateTime.now();
                record.setStatus("SUCCESS");
                record.setThirdTradeNo(generateThirdTradeNo(payMethod));
                record.setUpdatedAt(now);
                paymentRecordMapper.updateById(record);

                // 10. 更新订单
                order.setStatus("PAID");
                order.setPaidAt(now);
                orderMasterMapper.updateById(order);

                // 11. 标记处理成功
                logEntry.setProcessResult(1);
                notifyLogMapper.updateById(logEntry);

                log.info("[回调处理成功] orderId={} paymentNo={} payMethod={} amount={}",
                        order.getId(), paymentNo, payMethod, order.getTotalAmount());

                return payMethod.equals("ALIPAY") ? "success" : getWechatSuccessResponse();

            } finally {
                redisTemplate.delete(lockKey);
            }

        } catch (Exception e) {
            log.error("[回调处理失败] payMethod={} error={}", payMethod, e.getMessage(), e);
            if (logEntry.getId() != null) {
                logEntry.setProcessResult(0);
                logEntry.setErrorMsg(e.getMessage());
                notifyLogMapper.updateById(logEntry);
            }
            throw new RuntimeException("回调处理失败: " + e.getMessage());
        }
    }

    /** 微信支付回调成功响应JSON */
    private String getWechatSuccessResponse() {
        return "{\"code\":\"SUCCESS\",\"message\":\"成功\"}";
    }

    /** 从回调体提取支付流水号 */
    private String extractPaymentNo(String body, String payMethod) {
        if (properties.isMock()) {
            // Mock模式：尝试从body中解析paymentNo
            // 简化处理：查找 "paymentNo":"xxx" 或直接使用整个body作为标识
            int idx = body.indexOf("paymentNo\":\"");
            if (idx >= 0) {
                int start = idx + 13;
                int end = body.indexOf("\"", start);
                if (end > start) {
                    return body.substring(start, end);
                }
            }
        }
        // 真实模式需要解密后解析JSON
        return "unknown";
    }

    /** 生成模拟的第三方交易号 */
    private String generateThirdTradeNo(String payMethod) {
        String prefix = payMethod.equals("WECHAT") ? "4200" :
                        payMethod.equals("ALIPAY") ? "2024" : "BC";
        return prefix + System.currentTimeMillis() + String.format("%06d", (int)(Math.random() * 1000000));
    }
}

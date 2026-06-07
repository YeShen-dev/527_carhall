package com.autoparts.market.payment.service;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.mapper.OrderMasterMapper;
import com.autoparts.market.payment.config.PaymentProperties;
import com.autoparts.market.payment.dto.PayRequest;
import com.autoparts.market.payment.dto.PayResponse;
import com.autoparts.market.payment.dto.PaymentStatusDTO;
import com.autoparts.market.payment.dto.RefundRequest;
import com.autoparts.market.payment.entity.PaymentNotifyLog;
import com.autoparts.market.payment.entity.PaymentRecord;
import com.autoparts.market.payment.entity.RefundRecord;
import com.autoparts.market.payment.mapper.PaymentNotifyLogMapper;
import com.autoparts.market.payment.mapper.PaymentRecordMapper;
import com.autoparts.market.payment.mapper.RefundRecordMapper;
import com.autoparts.market.payment.strategy.PaymentStrategy;
import com.autoparts.market.payment.strategy.PaymentStrategyFactory;
import com.autoparts.market.payment.util.PaymentIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 支付中心 — 支付系统核心入口
 *
 * 负责支付创建、状态查询、退款等业务编排，
 * 具体的渠道差异由 PaymentStrategy 实现处理。
 *
 * 幂等性保证：
 * 1. payment_no 数据库唯一索引
 * 2. Redis 分布式锁防同一订单并发提交
 * 3. 创建支付前检查是否已有进行中的支付
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCenterService {

    private final OrderMasterMapper orderMasterMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final RefundRecordMapper refundRecordMapper;
    private final PaymentNotifyLogMapper notifyLogMapper;
    private final PaymentStrategyFactory strategyFactory;
    private final StringRedisTemplate redisTemplate;
    private final PaymentProperties properties;

    private static final String LOCK_PREFIX = "pay:lock:";

    /**
     * 创建支付订单
     */
    @Transactional
    public PayResponse createPayment(Long userId, PayRequest request) {
        // 1. 分布式锁防并发
        String lockKey = LOCK_PREFIX + request.getOrderId();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
        if (Boolean.FALSE.equals(locked)) {
            throw new RuntimeException("请勿重复提交支付请求");
        }

        try {
            // 2. 查询订单
            OrderMaster order = orderMasterMapper.selectById(request.getOrderId());
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            if (!order.getUserId().equals(userId)) {
                throw new RuntimeException("无权操作此订单");
            }
            if (!"PENDING".equals(order.getStatus()) && !"VERIFYING".equals(order.getStatus())) {
                throw new RuntimeException("订单状态不允许支付，当前状态: " + order.getStatus());
            }

            // 3. 检查是否已有进行中的支付
            PaymentRecord existRecord = paymentRecordMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getOrderId, request.getOrderId())
                            .eq(PaymentRecord::getStatus, "PROCESSING")
                            .last("LIMIT 1")
            );
            if (existRecord != null) {
                throw new RuntimeException("该订单已有进行中的支付，请勿重复创建: " + existRecord.getPaymentNo());
            }

            // 4. 获取支付策略
            PaymentStrategy strategy = strategyFactory.getStrategy(request.getPayMethod());

            // 5. 创建支付记录
            PaymentRecord record = new PaymentRecord();
            record.setPaymentNo(PaymentIdGenerator.generatePaymentNo());
            record.setOrderId(order.getId());
            record.setUserId(userId);
            record.setPayMethod(request.getPayMethod());
            record.setAmount(order.getTotalAmount());
            record.setStatus("PROCESSING");
            record.setCreatedAt(LocalDateTime.now());
            paymentRecordMapper.insert(record);

            // 6. 更新订单支付信息
            order.setPayMethod(request.getPayMethod());
            order.setPaymentNo(record.getPaymentNo());
            order.setStatus("PROCESSING");
            orderMasterMapper.updateById(order);

            // 7. 调用渠道创建支付
            PayResponse response = strategy.createPayment(order, record);

            // 8. 更新二维码URL
            if (response.getQrCodeUrl() != null) {
                record.setQrCodeUrl(response.getQrCodeUrl());
                record.setUpdatedAt(LocalDateTime.now());
                paymentRecordMapper.updateById(record);
            }

            log.info("[支付创建] orderId={} paymentNo={} payMethod={} amount={}",
                    order.getId(), record.getPaymentNo(), record.getPayMethod(), order.getTotalAmount());

            return response;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("[支付创建失败] orderId={} error={}", request.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("创建支付失败: " + e.getMessage());
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 查询支付状态
     */
    public PaymentStatusDTO queryStatus(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }

        return PaymentStatusDTO.builder()
                .orderId(order.getId())
                .paymentNo(order.getPaymentNo())
                .status(order.getStatus())
                .payMethod(order.getPayMethod())
                .amount(order.getTotalAmount())
                .paidAt(order.getPaidAt())
                .build();
    }

    /**
     * 关闭支付（取消未支付的订单）
     */
    @Transactional
    public void closePayment(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if ("PAID".equals(order.getStatus()) || "REFUNDING".equals(order.getStatus())
                || "REFUNDED".equals(order.getStatus())) {
            throw new RuntimeException("订单已支付，无法取消");
        }

        order.setStatus("CANCELED");
        orderMasterMapper.updateById(order);

        // 关闭支付记录
        if (order.getPaymentNo() != null) {
            PaymentRecord record = paymentRecordMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getPaymentNo, order.getPaymentNo())
            );
            if (record != null && !"SUCCESS".equals(record.getStatus())) {
                record.setStatus("CLOSED");
                record.setUpdatedAt(LocalDateTime.now());
                paymentRecordMapper.updateById(record);
            }
        }

        log.info("[支付关闭] orderId={} paymentNo={}", orderId, order.getPaymentNo());
    }

    /**
     * 发起退款
     */
    @Transactional
    public String refund(Long userId, RefundRequest request) {
        OrderMaster order = orderMasterMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此订单");
        }
        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不允许退款，当前状态: " + order.getStatus());
        }

        String paymentNo = order.getPaymentNo();
        BigDecimal refundAmount = request.getRefundAmount() != null ? request.getRefundAmount() : order.getTotalAmount();

        if (refundAmount.compareTo(order.getTotalAmount()) > 0) {
            throw new RuntimeException("退款金额不能超过支付金额");
        }

        // 创建退款记录
        RefundRecord refundRecord = new RefundRecord();
        refundRecord.setRefundNo(PaymentIdGenerator.generateRefundNo());
        refundRecord.setPaymentNo(paymentNo);
        refundRecord.setOrderId(order.getId());
        refundRecord.setUserId(userId);
        refundRecord.setRefundAmount(refundAmount);
        refundRecord.setReason(request.getReason());
        refundRecord.setStatus("PROCESSING");
        refundRecord.setCreatedAt(LocalDateTime.now());
        refundRecordMapper.insert(refundRecord);

        // 更新订单状态
        order.setStatus("REFUNDING");
        orderMasterMapper.updateById(order);

        try {
            // 调用渠道退款
            PaymentRecord payRecord = paymentRecordMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PaymentRecord>()
                            .eq(PaymentRecord::getPaymentNo, paymentNo)
            );
            PaymentStrategy strategy = strategyFactory.getStrategy(payRecord.getPayMethod());
            String thirdRefundNo = strategy.refund(paymentNo, refundAmount, request.getReason());

            refundRecord.setThirdRefundNo(thirdRefundNo);
            refundRecord.setStatus("SUCCESS");
            refundRecord.setUpdatedAt(LocalDateTime.now());
            refundRecordMapper.updateById(refundRecord);

            // 判断全额/部分退款
            if (refundAmount.compareTo(order.getTotalAmount()) >= 0) {
                order.setStatus("REFUNDED");
            } else {
                order.setStatus("REFUNDED");  // 简化处理，可扩展 PARTIAL_REFUND
            }
            orderMasterMapper.updateById(order);

            log.info("[退款成功] orderId={} refundNo={} amount={}", order.getId(), refundRecord.getRefundNo(), refundAmount);
            return refundRecord.getRefundNo();

        } catch (Exception e) {
            log.error("[退款失败] orderId={} error={}", order.getId(), e.getMessage(), e);
            refundRecord.setStatus("FAILED");
            refundRecord.setUpdatedAt(LocalDateTime.now());
            refundRecordMapper.updateById(refundRecord);

            order.setStatus("PAID");
            orderMasterMapper.updateById(order);

            throw new RuntimeException("退款失败: " + e.getMessage());
        }
    }

    /**
     * 查询退款状态
     */
    public RefundRecord queryRefundStatus(String refundNo) {
        RefundRecord record = refundRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RefundRecord>()
                        .eq(RefundRecord::getRefundNo, refundNo)
        );
        if (record == null) {
            throw new RuntimeException("退款记录不存在");
        }
        return record;
    }
}

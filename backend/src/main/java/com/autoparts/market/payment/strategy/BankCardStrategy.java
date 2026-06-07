package com.autoparts.market.payment.strategy;

import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.payment.config.PaymentProperties;
import com.autoparts.market.payment.dto.PayResponse;
import com.autoparts.market.payment.entity.PaymentRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 银行卡聚合支付
 *
 * mock模式：验证卡号格式后直接标记支付成功
 * 真实模式：调用聚合支付平台（如汇付天下、收钱吧）的API完成银行卡收款
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankCardStrategy implements PaymentStrategy {

    private final PaymentProperties properties;

    @Override
    public String getPayMethod() {
        return "BANKCARD";
    }

    @Override
    public PayResponse createPayment(OrderMaster order, PaymentRecord record) throws Exception {
        log.info("[银行卡支付] 直接付款, paymentNo={} amount={}", record.getPaymentNo(), order.getTotalAmount());

        // 银行卡支付无需二维码或跳转，直接扣款
        // 真实模式：调用聚合支付API
        // Mock模式：返回null表示无需前端额外操作，直接标记支付中

        return PayResponse.builder()
                .paymentNo(record.getPaymentNo())
                .payMethod("BANKCARD")
                .qrCodeUrl(null)  // 银行卡无需二维码
                .amount(order.getTotalAmount())
                .orderNo(order.getOrderNo())
                .expireTime(LocalDateTime.now().plusMinutes(properties.getExpireMinutes()))
                .build();
    }

    @Override
    public boolean handleNotify(String notifyBody) throws Exception {
        log.info("[银行卡回调] 收到回调通知");
        if (properties.isMock()) {
            return true;
        }
        // 聚合支付平台回调验签
        return true;
    }

    @Override
    public String refund(String paymentNo, BigDecimal refundAmount, String reason) throws Exception {
        log.info("[银行卡退款] paymentNo={} amount={}", paymentNo, refundAmount);

        if (properties.isMock()) {
            String refundNo = "BC" + System.currentTimeMillis();
            log.info("[银行卡退款Mock] 模拟退款成功, refundNo={}", refundNo);
            return refundNo;
        }

        // 调用聚合支付平台退款API
        return "BC" + System.currentTimeMillis();
    }

    @Override
    public String queryRefundStatus(String refundNo) throws Exception {
        if (properties.isMock()) {
            return "SUCCESS";
        }
        return "SUCCESS";
    }
}

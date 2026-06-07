package com.autoparts.market.payment.strategy;

import com.autoparts.market.payment.dto.PayResponse;
import com.autoparts.market.payment.entity.PaymentRecord;
import com.autoparts.market.entity.OrderMaster;

/**
 * 支付策略接口 — 定义统一的支付行为
 * 每种支付方式（微信/支付宝/银行卡）实现此接口，通过工厂注入
 */
public interface PaymentStrategy {

    /** 策略标识，对应 PayRequest.payMethod */
    String getPayMethod();

    /**
     * 创建支付
     * @param order 订单
     * @param paymentRecord 支付记录（已生成 paymentNo）
     * @return 包含二维码/跳转URL的响应
     */
    PayResponse createPayment(OrderMaster order, PaymentRecord paymentRecord) throws Exception;

    /**
     * 处理支付回调通知
     * @param notifyBody 回调原始报文
     * @return 处理成功返回true
     */
    boolean handleNotify(String notifyBody) throws Exception;

    /**
     * 发起退款
     * @param paymentNo 原支付流水号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 第三方退款单号
     */
    String refund(String paymentNo, java.math.BigDecimal refundAmount, String reason) throws Exception;

    /**
     * 查询退款状态
     * @param refundNo 退款流水号
     * @return 状态字符串
     */
    String queryRefundStatus(String refundNo) throws Exception;
}

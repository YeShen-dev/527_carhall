package com.autoparts.market.payment.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付状态查询响应
 */
@Data
@Builder
public class PaymentStatusDTO {
    /** 订单ID */
    private Long orderId;
    /** 支付流水号 */
    private String paymentNo;
    /** 支付状态 */
    private String status;
    /** 支付方式 */
    private String payMethod;
    /** 支付金额 */
    private BigDecimal amount;
    /** 支付时间 */
    private LocalDateTime paidAt;
}

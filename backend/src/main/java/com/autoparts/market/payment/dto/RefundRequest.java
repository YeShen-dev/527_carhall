package com.autoparts.market.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 退款请求
 */
@Data
public class RefundRequest {
    /** 订单ID */
    private Long orderId;
    /** 退款原因 */
    private String reason;
    /** 退款金额（为空则全额退款） */
    private BigDecimal refundAmount;
}

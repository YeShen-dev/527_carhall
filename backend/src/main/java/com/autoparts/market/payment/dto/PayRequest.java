package com.autoparts.market.payment.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 支付请求
 */
@Data
public class PayRequest {
    /** 订单ID */
    private Long orderId;
    /** 支付方式：WECHAT / ALIPAY / BANKCARD */
    private String payMethod;
}

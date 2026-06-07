package com.autoparts.market.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayResponse {
    /** 支付流水号 */
    private String paymentNo;
    /** 支付方式 */
    private String payMethod;
    /** 二维码链接（微信Native）或跳转表单HTML（支付宝） */
    private String qrCodeUrl;
    /** 支付金额 */
    private BigDecimal amount;
    /** 订单号 */
    private String orderNo;
    /** 过期时间 */
    private LocalDateTime expireTime;
}

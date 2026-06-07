package com.autoparts.market.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录 — 每笔支付请求对应一条记录，payment_no 为幂等键
 */
@Data
@TableName("payment_record")
public class PaymentRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付流水号（幂等键，格式：PAY + yyyyMMddHHmmss + 6位随机） */
    private String paymentNo;

    /** 关联订单ID */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 支付方式：WECHAT / ALIPAY / BANKCARD */
    private String payMethod;

    /** 支付金额 */
    private BigDecimal amount;

    /** 状态：CREATED / PROCESSING / SUCCESS / FAILED / CLOSED */
    private String status;

    /** 第三方交易号（微信transaction_id / 支付宝trade_no） */
    private String thirdTradeNo;

    /** 二维码链接（微信code_url）或支付跳转URL */
    private String qrCodeUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

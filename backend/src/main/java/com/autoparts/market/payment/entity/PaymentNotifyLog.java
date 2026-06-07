package com.autoparts.market.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 支付回调日志 — 记录每次第三方回调的原始内容和处理结果，用于审计和排错
 */
@Data
@TableName("payment_notify_log")
public class PaymentNotifyLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 支付流水号 */
    private String paymentNo;

    /** 通知类型：PAY（支付通知）/ REFUND（退款通知） */
    private String notifyType;

    /** 回调原始报文（加密或明文） */
    private String notifyBody;

    /** 解密后的报文内容 */
    private String decryptBody;

    /** 签名验证结果：0-失败 1-成功 */
    private Integer verifyResult;

    /** 业务处理结果：0-失败 1-成功 */
    private Integer processResult;

    /** 处理失败时的错误信息 */
    private String errorMsg;

    private LocalDateTime createdAt;
}

package com.autoparts.market.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录 — 每笔退款对应一条记录
 */
@Data
@TableName("refund_record")
public class RefundRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 退款流水号（格式：REF + yyyyMMddHHmmss + 6位随机） */
    private String refundNo;

    /** 原支付流水号 */
    private String paymentNo;

    /** 关联订单ID */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 退款金额 */
    private BigDecimal refundAmount;

    /** 退款原因 */
    private String reason;

    /** 状态：PROCESSING / SUCCESS / FAILED */
    private String status;

    /** 第三方退款单号 */
    private String thirdRefundNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

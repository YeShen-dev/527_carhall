package com.autoparts.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "订单")
@Data
@TableName("order_master")
public class OrderMaster {

    @Schema(description = "订单ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态：PENDING/PROCESSING/PAID/CANCELED/REFUNDING/REFUNDED")
    private String status;

    @Schema(description = "支付方式：WECHAT/ALIPAY/BANKCARD")
    private String payMethod;

    @Schema(description = "支付流水号")
    private String paymentNo;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "订单商品明细")
    @TableField(exist = false)
    private List<OrderItem> items;
}

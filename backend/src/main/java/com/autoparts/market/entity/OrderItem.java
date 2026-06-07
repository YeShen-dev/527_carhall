package com.autoparts.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Schema(description = "订单商品项")
@Data
@TableName("order_item")
public class OrderItem {

    @Schema(description = "明细ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "商品名称快照")
    private String productName;

    @Schema(description = "商品图片快照")
    private String productImage;

    @Schema(description = "下单时单价")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计金额")
    private BigDecimal subtotal;
}

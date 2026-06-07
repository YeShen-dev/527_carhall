package com.autoparts.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Schema(description = "订单商品项")
@Data
public class OrderItemDTO {
    @Schema(description = "商品ID")
    private Long productId;
    @Schema(description = "商品名称")
    private String productName;
    @Schema(description = "商品图片URL")
    private String productImage;
    @Schema(description = "单价")
    private BigDecimal price;
    @Schema(description = "数量")
    private Integer quantity;
    @Schema(description = "小计金额")
    private BigDecimal subtotal;
}

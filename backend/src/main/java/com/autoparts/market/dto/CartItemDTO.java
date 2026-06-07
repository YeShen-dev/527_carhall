package com.autoparts.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Schema(description = "购物车商品项")
@Data
public class CartItemDTO {
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
    @Schema(description = "当前库存")
    private Integer stock;
}

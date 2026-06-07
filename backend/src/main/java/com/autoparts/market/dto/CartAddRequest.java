package com.autoparts.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "添加购物车请求")
@Data
public class CartAddRequest {
    @Schema(description = "商品ID")
    private Long productId;
    @Schema(description = "数量")
    private Integer quantity;
}

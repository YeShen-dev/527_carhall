package com.autoparts.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "更新购物车商品数量请求")
@Data
public class CartUpdateRequest {
    @Schema(description = "新数量")
    private Integer quantity;
}

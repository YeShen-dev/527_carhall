package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.CartAddRequest;
import com.autoparts.market.dto.CartItemDTO;
import com.autoparts.market.dto.CartUpdateRequest;
import com.autoparts.market.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "临时购物车", description = "未登录用户的临时购物车接口")
@RestController
@RequestMapping("/api/cart/temp")
@RequiredArgsConstructor
public class CartTempController {

    private final CartService cartService;

    @Operation(summary = "查看临时购物车")
    @GetMapping("/{sessionId}")
    public ApiResponse<List<CartItemDTO>> list(@Parameter(description = "会话ID") @PathVariable String sessionId) {
        return ApiResponse.success(cartService.listCartTemp(sessionId));
    }

    @Operation(summary = "添加商品到临时购物车")
    @PostMapping("/{sessionId}")
    public ApiResponse<Void> add(@Parameter(description = "会话ID") @PathVariable String sessionId,
                                  @RequestBody CartAddRequest body) {
        cartService.addItemTemp(sessionId, body);
        return ApiResponse.success("添加成功", null);
    }

    @Operation(summary = "更新临时购物车商品数量")
    @PutMapping("/{sessionId}/{productId}")
    public ApiResponse<Void> update(@Parameter(description = "会话ID") @PathVariable String sessionId,
                                     @Parameter(description = "商品ID") @PathVariable Long productId,
                                     @RequestBody CartUpdateRequest body) {
        cartService.updateItemTemp(sessionId, productId, body.getQuantity());
        return ApiResponse.success("更新成功", null);
    }

    @Operation(summary = "从临时购物车移除商品")
    @DeleteMapping("/{sessionId}/{productId}")
    public ApiResponse<Void> remove(@Parameter(description = "会话ID") @PathVariable String sessionId,
                                     @Parameter(description = "商品ID") @PathVariable Long productId) {
        cartService.removeItemTemp(sessionId, productId);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "清空临时购物车")
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> clear(@Parameter(description = "会话ID") @PathVariable String sessionId) {
        cartService.clearCartTemp(sessionId);
        return ApiResponse.success("已清空", null);
    }
}

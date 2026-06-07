package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.CartAddRequest;
import com.autoparts.market.dto.CartItemDTO;
import com.autoparts.market.dto.CartUpdateRequest;
import com.autoparts.market.service.CartService;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车", description = "用户购物车管理接口")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "查看购物车")
    @GetMapping
    public ApiResponse<List<CartItemDTO>> list() {
        return ApiResponse.success(cartService.listCart(SecurityUtils.getCurrentUserId().toString()));
    }

    @Operation(summary = "添加商品到购物车")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody CartAddRequest body) {
        cartService.addItem(SecurityUtils.getCurrentUserId().toString(), body);
        return ApiResponse.success("添加成功", null);
    }

    @Operation(summary = "更新购物车商品数量")
    @PutMapping("/{productId}")
    public ApiResponse<Void> update(@Parameter(description = "商品ID") @PathVariable Long productId, @RequestBody CartUpdateRequest body) {
        cartService.updateItem(SecurityUtils.getCurrentUserId().toString(), productId, body.getQuantity());
        return ApiResponse.success("更新成功", null);
    }

    @Operation(summary = "从购物车移除商品")
    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@Parameter(description = "商品ID") @PathVariable Long productId) {
        cartService.removeItem(SecurityUtils.getCurrentUserId().toString(), productId);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping
    public ApiResponse<Void> clear() {
        cartService.clearCart(SecurityUtils.getCurrentUserId().toString());
        return ApiResponse.success("已清空", null);
    }
}

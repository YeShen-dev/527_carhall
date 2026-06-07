package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.OrderDTO;
import com.autoparts.market.dto.PageResult;
import com.autoparts.market.service.OrderService;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理", description = "用户订单创建与查询接口")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "提交订单", description = "根据购物车内容生成订单，使用乐观锁扣减库存")
    @PostMapping
    public ApiResponse<OrderDTO> placeOrder() {
        return ApiResponse.success("下单成功", orderService.placeOrder(SecurityUtils.getCurrentUserId()));
    }

    @Operation(summary = "分页查询我的订单")
    @GetMapping
    public ApiResponse<PageResult<OrderDTO>> list(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.listOrders(SecurityUtils.getCurrentUserId(), page, size));
    }

    @Operation(summary = "查询订单详情")
    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> detail(@Parameter(description = "订单ID") @PathVariable Long id) {
        return ApiResponse.success(orderService.getOrderDetail(id));
    }

    @Operation(summary = "确认收货", description = "将订单状态从 PAID/SHIPPED 改为 COMPLETED")
    @PostMapping("/{id}/confirm")
    public ApiResponse<Void> confirmReceipt(@Parameter(description = "订单ID") @PathVariable Long id) {
        orderService.confirmReceipt(id, SecurityUtils.getCurrentUserId());
        return ApiResponse.success("确认收货成功", null);
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@Parameter(description = "订单ID") @PathVariable Long id) {
        orderService.cancelOrder(id, SecurityUtils.getCurrentUserId());
        return ApiResponse.success("已取消", null);
    }
}

package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.OrderDTO;
import com.autoparts.market.dto.PageResult;
import com.autoparts.market.entity.OrderItem;
import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.mapper.OrderItemMapper;
import com.autoparts.market.mapper.OrderMasterMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "管理后台-订单管理", description = "管理员订单查看与状态更新接口")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderMasterMapper orderMasterMapper;
    private final OrderItemMapper orderItemMapper;

    @Operation(summary = "分页查询所有订单", description = "支持按状态筛选")
    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            @Parameter(description = "订单状态：PENDING/SHIPPED/COMPLETED/CANCELLED") @RequestParam(required = false) String status,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<OrderMaster> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) {
            wrapper.eq(OrderMaster::getStatus, status);
        }
        wrapper.orderByDesc(OrderMaster::getCreatedAt);

        Page<OrderMaster> mpPage = orderMasterMapper.selectPage(new Page<>(page + 1, size), wrapper);
        List<Map<String, Object>> list = mpPage.getRecords().stream().map(order -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", order.getId());
            m.put("orderNo", order.getOrderNo());
            m.put("userId", order.getUserId());
            m.put("totalAmount", order.getTotalAmount());
            m.put("status", order.getStatus());
            m.put("createdAt", order.getCreatedAt());
            return m;
        }).collect(Collectors.toList());

        return ApiResponse.success(PageResult.from(mpPage, list));
    }

    @Operation(summary = "查看订单详情（含订单商品明细）")
    @GetMapping("/{id}")
    public ApiResponse<OrderDTO> detail(@Parameter(description = "订单ID") @PathVariable Long id) {
        OrderMaster order = orderMasterMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, id);
        order.setItems(orderItemMapper.selectList(wrapper));

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setItems(order.getItems().stream().map(item -> {
            com.autoparts.market.dto.OrderItemDTO itemDto = new com.autoparts.market.dto.OrderItemDTO();
            itemDto.setProductId(item.getProductId());
            itemDto.setProductName(item.getProductName());
            itemDto.setProductImage(item.getProductImage());
            itemDto.setPrice(item.getPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setSubtotal(item.getSubtotal());
            return itemDto;
        }).collect(Collectors.toList()));
        return ApiResponse.success(dto);
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    @Transactional
    public ApiResponse<Void> updateStatus(@Parameter(description = "订单ID") @PathVariable Long id, @RequestBody Map<String, String> body) {
        OrderMaster order = orderMasterMapper.selectById(id);
        if (order == null) throw new RuntimeException("订单不存在");
        order.setStatus(body.get("status"));
        orderMasterMapper.updateById(order);
        return ApiResponse.success("状态更新成功", null);
    }
}

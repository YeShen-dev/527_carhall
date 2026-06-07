package com.autoparts.market.service;

import com.autoparts.market.dto.OrderDTO;
import com.autoparts.market.dto.OrderItemDTO;
import com.autoparts.market.dto.PageResult;
import com.autoparts.market.entity.OrderItem;
import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.entity.Product;
import com.autoparts.market.mapper.OrderItemMapper;
import com.autoparts.market.mapper.OrderMasterMapper;
import com.autoparts.market.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMasterMapper orderMasterMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final CartService cartService;

    @Transactional
    public OrderDTO placeOrder(Long userId) {
        Map<Long, Integer> cartMap = cartService.getCartMap(userId.toString());
        if (cartMap.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        List<Product> products = productMapper.selectBatchIds(cartMap.keySet());

        for (Product product : products) {
            int requestedQty = cartMap.get(product.getId());
            if (product.getStock() < requestedQty) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足，当前库存：" + product.getStock());
            }
        }

        for (Product product : products) {
            int requestedQty = cartMap.get(product.getId());
            product.setStock(product.getStock() - requestedQty);
            if (productMapper.updateById(product) == 0) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存扣减失败，请重试");
            }
        }

        OrderMaster order = new OrderMaster();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus("PENDING");

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Product product : products) {
            int qty = cartMap.get(product.getId());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(qty));
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setPrice(product.getPrice());
            item.setQuantity(qty);
            item.setSubtotal(subtotal);
            orderItems.add(item);
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);
        order.setItems(orderItems);
        orderMasterMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        cartService.clearCart(userId.toString());
        return toOrderDTO(order);
    }

    public PageResult<OrderDTO> listOrders(Long userId, int page, int size) {
        LambdaQueryWrapper<OrderMaster> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderMaster::getUserId, userId)
               .orderByDesc(OrderMaster::getCreatedAt);
        Page<OrderMaster> mpPage = orderMasterMapper.selectPage(new Page<>(page + 1, size), wrapper);

        if (mpPage.getRecords().isEmpty()) {
            return PageResult.from(mpPage, Collections.emptyList());
        }

        List<Long> orderIds = mpPage.getRecords().stream()
                .map(OrderMaster::getId).collect(Collectors.toList());
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.in(OrderItem::getOrderId, orderIds);
        Map<Long, List<OrderItem>> itemsMap = orderItemMapper.selectList(itemWrapper).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<OrderDTO> dtoList = mpPage.getRecords().stream().map(order -> {
            order.setItems(itemsMap.getOrDefault(order.getId(), Collections.emptyList()));
            return toOrderDTO(order);
        }).collect(Collectors.toList());
        return PageResult.from(mpPage, dtoList);
    }

    public OrderDTO getOrderDetail(Long orderId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        order.setItems(orderItemMapper.selectList(wrapper));
        return toOrderDTO(order);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }

    public void confirmReceipt(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作该订单");
        if (!"PAID".equals(order.getStatus()) && !"SHIPPED".equals(order.getStatus()))
            throw new RuntimeException("订单状态不允许确认收货");
        order.setStatus("COMPLETED");
        orderMasterMapper.updateById(order);
    }

    public void cancelOrder(Long orderId, Long userId) {
        OrderMaster order = orderMasterMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在");
        if (!order.getUserId().equals(userId)) throw new RuntimeException("无权操作该订单");
        if (!"PENDING".equals(order.getStatus()) && !"PROCESSING".equals(order.getStatus()))
            throw new RuntimeException("订单状态不允许取消");
        order.setStatus("CANCELLED");
        orderMasterMapper.updateById(order);
    }

    private OrderDTO toOrderDTO(OrderMaster order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream().map(item -> {
                OrderItemDTO itemDto = new OrderItemDTO();
                itemDto.setProductId(item.getProductId());
                itemDto.setProductName(item.getProductName());
                itemDto.setProductImage(item.getProductImage());
                itemDto.setPrice(item.getPrice());
                itemDto.setQuantity(item.getQuantity());
                itemDto.setSubtotal(item.getSubtotal());
                return itemDto;
            }).collect(Collectors.toList()));
        }
        return dto;
    }
}

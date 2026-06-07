package com.autoparts.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "订单详情")
@Data
public class OrderDTO {
    @Schema(description = "订单ID")
    private Long id;
    @Schema(description = "订单编号")
    private String orderNo;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;
    @Schema(description = "订单状态")
    private String status;
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @Schema(description = "订单商品列表")
    private List<OrderItemDTO> items;
}

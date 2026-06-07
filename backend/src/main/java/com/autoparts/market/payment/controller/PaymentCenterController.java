package com.autoparts.market.payment.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.payment.dto.PayRequest;
import com.autoparts.market.payment.dto.PayResponse;
import com.autoparts.market.payment.dto.PaymentStatusDTO;
import com.autoparts.market.payment.dto.RefundRequest;
import com.autoparts.market.payment.entity.RefundRecord;
import com.autoparts.market.payment.service.PaymentCenterService;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 支付中心 API
 *
 * POST   /api/payment/create         创建支付（返回二维码/跳转URL）
 * GET    /api/payment/status/{id}     查询支付状态
 * POST   /api/payment/close/{id}      关闭未支付订单
 * POST   /api/payment/refund           发起退款
 * GET    /api/payment/refund/status/{refundNo}  查询退款状态
 */
@Slf4j
@Tag(name = "支付中心", description = "支付创建、状态查询、退款接口")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentCenterController {

    private final PaymentCenterService paymentCenterService;

    @Operation(summary = "创建支付", description = "根据订单ID和支付方式创建支付，返回二维码链接或跳转URL")
    @PostMapping("/create")
    public ApiResponse<PayResponse> createPayment(@RequestBody PayRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        PayResponse response = paymentCenterService.createPayment(userId, request);
        return ApiResponse.success("支付创建成功", response);
    }

    @Operation(summary = "查询支付状态")
    @GetMapping("/status/{orderId}")
    public ApiResponse<PaymentStatusDTO> queryStatus(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        PaymentStatusDTO status = paymentCenterService.queryStatus(orderId, userId);
        return ApiResponse.success(status);
    }

    @Operation(summary = "关闭支付", description = "取消未支付的订单")
    @PostMapping("/close/{orderId}")
    public ApiResponse<Void> closePayment(
            @Parameter(description = "订单ID") @PathVariable Long orderId) {
        Long userId = SecurityUtils.getCurrentUserId();
        paymentCenterService.closePayment(orderId, userId);
        return ApiResponse.success("支付已关闭", null);
    }

    @Operation(summary = "发起退款")
    @PostMapping("/refund")
    public ApiResponse<String> refund(@RequestBody RefundRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String refundNo = paymentCenterService.refund(userId, request);
        return ApiResponse.success("退款已提交", refundNo);
    }

    @Operation(summary = "查询退款状态")
    @GetMapping("/refund/status/{refundNo}")
    public ApiResponse<RefundRecord> queryRefundStatus(
            @Parameter(description = "退款流水号") @PathVariable String refundNo) {
        RefundRecord record = paymentCenterService.queryRefundStatus(refundNo);
        return ApiResponse.success(record);
    }
}

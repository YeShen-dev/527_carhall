package com.autoparts.market.payment.controller;

import com.autoparts.market.payment.service.PaymentNotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;

/**
 * 支付回调通知 API — 无需登录认证，由第三方支付平台直连回调
 *
 * POST /api/payment/notify/wechat   微信支付异步通知
 * POST /api/payment/notify/alipay   支付宝异步通知
 */
@Slf4j
@Tag(name = "支付回调", description = "微信/支付宝异步回调通知接口（无需认证）")
@RestController
@RequestMapping("/api/payment/notify")
@RequiredArgsConstructor
public class PaymentNotifyController {

    private final PaymentNotifyService paymentNotifyService;

    @Operation(summary = "微信支付回调", description = "微信支付V3异步通知，需验签+AES解密")
    @PostMapping("/wechat")
    public String wechatNotify(HttpServletRequest request) {
        try {
            String body = readBody(request);
            log.info("[微信回调] 收到通知, requestId={}", request.getHeader("Request-ID"));
            return paymentNotifyService.handleWechatNotify(body);
        } catch (Exception e) {
            log.error("[微信回调处理异常]", e);
            // 微信：返回失败状态码会使微信重试
            return "{\"code\":\"FAIL\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    @Operation(summary = "支付宝支付回调", description = "支付宝异步通知，需RSA2验签")
    @PostMapping("/alipay")
    public String alipayNotify(HttpServletRequest request) {
        try {
            String body = readBody(request);
            log.info("[支付宝回调] 收到通知");
            return paymentNotifyService.handleAlipayNotify(body);
        } catch (Exception e) {
            log.error("[支付宝回调处理异常]", e);
            // 支付宝：返回"fail"会触发重试
            return "fail";
        }
    }

    private String readBody(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}

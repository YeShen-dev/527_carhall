package com.autoparts.market.payment.strategy;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.payment.config.PaymentProperties;
import com.autoparts.market.payment.dto.PayResponse;
import com.autoparts.market.payment.entity.PaymentRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝 PC 网页支付 — 支持真实API和Mock模式
 *
 * 真实模式: 使用 Alipay SDK 生成支付表单HTML → 前端提交跳转支付宝收银台
 * Mock模式: 返回模拟HTML表单
 *
 * 回调:
 *   return_url: 用户支付后浏览器同步跳回 (GET)
 *   notify_url: 支付宝服务端异步通知 (POST) — 需要RSA2验签
 */
@Slf4j
@Component
public class AlipayStrategy implements PaymentStrategy {

    private final PaymentProperties properties;
    private AlipayClient alipayClient;

    public AlipayStrategy(PaymentProperties properties) {
        this.properties = properties;
        if (!properties.isMock()) {
            PaymentProperties.AlipayConfig c = properties.getAlipay();
            AlipayConfig config = new AlipayConfig();
            config.setServerUrl(c.getGatewayUrl());
            config.setAppId(c.getAppId());
            config.setPrivateKey(c.getPrivateKey());
            config.setAlipayPublicKey(c.getAlipayPublicKey());
            config.setFormat("json");
            config.setCharset("UTF-8");
            config.setSignType("RSA2");
            try {
                this.alipayClient = new DefaultAlipayClient(config);
            } catch (AlipayApiException e) {
                log.error("[支付宝] SDK初始化失败", e);
            }
        }
    }

    @Override
    public String getPayMethod() {
        return "ALIPAY";
    }

    @Override
    public PayResponse createPayment(OrderMaster order, PaymentRecord record) throws Exception {
        String formHtml;

        if (properties.isMock()) {
            String returnUrl = properties.getAlipay().getReturnUrl() + "?paymentNo=" + record.getPaymentNo();
            formHtml = buildMockForm(order, record, returnUrl);
            log.info("[支付宝Mock] 生成模拟支付表单, paymentNo={} amount={}", record.getPaymentNo(), order.getTotalAmount());
        } else {
            PaymentProperties.AlipayConfig c = properties.getAlipay();

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(record.getPaymentNo());
            model.setTotalAmount(order.getTotalAmount().toString());
            model.setSubject("汽修配件-" + order.getOrderNo());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            // 超时时间: 30分钟
            model.setTimeoutExpress("30m");

            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
            request.setBizModel(model);
            request.setNotifyUrl(c.getNotifyUrl());
            request.setReturnUrl(c.getReturnUrl());

            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (!response.isSuccess()) {
                log.error("[支付宝] 下单失败: code={} msg={}", response.getCode(), response.getMsg());
                throw new RuntimeException("支付宝下单失败: " + response.getMsg());
            }

            formHtml = response.getBody();
            log.info("[支付宝] 下单成功: paymentNo={} amount={}", record.getPaymentNo(), order.getTotalAmount());
        }

        return PayResponse.builder()
                .paymentNo(record.getPaymentNo())
                .payMethod("ALIPAY")
                .qrCodeUrl(formHtml)
                .amount(order.getTotalAmount())
                .orderNo(order.getOrderNo())
                .expireTime(LocalDateTime.now().plusMinutes(properties.getExpireMinutes()))
                .build();
    }

    @Override
    public boolean handleNotify(String notifyBody) throws Exception {
        log.info("[支付宝回调] 收到回调通知");

        if (properties.isMock()) {
            log.info("[支付宝回调Mock] 模拟验签通过，支付成功");
            return true;
        }

        // 真实模式: RSA2验签
        Map<String, String> params = parseFormParams(notifyBody);
        PaymentProperties.AlipayConfig c = properties.getAlipay();

        boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                c.getAlipayPublicKey(),
                "UTF-8",
                "RSA2"
        );

        if (!signVerified) {
            log.error("[支付宝回调] RSA2验签失败");
            return false;
        }

        // 校验 app_id
        String notifyAppId = params.get("app_id");
        if (!c.getAppId().equals(notifyAppId)) {
            log.error("[支付宝回调] app_id不匹配: notify={} config={}", notifyAppId, c.getAppId());
            return false;
        }

        // 校验交易状态
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.warn("[支付宝回调] 交易状态非成功: {}", tradeStatus);
            return false;
        }

        log.info("[支付宝回调] 验签通过: out_trade_no={} trade_no={} amount={}",
                params.get("out_trade_no"), params.get("trade_no"), params.get("total_amount"));
        return true;
    }

    @Override
    public String refund(String paymentNo, BigDecimal refundAmount, String reason) throws Exception {
        log.info("[支付宝退款] paymentNo={} amount={}", paymentNo, refundAmount);

        if (properties.isMock()) {
            String refundNo = "ALI" + System.currentTimeMillis();
            log.info("[支付宝退款Mock] 模拟退款成功, refundNo={}", refundNo);
            return refundNo;
        }

        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(paymentNo);
        model.setRefundAmount(refundAmount.toString());
        model.setRefundReason(reason);
        model.setOutRequestNo("REF" + System.currentTimeMillis());

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizModel(model);

        AlipayTradeRefundResponse response = alipayClient.execute(request);
        if (!response.isSuccess()) {
            log.error("[支付宝退款] 失败: code={} msg={}", response.getCode(), response.getMsg());
            throw new RuntimeException("支付宝退款失败: " + response.getMsg());
        }

        log.info("[支付宝退款] 成功: refundNo={}", model.getOutRequestNo());
        return model.getOutRequestNo();
    }

    @Override
    public String queryRefundStatus(String refundNo) throws Exception {
        if (properties.isMock()) {
            return "SUCCESS";
        }
        // 支付宝退款是同步返回结果的，这里简单返回 SUCCESS
        return "SUCCESS";
    }

    /** 解析支付宝回调的 form 参数为 Map */
    private Map<String, String> parseFormParams(String body) {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) return params;
        for (String pair : body.split("&")) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                params.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return params;
    }

    /** 构建模拟支付宝支付表单HTML */
    private String buildMockForm(OrderMaster order, PaymentRecord record, String returnUrl) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>支付宝支付</title></head><body>"
                + "<form id='alipay-form' action='" + returnUrl + "' method='GET' style='text-align:center;padding:40px;background:#1c1c2c;border-radius:16px;max-width:500px;margin:0 auto'>"
                + "<div style='font-size:48px;margin-bottom:20px'>&#128179;</div>"
                + "<div style='font-size:16px;color:#9898a8;margin-bottom:8px'>支付金额</div>"
                + "<div style='font-size:36px;font-weight:700;color:#1677ff;margin-bottom:24px'>¥" + order.getTotalAmount() + "</div>"
                + "<div style='font-size:14px;color:#707080;margin-bottom:8px'>订单号: " + order.getOrderNo() + "</div>"
                + "<div style='font-size:13px;color:#707080;margin-bottom:24px'>支付流水号: " + record.getPaymentNo() + "</div>"
                + "<button type='submit' style='background:#1677ff;color:#fff;border:none;padding:14px 48px;font-size:18px;border-radius:8px;cursor:pointer'>确认支付</button>"
                + "</form></body></html>";
    }
}

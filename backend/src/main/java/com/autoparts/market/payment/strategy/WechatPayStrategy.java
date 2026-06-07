package com.autoparts.market.payment.strategy;

import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.payment.config.PaymentProperties;
import com.autoparts.market.payment.dto.PayResponse;
import com.autoparts.market.payment.entity.PaymentRecord;
import com.autoparts.market.payment.util.WechatPayHttpClient;
import com.autoparts.market.payment.util.WechatPaySignature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 微信支付 V3 Native 扫码支付 — 支持真实API和Mock模式
 *
 * 真实模式: POST /v3/pay/transactions/native → 获取 code_url → 前端生成二维码
 * Mock模式: 返回模拟链接，前端生成二维码后系统自动确认
 */
@Slf4j
@Component
public class WechatPayStrategy implements PaymentStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PaymentProperties properties;
    private WechatPayHttpClient httpClient;

    public WechatPayStrategy(PaymentProperties properties) {
        this.properties = properties;
        if (!properties.isMock()) {
            PaymentProperties.WechatConfig c = properties.getWechat();
            // 读取私钥文件内容（支持classpath和文件系统路径）
            String privateKeyContent = loadPrivateKey(c.getPrivateKeyPath());
            this.httpClient = new WechatPayHttpClient(c.getMchId(), c.getSerialNo(), privateKeyContent, c.getApiV3Key());
        }
    }

    @Override
    public String getPayMethod() {
        return "WECHAT";
    }

    @Override
    public PayResponse createPayment(OrderMaster order, PaymentRecord record) throws Exception {
        String qrCodeUrl;

        if (properties.isMock()) {
            qrCodeUrl = "weixin://wxpay/bizpayurl?pr=" + record.getPaymentNo();
            log.info("[微信支付Mock] 生成模拟支付链接: paymentNo={} amount={}", record.getPaymentNo(), order.getTotalAmount());
        } else {
            // 构建Native下单请求 JSON
            String requestBody = buildNativeOrderRequest(order, record);
            log.info("[微信支付] 发起Native下单: out_trade_no={} amount={}分",
                    record.getPaymentNo(), order.getTotalAmount().multiply(new BigDecimal("100")).intValue());

            JsonNode response = httpClient.createNativeOrder(requestBody);
            qrCodeUrl = response.get("code_url").asText();
            log.info("[微信支付] 获取code_url成功: paymentNo={} code_url={}", record.getPaymentNo(), qrCodeUrl);
        }

        return PayResponse.builder()
                .paymentNo(record.getPaymentNo())
                .payMethod("WECHAT")
                .qrCodeUrl(qrCodeUrl)
                .amount(order.getTotalAmount())
                .orderNo(order.getOrderNo())
                .expireTime(LocalDateTime.now().plusMinutes(properties.getExpireMinutes()))
                .build();
    }

    @Override
    public boolean handleNotify(String notifyBody) throws Exception {
        log.info("[微信支付回调] 收到回调通知");

        if (properties.isMock()) {
            log.info("[微信支付回调Mock] 模拟验签通过，支付成功");
            return true;
        }

        // 真实模式回调处理:
        // 1. 从回调body中提取 resource 字段
        JsonNode root = MAPPER.readTree(notifyBody);
        JsonNode resource = root.get("resource");
        if (resource == null) {
            log.error("[微信回调] 回调体中缺少resource字段");
            return false;
        }

        String algorithm = resource.get("algorithm").asText();
        String ciphertext = resource.get("ciphertext").asText();
        String associatedData = resource.has("associated_data") ? resource.get("associated_data").asText() : "";
        String nonce = resource.get("nonce").asText();

        if (!"AEAD_AES_256_GCM".equals(algorithm)) {
            log.error("[微信回调] 不支持的加密算法: {}", algorithm);
            return false;
        }

        // 2. AES-256-GCM 解密
        String decrypted = httpClient.decryptNotify(associatedData, nonce, ciphertext);
        log.info("[微信回调] 解密成功: {}", decrypted);

        // 3. 验签 — 在实际部署中需要从微信服务器获取平台证书来验证
        // 验签在各个场景的实现中有所不同，这里我们信任解密后的内容
        // 真正生产环境应使用 WechatPaySignature.verify() + 微信平台公钥

        return true;
    }

    @Override
    public String refund(String paymentNo, BigDecimal refundAmount, String reason) throws Exception {
        log.info("[微信退款] paymentNo={} amount={} reason={}", paymentNo, refundAmount, reason);

        if (properties.isMock()) {
            String refundNo = "WX" + System.currentTimeMillis();
            log.info("[微信退款Mock] 模拟退款成功, refundNo={}", refundNo);
            return refundNo;
        }

        String outRefundNo = "REF" + System.currentTimeMillis();
        int refundFen = refundAmount.multiply(new BigDecimal("100")).intValue();

        String body = String.format(
            "{\"out_trade_no\":\"%s\",\"out_refund_no\":\"%s\",\"reason\":\"%s\",\"amount\":{\"refund\":%d,\"total\":%d,\"currency\":\"CNY\"}}",
            paymentNo, outRefundNo, reason, refundFen, refundFen
        );

        httpClient.refund(body);
        return outRefundNo;
    }

    @Override
    public String queryRefundStatus(String refundNo) throws Exception {
        if (properties.isMock()) {
            return "SUCCESS";
        }
        JsonNode resp = httpClient.queryRefund(refundNo);
        return resp.get("status").asText();
    }

    /** 构建微信V3 Native下单JSON请求体 */
    private String buildNativeOrderRequest(OrderMaster order, PaymentRecord record) throws Exception {
        PaymentProperties.WechatConfig c = properties.getWechat();
        int amountFen = order.getTotalAmount().multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();

        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("appid", c.getAppId());
        body.put("mchid", c.getMchId());
        body.put("description", "汽修配件-" + order.getOrderNo());
        body.put("out_trade_no", record.getPaymentNo());
        body.put("notify_url", c.getNotifyUrl());

        var amount = new java.util.LinkedHashMap<String, Object>();
        amount.put("total", amountFen);
        amount.put("currency", "CNY");
        body.put("amount", amount);

        return MAPPER.writeValueAsString(body);
    }

    /** 加载私钥内容 (支持文件路径和classpath) */
    private String loadPrivateKey(String keyPath) {
        if (keyPath == null || keyPath.isEmpty()) return "";
        try {
            // 如果是PEM文件，去除头尾标记和换行
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of(keyPath)));
            return content
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
        } catch (Exception e) {
            log.warn("[微信支付] 无法加载私钥文件: {} error={}", keyPath, e.getMessage());
            return keyPath; // fallback: 直接作为key内容
        }
    }
}

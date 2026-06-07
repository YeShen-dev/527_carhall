package com.autoparts.market.payment.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;

/**
 * 微信支付V3 HTTP客户端 — 实现WECHATPAY2-SHA256-RSA2048签名
 *
 * 签名规则: 构造规范请求串 → RSA-SHA256签名 → 组装Authorization头
 * 规范请求串格式: HTTP_METHOD\nURL_PATH\nTIMESTAMP\nNONCE_STR\nBODY\n
 */
@Slf4j
public class WechatPayHttpClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String mchId;
    private final String serialNo;
    private final String privateKey;
    private final String apiV3Key;

    public WechatPayHttpClient(String mchId, String serialNo, String privateKey, String apiV3Key) {
        this.mchId = mchId;
        this.serialNo = serialNo;
        this.privateKey = privateKey;
        this.apiV3Key = apiV3Key;
    }

    /** POST /v3/pay/transactions/native — 创建Native支付订单，返回 code_url */
    public JsonNode createNativeOrder(String requestBody) throws Exception {
        return post("https://api.mch.weixin.qq.com/v3/pay/transactions/native", requestBody);
    }

    /** GET /v3/pay/transactions/out-trade-no/{outTradeNo} — 查询订单 */
    public JsonNode queryByOutTradeNo(String outTradeNo) throws Exception {
        return get("/v3/pay/transactions/out-trade-no/" + outTradeNo);
    }

    /** POST /v3/refund/domestic/refunds — 申请退款 */
    public JsonNode refund(String requestBody) throws Exception {
        return post("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds", requestBody);
    }

    /** GET /v3/refund/domestic/refunds/{outRefundNo} — 查询退款 */
    public JsonNode queryRefund(String outRefundNo) throws Exception {
        return get("/v3/refund/domestic/refunds/" + outRefundNo);
    }

    /** WeChat callback decrypt with AES-256-GCM */
    public String decryptNotify(String associatedData, String nonce, String ciphertext) throws Exception {
        return WechatPaySignature.decrypt(
                associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext,
                apiV3Key
        );
    }

    private JsonNode post(String url, String body) throws Exception {
        String path = URI.create(url).getPath();
        String authToken = buildAuthToken("POST", path, body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authToken)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> resp = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("[微信支付V3] {} {} -> status={}, body={}", "POST", path, resp.statusCode(), resp.body());

        if (resp.statusCode() >= 400) {
            throw new RuntimeException("微信支付V3请求失败 [" + resp.statusCode() + "]: " + resp.body());
        }
        return MAPPER.readTree(resp.body());
    }

    private JsonNode get(String url) throws Exception {
        String path = URI.create(url).getPath();
        String authToken = buildAuthToken("GET", path, "");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", authToken)
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> resp = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("[微信支付V3] {} {} -> status={}", "GET", path, resp.statusCode());

        if (resp.statusCode() >= 400) {
            throw new RuntimeException("微信支付V3请求失败 [" + resp.statusCode() + "]: " + resp.body());
        }
        return MAPPER.readTree(resp.body());
    }

    /**
     * 构建 Wechatpay-SHA256-RSA2048 Authorization 头
     */
    private String buildAuthToken(String method, String path, String body) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String nonce = generateNonce(32);

        String message = method + "\n"
                + path + "\n"
                + timestamp + "\n"
                + nonce + "\n"
                + body + "\n";

        String signature = WechatPaySignature.sign(message, privateKey);

        return "WECHATPAY2-SHA256-RSA2048 "
                + "mchid=\"" + mchId + "\","
                + "nonce_str=\"" + nonce + "\","
                + "signature=\"" + signature + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + serialNo + "\"";
    }

    private String generateNonce(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

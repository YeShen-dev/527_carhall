package com.autoparts.market.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付系统配置 — 支持 mock 模式，无真实商户号时用 mock 模式本地运行
 *
 * application.yml:
 * payment.mock=true   # mock模式
 * payment.mock=false  # 真实支付（需配置对应渠道参数）
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    /** 是否使用模拟支付模式 */
    private boolean mock = true;

    /** 支付过期时间（分钟），默认30分钟 */
    private int expireMinutes = 30;

    /** 微信支付配置 */
    private WechatConfig wechat = new WechatConfig();

    /** 支付宝配置 */
    private AlipayConfig alipay = new AlipayConfig();

    @Data
    public static class WechatConfig {
        private String appId;
        private String mchId;
        private String apiV3Key;
        private String privateKeyPath;
        private String serialNo;
        private String notifyUrl;
    }

    @Data
    public static class AlipayConfig {
        private String appId;
        private String privateKey;
        private String alipayPublicKey;
        private String gatewayUrl;
        private String notifyUrl;
        private String returnUrl;
    }
}

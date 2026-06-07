package com.autoparts.market.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务 — 使用 StringRedisTemplate 保证 Redis 值无 JSON 序列化
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_TTL_MINUTES = 5;

    @Value("${captcha.enabled:true}")
    private boolean captchaEnabled;

    /**
     * 生成验证码，返回 uuid + plain code
     */
    public Map<String, String> generate() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9999));
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + uuid,
                code,
                CAPTCHA_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        Map<String, String> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("code", code);
        return result;
    }

    /**
     * 校验验证码，一次有效
     */
    public boolean validate(String uuid, String code) {
        if (!captchaEnabled) return true;
        if (uuid == null || code == null) return false;
        String key = CAPTCHA_PREFIX + uuid;
        String stored = stringRedisTemplate.opsForValue().get(key);
        if (stored == null) return false;
        boolean valid = code.equalsIgnoreCase(stored);
        if (valid) {
            stringRedisTemplate.delete(key);
        }
        return valid;
    }

    public boolean isCaptchaEnabled() {
        return captchaEnabled;
    }
}

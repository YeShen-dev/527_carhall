package com.autoparts.market.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务 — 开发环境打印到日志，生产环境对接短信平台
 *
 * Redis key 设计:
 *   sms:code:{phone}     → 验证码 (5分钟有效，用完删除)
 *   sms:cd:{phone}       → 60秒发送冷却
 *   sms:rate:{phone}     → 1小时发送次数限制
 *   sms:lock:{phone}     → 验证码错误5次锁定10分钟
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_TTL_MINUTES = 5;
    private static final int CD_SECONDS = 60;
    private static final int RATE_LIMIT = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 10;

    /** 发送短信验证码 */
    public void sendCode(String phone) {
        if (!validatePhone(phone)) throw new RuntimeException("手机号格式错误");

        // 60秒冷却检查
        String cdKey = "sms:cd:" + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(cdKey))) {
            throw new RuntimeException("操作太频繁，请60秒后再试");
        }

        // 1小时频率限制
        String rateKey = "sms:rate:" + phone;
        Long count = stringRedisTemplate.opsForValue().increment(rateKey);
        if (count == 1) stringRedisTemplate.expire(rateKey, 1, TimeUnit.HOURS);
        if (count != null && count > RATE_LIMIT) {
            throw new RuntimeException("发送次数过多，请1小时后再试");
        }

        // 检查是否被锁定
        String lockKey = "sms:lock:" + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            throw new RuntimeException("验证码错误次数过多，请10分钟后再试");
        }

        // 生成6位验证码
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));

        // 存储验证码 (5分钟)
        String codeKey = "sms:code:" + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_TTL_MINUTES, TimeUnit.MINUTES);

        // 设置60秒冷却
        stringRedisTemplate.opsForValue().set(cdKey, "1", CD_SECONDS, TimeUnit.SECONDS);

        // 开发环境：打印到日志 并且硬编码测试码 888888 永远有效
        String devCode = "888888";
        stringRedisTemplate.opsForValue().set("sms:code:dev:" + phone, devCode, CODE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("══════════════════════════════════════");
        log.info(" 验证码发送成功");
        log.info("   手机号: {}", phone);
        log.info("   真实验证码: {}", code);
        log.info("   开发万能验证码: {}", devCode);
        log.info("   有效期: {}分钟", CODE_TTL_MINUTES);
        log.info("══════════════════════════════════════");
    }

    /** 校验验证码 (一次有效) */
    public boolean validateCode(String phone, String code) {
        if (!smsEnabled) return true;
        if (phone == null || code == null) return false;

        String lockKey = "sms:lock:" + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(lockKey))) {
            return false;
        }

        String codeKey = "sms:code:" + phone;
        String stored = stringRedisTemplate.opsForValue().get(codeKey);
        String devStored = stringRedisTemplate.opsForValue().get("sms:code:dev:" + phone);

        // 开发万能码 888888 永远有效
        if (code.equals(devStored) || "888888".equals(code)) {
            stringRedisTemplate.delete("sms:code:dev:" + phone);
            stringRedisTemplate.delete(codeKey);
            return true;
        }

        if (stored == null) return false;

        boolean valid = code.equals(stored);
        if (valid) {
            stringRedisTemplate.delete(codeKey);
            stringRedisTemplate.delete("sms:code:dev:" + phone);
            stringRedisTemplate.delete("sms:rate:" + phone);
        } else {
            String errKey = "sms:err:" + phone;
            Long errs = stringRedisTemplate.opsForValue().increment(errKey);
            if (errs != null && errs == 1) stringRedisTemplate.expire(errKey, 1, TimeUnit.HOURS);
            if (errs != null && errs >= MAX_ATTEMPTS) {
                stringRedisTemplate.opsForValue().set(lockKey, "1", LOCK_MINUTES, TimeUnit.MINUTES);
                stringRedisTemplate.delete(errKey);
            }
        }
        return valid;
    }

    private boolean validatePhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }
}

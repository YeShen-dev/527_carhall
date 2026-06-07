package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.LoginRequest;
import com.autoparts.market.service.CaptchaService;
import com.autoparts.market.service.SmsCodeService;
import com.autoparts.market.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户认证 API
 *
 * 登录区分客户端来源：
 *   clientType = "miniapp" → 微信小程序，免验证码
 *   clientType = "web" / null → PC/H5，需要验证码
 *
 * 可通过 captcha.enabled=false 全局关闭验证码（开发/测试环境）
 */
@Slf4j
@Tag(name = "用户认证", description = "注册、登录接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CaptchaService captchaService;
    private final SmsCodeService smsCodeService;

    // ==================== 短信验证码 ====================

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sendCode")
    public ApiResponse<Map<String, String>> sendCode(@RequestBody Map<String, String> body) {
        smsCodeService.sendCode(body.get("phone"));
        return ApiResponse.success("发送成功", null);
    }

    @Operation(summary = "生成图形验证码 (兼容 verifyCode 调用)")
    @PostMapping("/verifyCode")
    public ApiResponse<Map<String, String>> verifyCode() {
        return ApiResponse.success(captchaService.generate());
    }

    @Operation(summary = "手机号+验证码注册")
    @PostMapping("/registerByPhone")
    public ApiResponse<Map<String, Object>> registerByPhone(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("smsCode");
        String password = body.get("password");
        if (!smsCodeService.validateCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        return ApiResponse.success(userService.registerByPhone(phone, password));
    }

    @Operation(summary = "手机号+验证码登录")
    @PostMapping("/loginByPhone")
    public ApiResponse<Map<String, Object>> loginByPhone(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("smsCode");
        if (!smsCodeService.validateCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        return ApiResponse.success(userService.loginByPhone(phone));
    }

    // ==================== 原有接口 ====================

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody LoginRequest req) {
        // miniapp 跳过图片验证码, web 端需验证
        if (!isMiniapp(req)) {
            requireCaptcha(req);
        }
        return ApiResponse.success(
                userService.register(
                        req.getUsername(),
                        req.getPassword(),
                        req.getPhone(),
                        req.getEmail(),
                        req.getSessionId()
                )
        );
    }

    /**
     * 用户登录
     *
     * 微信小程序 (clientType=miniapp): 跳过验证码校验, 直接验证用户名密码
     * PC/H5 (clientType=web 或 null): 需提交 captchaUuid + captchaCode
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        requireCaptcha(req);
        log.info("[登录] username={} clientType={} captchaSkipped={}",
                req.getUsername(), req.getClientType(), isMiniapp(req));
        return ApiResponse.success(
                userService.login(req.getUsername(), req.getPassword(), req.getSessionId())
        );
    }

    @Operation(summary = "刷新令牌", description = "使用 refreshToken 获取新的 accessToken")
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh(@RequestBody Map<String, String> body) {
        return ApiResponse.success(userService.refreshToken(body.get("refreshToken")));
    }

    // ---- private helpers ----

    /** 非小程序来源需要验证码；小程序直接跳过 */
    private void requireCaptcha(LoginRequest req) {
        if (isMiniapp(req)) return;
        if (req.getCaptchaUuid() == null || req.getCaptchaCode() == null) {
            throw new RuntimeException("验证码不能为空");
        }
        if (!captchaService.validate(req.getCaptchaUuid(), req.getCaptchaCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
    }

    private boolean isMiniapp(LoginRequest req) {
        return "miniapp".equalsIgnoreCase(req.getClientType());
    }
}

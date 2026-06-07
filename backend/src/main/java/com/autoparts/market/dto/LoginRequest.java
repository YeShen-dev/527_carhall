package com.autoparts.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 — 统一处理 Web(H5) 和微信小程序两端
 *
 * Web端: clientType=web, 需提交 captchaCode + captchaUuid
 * 小程序: clientType=miniapp, captcha 字段可空, 跳过验证码校验
 */
@Data
@Schema(description = "用户登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "客户端类型: web / miniapp", example = "miniapp", defaultValue = "web")
    private String clientType;

    @Schema(description = "验证码 key (uuid), Web端必填, 小程序可空")
    private String captchaUuid;

    @Schema(description = "验证码, Web端必填, 小程序可空")
    private String captchaCode;

    @Schema(description = "浏览器 sessionId, 用于购物车合并")
    private String sessionId;

    @Schema(description = "手机号 (注册时使用)")
    private String phone;

    @Schema(description = "邮箱 (注册时使用)")
    private String email;
}

package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "验证码", description = "图形验证码生成接口")
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @Operation(summary = "生成验证码", description = "返回验证码UUID和4位数字验证码")
    @GetMapping("/generate")
    public ApiResponse<Map<String, String>> generate() {
        return ApiResponse.success(captchaService.generate());
    }
}

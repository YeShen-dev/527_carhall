package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.entity.User;
import com.autoparts.market.service.UserService;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "个人中心", description = "用户信息查看与修改接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public ApiResponse<User> getInfo() {
        return ApiResponse.success(SecurityUtils.getCurrentUser());
    }

    @Operation(summary = "更新个人信息（手机号、邮箱）")
    @PutMapping("/info")
    public ApiResponse<Void> updateInfo(@RequestBody Map<String, String> body) {
        userService.updateUserInfo(
                SecurityUtils.getCurrentUserId(),
                body.get("phone"),
                body.get("email"));
        return ApiResponse.success("更新成功", null);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(@RequestBody Map<String, String> body) {
        userService.updatePassword(
                SecurityUtils.getCurrentUserId(),
                body.get("oldPassword"),
                body.get("newPassword"));
        return ApiResponse.success("密码修改成功", null);
    }
}

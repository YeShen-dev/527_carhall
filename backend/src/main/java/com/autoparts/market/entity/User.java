package com.autoparts.market.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Schema(description = "用户")
@Data
@TableName("user")
public class User {

    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码（BCrypt加密）")
    private String password;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "角色：USER / ADMIN")
    private String role;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}

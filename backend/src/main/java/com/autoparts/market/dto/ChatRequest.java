package com.autoparts.market.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "AI聊天请求")
@Data
public class ChatRequest {
    @Schema(description = "用户消息内容")
    private String message;
}

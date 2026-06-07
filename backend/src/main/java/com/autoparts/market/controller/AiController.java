package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.ChatRequest;
import com.autoparts.market.service.AiService;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI智能助手", description = "AI配件咨询与智能推荐接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI智能对话", description = "根据车型和故障描述推荐合适的汽修配件")
    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ApiResponse.error("消息不能为空");
        }
        String reply = aiService.chat(request.getMessage());
        return ApiResponse.success(reply);
    }

    @Operation(summary = "AI智能推荐", description = "基于用户浏览历史推荐配件")
    @GetMapping("/recommend")
    public ApiResponse<String> recommend() {
        String result = aiService.recommend(SecurityUtils.getCurrentUserId());
        return ApiResponse.success(result);
    }
}

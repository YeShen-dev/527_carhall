package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.entity.SeckillActivity;
import com.autoparts.market.service.SeckillService;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "秒杀活动", description = "限时秒杀活动接口")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "获取当前活动秒杀列表")
    @GetMapping("/activities")
    public ApiResponse<List<SeckillActivity>> listActivities() {
        return ApiResponse.success(seckillService.listActiveActivities());
    }

    @Operation(summary = "执行秒杀", description = "参与指定秒杀活动，扣减Redis库存并生成订单")
    @PostMapping("/{activityId}/execute")
    public ApiResponse<Map<String, Object>> execute(
            @Parameter(description = "秒杀活动ID") @PathVariable Long activityId) {
        return ApiResponse.success("秒杀成功", seckillService.executeSeckill(activityId, SecurityUtils.getCurrentUserId()));
    }
}

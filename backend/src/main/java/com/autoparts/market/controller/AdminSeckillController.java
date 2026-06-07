package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.PageResult;
import com.autoparts.market.entity.Product;
import com.autoparts.market.entity.SeckillActivity;
import com.autoparts.market.mapper.ProductMapper;
import com.autoparts.market.mapper.SeckillActivityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "管理后台-秒杀管理", description = "管理员秒杀活动CRUD接口")
@RestController
@RequestMapping("/api/admin/seckill")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSeckillController {

    private final SeckillActivityMapper seckillActivityMapper;
    private final ProductMapper productMapper;

    @Operation(summary = "分页查询秒杀活动")
    @GetMapping
    public ApiResponse<PageResult<SeckillActivity>> list(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SeckillActivity::getCreatedAt);
        Page<SeckillActivity> mpPage = seckillActivityMapper.selectPage(new Page<>(page + 1, size), wrapper);

        for (SeckillActivity a : mpPage.getRecords()) {
            Product p = productMapper.selectById(a.getProductId());
            if (p != null) a.setProductName(p.getName());
        }

        return ApiResponse.success(PageResult.from(mpPage, mpPage.getRecords()));
    }

    @Operation(summary = "创建秒杀活动")
    @PostMapping
    public ApiResponse<SeckillActivity> create(@RequestBody SeckillActivity activity) {
        activity.setCreatedAt(LocalDateTime.now());
        seckillActivityMapper.insert(activity);
        return ApiResponse.success("创建成功", activity);
    }

    @Operation(summary = "更新秒杀活动")
    @PutMapping("/{id}")
    public ApiResponse<SeckillActivity> update(@Parameter(description = "活动ID") @PathVariable Long id,
                                                @RequestBody SeckillActivity activity) {
        SeckillActivity existing = seckillActivityMapper.selectById(id);
        if (existing == null) throw new RuntimeException("活动不存在");
        existing.setProductId(activity.getProductId());
        existing.setSeckillPrice(activity.getSeckillPrice());
        existing.setStock(activity.getStock());
        existing.setStartTime(activity.getStartTime());
        existing.setEndTime(activity.getEndTime());
        seckillActivityMapper.updateById(existing);
        return ApiResponse.success("更新成功", existing);
    }

    @Operation(summary = "删除秒杀活动")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "活动ID") @PathVariable Long id) {
        seckillActivityMapper.deleteById(id);
        return ApiResponse.success("删除成功", null);
    }
}

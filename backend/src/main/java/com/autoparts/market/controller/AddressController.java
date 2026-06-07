package com.autoparts.market.controller;

import com.autoparts.market.entity.*;
import com.autoparts.market.mapper.*;
import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Tag(name = "收货地址", description = "用户收货地址管理")
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final com.autoparts.market.mapper.AddressMapper addressMapper;

    @Operation(summary = "我的地址列表")
    @GetMapping
    public ApiResponse<List<com.autoparts.market.entity.Address>> list() {
        Long uid = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(addressMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.autoparts.market.entity.Address>()
                        .eq(com.autoparts.market.entity.Address::getUserId, uid)
                        .orderByDesc(com.autoparts.market.entity.Address::getIsDefault)
                        .orderByDesc(com.autoparts.market.entity.Address::getId)));
    }

    @Operation(summary = "新增地址")
    @PostMapping
    public ApiResponse<com.autoparts.market.entity.Address> add(@RequestBody com.autoparts.market.entity.Address addr) {
        addr.setUserId(SecurityUtils.getCurrentUserId());
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(addr.getUserId());
        }
        addressMapper.insert(addr);
        return ApiResponse.success(addr);
    }

    @Operation(summary = "更新地址")
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody com.autoparts.market.entity.Address addr) {
        com.autoparts.market.entity.Address exist = addressMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(SecurityUtils.getCurrentUserId()))
            throw new RuntimeException("地址不存在");
        if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
            clearDefault(exist.getUserId());
        }
        addr.setId(id); addr.setUserId(exist.getUserId());
        addressMapper.updateById(addr);
        return ApiResponse.success(null);
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        com.autoparts.market.entity.Address exist = addressMapper.selectById(id);
        if (exist != null && exist.getUserId().equals(SecurityUtils.getCurrentUserId()))
            addressMapper.deleteById(id);
        return ApiResponse.success(null);
    }

    private void clearDefault(Long userId) {
        addressMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.autoparts.market.entity.Address>()
                .eq(com.autoparts.market.entity.Address::getUserId, userId)
                .set(com.autoparts.market.entity.Address::getIsDefault, 0));
    }
}

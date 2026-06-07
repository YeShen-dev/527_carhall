package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.PageResult;
import com.autoparts.market.entity.Product;
import com.autoparts.market.mapper.ProductMapper;
import com.autoparts.market.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "管理后台-商品管理", description = "管理员商品CRUD接口")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductMapper productMapper;
    private final ProductService productService;

    @Operation(summary = "分页查询商品（含关键词搜索）")
    @GetMapping
    public ApiResponse<PageResult<Product>> list(
            @Parameter(description = "搜索关键词（名称/分类/品牌/制造商）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Product::getName, keyword)
                .or().like(Product::getCategory, keyword)
                .or().like(Product::getBrand, keyword)
                .or().like(Product::getSpec, keyword)
                .or().like(Product::getManufacturer, keyword)
            );
        }
        wrapper.orderByAsc(Product::getId);

        Page<Product> mpPage = productMapper.selectPage(new Page<>(page + 1, size), wrapper);
        return ApiResponse.success(PageResult.from(mpPage, mpPage.getRecords()));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public ApiResponse<Product> add(@RequestBody Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        productService.clearProductCache();
        return ApiResponse.success("添加成功", product);
    }

    @Operation(summary = "更新商品信息")
    @PutMapping("/{id}")
    public ApiResponse<Product> update(@Parameter(description = "商品ID") @PathVariable Long id, @RequestBody Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) throw new RuntimeException("商品不存在");

        existing.setName(product.getName());
        existing.setCategory(product.getCategory());
        existing.setBrand(product.getBrand());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setImageUrl(product.getImageUrl());
        existing.setDescription(product.getDescription());
        existing.setSpec(product.getSpec());
        existing.setManufacturer(product.getManufacturer());
        if (product.getStatus() != null) {
            existing.setStatus(product.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        productMapper.updateById(existing);
        productService.clearProductCache();
        return ApiResponse.success("更新成功", existing);
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "商品ID") @PathVariable Long id) {
        productMapper.deleteById(id);
        productService.clearProductCache();
        return ApiResponse.success("删除成功", null);
    }
}

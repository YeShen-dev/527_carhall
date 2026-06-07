package com.autoparts.market.controller;

import com.autoparts.market.dto.ApiResponse;
import com.autoparts.market.dto.PageResult;
import com.autoparts.market.entity.Product;
import com.autoparts.market.entity.User;
import com.autoparts.market.service.BrowseHistoryService;
import com.autoparts.market.service.ProductService;
import com.autoparts.market.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "商品浏览", description = "商品列表与详情查询接口")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final BrowseHistoryService browseHistoryService;

    @Operation(summary = "分页查询商品列表", description = "支持关键词模糊搜索、分类品牌筛选、价格排序")
    @GetMapping
    public ApiResponse<PageResult<Product>> list(
            @Parameter(description = "模糊搜索关键词（匹配名称、分类、品牌、规格、厂商）") @RequestParam(required = false) String keyword,
            @Parameter(description = "商品分类") @RequestParam(required = false) String category,
            @Parameter(description = "品牌名称") @RequestParam(required = false) String brand,
            @Parameter(description = "排序方式：default(默认) / price_asc(价格升序) / price_desc(价格降序)") @RequestParam(required = false, defaultValue = "default") String sort,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "12") int size) {
        PageResult<Product> result = productService.listProducts(keyword, category, brand, sort, page, size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    public ApiResponse<Product> detail(@Parameter(description = "商品ID") @PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            browseHistoryService.recordView(user.getId(), id);
        }
        return ApiResponse.success(product);
    }
}

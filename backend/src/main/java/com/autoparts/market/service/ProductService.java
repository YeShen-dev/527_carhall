package com.autoparts.market.service;

import com.autoparts.market.dto.PageResult;
import com.autoparts.market.entity.Product;
import com.autoparts.market.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "mall:products:";
    private static final String CACHE_KEYS_SET = "mall:products:keys";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    public ProductService(ProductMapper productMapper,
                          RedisTemplate<String, Object> redisTemplate) {
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    public PageResult<Product> listProducts(String keyword, String category, String brand, String sort, int page, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "ON");

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                .like(Product::getName, keyword)
                .or().like(Product::getCategory, keyword)
                .or().like(Product::getBrand, keyword)
                .or().like(Product::getSpec, keyword)
                .or().like(Product::getManufacturer, keyword)
            );
        }

        if (category != null && !category.isBlank()) {
            wrapper.eq(Product::getCategory, category);
        }
        if (brand != null && !brand.isBlank()) {
            wrapper.like(Product::getBrand, brand);
        }

        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else {
            wrapper.orderByAsc(Product::getId);
        }

        Page<Product> mpPage = productMapper.selectPage(new Page<>(page + 1, size), wrapper);
        return PageResult.from(mpPage, mpPage.getRecords());
    }

    public Product getProductDetail(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return product;
    }

    public String getProductCatalogSummary() {
        return productMapper.selectList(null).stream()
                .map(p -> String.format("- [%s] %s（适用品牌：%s，价格：%.2f元）%s",
                        p.getCategory(), p.getName(), p.getBrand(), p.getPrice(),
                        p.getDescription() != null ? p.getDescription().substring(0, Math.min(50, p.getDescription().length())) : ""))
                .reduce("", (a, b) -> a + b + "\n");
    }

    public void clearProductCache() {
        try {
            java.util.Set<Object> keys = redisTemplate.opsForSet().members(CACHE_KEYS_SET);
            if (keys != null && !keys.isEmpty()) {
                for (Object key : keys) {
                    redisTemplate.delete(key.toString());
                }
                redisTemplate.delete(CACHE_KEYS_SET);
            }
        } catch (Exception ignored) {
        }
    }
}

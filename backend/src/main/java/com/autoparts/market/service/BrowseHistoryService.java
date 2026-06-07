package com.autoparts.market.service;

import com.autoparts.market.entity.Product;
import com.autoparts.market.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrowseHistoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;
    private static final String HISTORY_PREFIX = "user:history:";
    private static final int MAX_HISTORY = 10;

    public void recordView(Long userId, Long productId) {
        String key = HISTORY_PREFIX + userId;
        redisTemplate.opsForList().remove(key, 0, productId.toString());
        redisTemplate.opsForList().leftPush(key, productId.toString());
        redisTemplate.opsForList().trim(key, 0, MAX_HISTORY - 1);
    }

    public List<Product> getBrowseHistory(Long userId) {
        String key = HISTORY_PREFIX + userId;
        List<Object> ids = redisTemplate.opsForList().range(key, 0, MAX_HISTORY - 1);
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = ids.stream()
                .map(o -> Long.parseLong(o.toString()))
                .collect(Collectors.toList());
        return productMapper.selectBatchIds(productIds);
    }
}

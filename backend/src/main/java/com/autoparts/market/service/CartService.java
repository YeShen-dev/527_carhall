package com.autoparts.market.service;

import com.autoparts.market.dto.CartAddRequest;
import com.autoparts.market.dto.CartItemDTO;
import com.autoparts.market.entity.Product;
import com.autoparts.market.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;

    private static final String CART_PREFIX = "cart:";
    private static final String TEMP_CART_PREFIX = "cart:temp:";

    public void addItem(String userId, CartAddRequest request) {
        doAddItem(CART_PREFIX + userId, request);
    }

    public void addItemTemp(String sessionId, CartAddRequest request) {
        doAddItem(TEMP_CART_PREFIX + sessionId, request);
    }

    private void doAddItem(String key, CartAddRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        if (request.getQuantity() <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("库存不足，当前库存：" + product.getStock());
        }

        Object existingVal = redisTemplate.opsForHash().get(key, request.getProductId().toString());
        int existingQty = existingVal == null ? 0 : Integer.parseInt(existingVal.toString());
        int newQty = existingQty + request.getQuantity();

        if (newQty > product.getStock()) {
            throw new RuntimeException("购物车中该商品总数超过库存，当前库存：" + product.getStock());
        }

        redisTemplate.opsForHash().put(key, request.getProductId().toString(), String.valueOf(newQty));
    }

    public void updateItem(String userId, Long productId, int quantity) {
        doUpdateItem(CART_PREFIX + userId, productId, quantity);
    }

    public void updateItemTemp(String sessionId, Long productId, int quantity) {
        doUpdateItem(TEMP_CART_PREFIX + sessionId, productId, quantity);
    }

    private void doUpdateItem(String key, Long productId, int quantity) {
        if (quantity <= 0) {
            redisTemplate.opsForHash().delete(key, productId.toString());
            return;
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (quantity > product.getStock()) {
            throw new RuntimeException("库存不足，当前库存：" + product.getStock());
        }
        redisTemplate.opsForHash().put(key, productId.toString(), String.valueOf(quantity));
    }

    public void removeItem(String userId, Long productId) {
        redisTemplate.opsForHash().delete(CART_PREFIX + userId, productId.toString());
    }

    public void removeItemTemp(String sessionId, Long productId) {
        redisTemplate.opsForHash().delete(TEMP_CART_PREFIX + sessionId, productId.toString());
    }

    public void clearCart(String userId) {
        redisTemplate.delete(CART_PREFIX + userId);
    }

    public void clearCartTemp(String sessionId) {
        redisTemplate.delete(TEMP_CART_PREFIX + sessionId);
    }

    public List<CartItemDTO> listCart(String userId) {
        return doListCart(CART_PREFIX + userId);
    }

    public List<CartItemDTO> listCartTemp(String sessionId) {
        return doListCart(TEMP_CART_PREFIX + sessionId);
    }

    private List<CartItemDTO> doListCart(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> productIds = entries.keySet().stream()
                .map(k -> Long.parseLong(k.toString()))
                .collect(Collectors.toList());

        List<Product> products = productMapper.selectBatchIds(productIds);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<CartItemDTO> items = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long pid = Long.parseLong(entry.getKey().toString());
            int qty = Integer.parseInt(entry.getValue().toString());
            Product product = productMap.get(pid);
            if (product != null) {
                CartItemDTO dto = new CartItemDTO();
                dto.setProductId(pid);
                dto.setProductName(product.getName());
                dto.setProductImage(product.getImageUrl());
                dto.setPrice(product.getPrice());
                dto.setQuantity(qty);
                dto.setStock(product.getStock());
                items.add(dto);
            }
        }
        items.sort(Comparator.comparing(CartItemDTO::getProductId));
        return items;
    }

    public Map<Long, Integer> getCartMap(String userId) {
        String key = CART_PREFIX + userId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<Long, Integer> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            result.put(Long.parseLong(entry.getKey().toString()),
                       Integer.parseInt(entry.getValue().toString()));
        }
        return result;
    }

    public void mergeTempToUser(String sessionId, String userId) {
        String tempKey = TEMP_CART_PREFIX + sessionId;
        String userKey = CART_PREFIX + userId;
        Map<Object, Object> tempEntries = redisTemplate.opsForHash().entries(tempKey);
        if (tempEntries.isEmpty()) return;

        for (Map.Entry<Object, Object> entry : tempEntries.entrySet()) {
            String field = entry.getKey().toString();
            int tempQty = Integer.parseInt(entry.getValue().toString());
            Object existingVal = redisTemplate.opsForHash().get(userKey, field);
            int existingQty = existingVal == null ? 0 : Integer.parseInt(existingVal.toString());
            redisTemplate.opsForHash().put(userKey, field, String.valueOf(existingQty + tempQty));
        }
        redisTemplate.delete(tempKey);
    }
}

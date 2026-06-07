package com.autoparts.market.service;

import com.autoparts.market.entity.OrderItem;
import com.autoparts.market.entity.OrderMaster;
import com.autoparts.market.entity.Product;
import com.autoparts.market.entity.SeckillActivity;
import com.autoparts.market.mapper.OrderItemMapper;
import com.autoparts.market.mapper.OrderMasterMapper;
import com.autoparts.market.mapper.ProductMapper;
import com.autoparts.market.mapper.SeckillActivityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 秒杀服务 — 使用 StringRedisTemplate 保证 Redis 值为纯数字字符串
 *
 * 库存 key:  seckill:stock:{activityId}   → 值: 222（纯数字，无引号）
 * 用户 key:  seckill:users:{activityId}   → Set of userId
 *
 * 注意: 旧代码使用 RedisTemplate<Object> + GenericJackson2JsonRedisSerializer
 * 会导致库存值被序列化为 "\"222\""（带 JSON 双引号），listActiveActivities()
 * 已做兼容处理，读取时自动清理脏数据并重新初始化。
 */
@Slf4j
@Service
public class SeckillService {

    private final SeckillActivityMapper seckillActivityMapper;
    private final ProductMapper productMapper;
    private final OrderMasterMapper orderMasterMapper;
    private final OrderItemMapper orderItemMapper;
    private final StringRedisTemplate redis;

    public SeckillService(SeckillActivityMapper seckillActivityMapper,
                          ProductMapper productMapper,
                          OrderMasterMapper orderMasterMapper,
                          OrderItemMapper orderItemMapper,
                          StringRedisTemplate redis) {
        this.seckillActivityMapper = seckillActivityMapper;
        this.productMapper = productMapper;
        this.orderMasterMapper = orderMasterMapper;
        this.orderItemMapper = orderItemMapper;
        this.redis = redis;
    }

    /**
     * 列出当前活跃的秒杀活动，同时从 Redis 读取实时库存。
     * 兼容旧版 JSON 序列化脏数据：检测到则删除 key 重新初始化。
     */
    public List<SeckillActivity> listActiveActivities() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(SeckillActivity::getStartTime, now)
               .ge(SeckillActivity::getEndTime, now);
        List<SeckillActivity> activities = seckillActivityMapper.selectList(wrapper);

        for (SeckillActivity a : activities) {
            Product p = productMapper.selectById(a.getProductId());
            if (p != null) {
                a.setProductName(p.getName());
                a.setProductPrice(p.getPrice());
                a.setProductImage(p.getImageUrl());
            }

            String stockKey = "seckill:stock:" + a.getId();
            String raw = redis.opsForValue().get(stockKey);

            if (raw == null) {
                // Key 不存在 → 用数据库 stock 初始化
                initStockKey(stockKey, a.getStock());
                a.setStock(a.getStock());
            } else {
                // 兼容旧版 JSON 序列化脏数据: 值可能为 "\"222\"" 而非 "222"
                int parsed = parseStockValue(stockKey, raw, a.getStock());
                a.setStock(parsed);
            }
        }
        return activities;
    }

    // ---- 秒杀核心 ----

    @Transactional
    public Map<String, Object> executeSeckill(Long activityId, Long userId) {
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) throw new RuntimeException("秒杀活动不存在");

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime())) throw new RuntimeException("秒杀尚未开始");
        if (now.isAfter(activity.getEndTime())) throw new RuntimeException("秒杀已结束");

        // 防重复抢购
        String userKey = "seckill:users:" + activityId;
        Boolean alreadyDone = redis.opsForSet().isMember(userKey, userId.toString());
        if (Boolean.TRUE.equals(alreadyDone)) throw new RuntimeException("您已参与过本次秒杀");

        // 初始化 / 扣减 Redis 库存
        String stockKey = "seckill:stock:" + activityId;
        ensureStockKey(stockKey, activity.getStock());

        Long remaining = redis.opsForValue().decrement(stockKey);
        if (remaining == null || remaining < 0) {
            // 库存不足，回补
            redis.opsForValue().increment(stockKey);
            throw new RuntimeException("秒杀库存已抢光");
        }

        // 标记用户已参与
        redis.opsForSet().add(userKey, userId.toString());

        try {
            // 扣减 DB 库存
            Product product = productMapper.selectById(activity.getProductId());
            if (product == null || product.getStock() < 1) {
                throw new RuntimeException("商品库存不足");
            }
            product.setStock(product.getStock() - 1);
            if (productMapper.updateById(product) == 0) {
                throw new RuntimeException("库存扣减失败，请重试");
            }

            // 创建秒杀订单
            OrderMaster order = new OrderMaster();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setStatus("PENDING");
            order.setTotalAmount(activity.getSeckillPrice());
            orderMasterMapper.insert(order);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setPrice(activity.getSeckillPrice());
            item.setQuantity(1);
            item.setSubtotal(activity.getSeckillPrice());
            orderItemMapper.insert(item);

            Map<String, Object> result = new HashMap<>();
            result.put("orderId", order.getId());
            result.put("orderNo", order.getOrderNo());
            result.put("seckillPrice", activity.getSeckillPrice());
            return result;

        } catch (Exception e) {
            // 回滚 Redis 状态
            redis.opsForValue().increment(stockKey);
            redis.opsForSet().remove(userKey, userId.toString());
            throw e;
        }
    }

    // ---- 库存读写工具方法 ----

    /**
     * 初始化库存 key — 直接写入纯数字字符串如 "222"
     */
    private void initStockKey(String stockKey, int stock) {
        redis.opsForValue().set(stockKey, String.valueOf(stock));
        log.info("[秒杀库存] 初始化 {} = {}", stockKey, stock);
    }

    /**
     * 确保库存 key 存在（用于秒杀时懒初始化）
     */
    private void ensureStockKey(String stockKey, int stock) {
        Boolean ok = redis.opsForValue().setIfAbsent(stockKey, String.valueOf(stock));
        if (Boolean.TRUE.equals(ok)) {
            log.info("[秒杀库存] 懒初始化 {} = {}", stockKey, stock);
        }
    }

    /**
     * 解析 Redis 库存值 — 兼容旧版 JSON 序列化脏数据
     *
     * 合法值:  "222"   → parseInt → 222
     * 脏数据:  "\"222\"" → 检测到引号 → 删除 key → 重新初始化 → 返回正确值
     *
     * @param stockKey    Redis key
     * @param raw         Redis 中读取到的原始字符串
     * @param dbStock     数据库中的库存（用于重新初始化）
     * @return 正确的库存整数值
     */
    private int parseStockValue(String stockKey, String raw, int dbStock) {
        if (raw == null || raw.isEmpty()) {
            initStockKey(stockKey, dbStock);
            return dbStock;
        }

        // 情况1: 纯数字 → 直接解析
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
            // 情况2: 被 JSON 序列化包裹的值，如 "\"222\""
            log.warn("[秒杀库存] 检测到脏数据: key={} raw={} — 将清理并重新初始化", stockKey, raw);
        }

        // 情况3: 尝试去掉首尾引号再解析
        String stripped = raw;
        if (stripped.startsWith("\"") && stripped.endsWith("\"")) {
            stripped = stripped.substring(1, stripped.length() - 1);
            // 递归：去掉一层引号后可能仍是纯数字
            // 例如 "\"222\"" → 去掉外层引号 → "222" → 再去一层 → 222
            if (stripped.contains("\"")) {
                stripped = stripped.replace("\"", "");
            }
        }

        try {
            int value = Integer.parseInt(stripped);
            // 解析成功 → 删除脏 key 并重新写入纯数字值
            log.info("[秒杀库存] 脏数据已清理: {} 从 \"{}\" 修复为 {}", stockKey, raw, value);
            redis.delete(stockKey);
            initStockKey(stockKey, Math.max(value, dbStock));
            return Math.max(value, dbStock);
        } catch (NumberFormatException e) {
            // 彻底无法解析 → 删除 key 用 DB 值初始化
            log.error("[秒杀库存] 无法解析: key={} raw={} — 使用数据库库存 {} 覆盖", stockKey, raw, dbStock);
            redis.delete(stockKey);
            initStockKey(stockKey, dbStock);
            return dbStock;
        }
    }

    private String generateOrderNo() {
        return "SK" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}

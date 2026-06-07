package com.autoparts.market.payment.strategy;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付策略工厂 — 根据支付方式返回对应的策略实现
 *
 * Spring 自动注入所有 PaymentStrategy 的实现类到 strategies 列表中，
 * 初始化时按 getPayMethod() 建立映射，后续通过支付方式直接查找。
 * 扩展新支付渠道只需新增一个实现 PaymentStrategy 的 @Component 即可。
 */
@Component
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> strategyMap = new ConcurrentHashMap<>();

    public PaymentStrategyFactory(List<PaymentStrategy> strategies) {
        for (PaymentStrategy strategy : strategies) {
            strategyMap.put(strategy.getPayMethod(), strategy);
        }
    }

    /** 根据支付方式获取对应的策略实现 */
    public PaymentStrategy getStrategy(String payMethod) {
        PaymentStrategy strategy = strategyMap.get(payMethod);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + payMethod + "，支持: " + strategyMap.keySet());
        }
        return strategy;
    }
}

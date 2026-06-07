package com.autoparts.market.service;

import com.autoparts.market.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final ProductService productService;

    private final BrowseHistoryService browseHistoryService;

    public String recommend(Long userId) {
        List<Product> historyProducts = browseHistoryService.getBrowseHistory(userId);
        String catalogSummary = productService.getProductCatalogSummary();

        StringBuilder historyBuilder = new StringBuilder();
        if (!historyProducts.isEmpty()) {
            historyBuilder.append("用户最近浏览过的商品：\n");
            for (Product p : historyProducts) {
                historyBuilder.append(String.format("- [%s] %s（%s，%.2f元）\n",
                        p.getCategory(), p.getName(), p.getBrand(), p.getPrice()));
            }
        } else {
            historyBuilder.append("用户暂无浏览记录。\n");
        }

        String systemPrompt = """
                你是一个专业的汽修配件顾问助手。根据用户的浏览历史，从商城商品目录中推荐3款用户可能感兴趣的配件。
                推荐要有个性化，说明推荐理由（例如：浏览历史表明用户关注某车型/某品牌的配件，推荐同品牌关联商品）。
                如果用户没有浏览记录，则推荐商城的热门商品。

                以下是商城全部商品目录：
                %s

                %s

                请以JSON格式返回：{"recommendations":[{"productId":1,"reason":"推荐理由"}]}
                只返回JSON，不要其他内容。
                """.formatted(catalogSummary, historyBuilder.toString());

        return chatClient.call(systemPrompt);
    }

    public String chat(String userMessage) {
        String catalogSummary = productService.getProductCatalogSummary();
        String systemPrompt = """
                你是一个专业的汽修配件顾问助手。根据用户的车型和故障描述，推荐合适的配件。
                以下是当前商城可用的配件列表：

                %s

                请根据用户的问题，从上述列表中推荐最匹配的配件，并简要说明推荐理由。
                如果用户的问题与汽修配件无关，请引导用户描述车型和故障症状。
                回答要简洁实用，控制在200字以内。
                """.formatted(catalogSummary);

        return chatClient.call(systemPrompt + "\n\n用户问题：" + userMessage);
    }
}

package com.autoparts.market.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI autoPartsMallApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("汽修配件商城系统 API")
                        .version("1.0.0")
                        .description("汽修配件商城系统接口文档，提供商品浏览、购物车、订单管理、用户认证、AI智能推荐等功能。")
                        .contact(new Contact()
                                .name("AutoParts Mall")
                                .email("support@autoparts-mall.com")));
    }
}

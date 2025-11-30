package com.everyones_coupon.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Everyone's Coupon API")
                        .description("APIs for Everyone's Coupon project (관리자 및 공개 API)")
                        .version("v1.0.0")
                        .contact(new Contact().name("Everyones Coupon Team").email("dev@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Repository")
                        .url("https://github.com/2025-DGU-SE-04/everyones-coupon-back"));
    }
}

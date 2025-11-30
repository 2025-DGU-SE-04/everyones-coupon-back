package com.everyones_coupon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    /**
     * CORS 허용 출처(콤마로 구분된 목록). 기본값은 로컬 개발과 현재 정적 호스트를 포함합니다.
     * 예: http://localhost:3000,https://zealous-sand-04c7aae00.3.azurestaticapps.net
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000,https://zealous-sand-04c7aae00.3.azurestaticapps.net}")
    private String allowedOriginsProp;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOriginsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}

package com.everyones_coupon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        List<String> origins = Arrays.stream(allowedOriginsProp.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(ArrayList::new));
        // origins.add("http://localhost:3000");
        // 여기에 추가 허용 출처를 넣을 수 있습니다.
        // ArrayList를 활용하세요.

        registry.addMapping("/**")
                .allowedOrigins(origins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
    }
}

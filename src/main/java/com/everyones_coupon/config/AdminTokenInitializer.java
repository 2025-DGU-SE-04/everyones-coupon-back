package com.everyones_coupon.config;

import com.everyones_coupon.service.AdminTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminTokenInitializer implements ApplicationRunner {

    private final AdminTokenService adminTokenService;

    @Value("${app.admin.init-token:}")
    private String initToken;

    @Value("${app.admin.init-description:Master admin token}")
    private String initDescription;

    @Value("${app.admin.auto-generate:false}")
    private boolean autoGenerate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if ((initToken == null || initToken.isBlank()) && !autoGenerate) return;
        if (autoGenerate) {
            initToken = null; // let service generate secure random token
        }
        String saved = adminTokenService.createAdminToken(initToken, initDescription);
        // Do not log full token in production. Log only limited information so token is not exposed in logs.
        String info = "(auto-generated)";
        if (initToken != null && !initToken.isBlank()) {
            info = maskToken(saved);
        }
        log.info("Admin token initialized: {}", info);
    }

    private String maskToken(String token) {
        if (token == null) return "";
        int len = token.length();
        if (len <= 6) return "*****";
        return token.substring(0, 3) + "..." + token.substring(len - 3);
    }
}

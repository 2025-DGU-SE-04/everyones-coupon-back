package com.everyones_coupon.config;

import com.everyones_coupon.service.AdminTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
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
        System.out.println("Admin token initialized: " + saved);
    }
}

package com.everyones_coupon.integration;

import com.everyones_coupon.repository.AdminTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {"app.admin.auto-generate:false", "app.admin.init-token:literal-token-123"})
public class AdminTokenInitializerProvidedTokenIntegrationTest {

    @Autowired
    private AdminTokenRepository adminTokenRepository;

    @Test
    void adminInitTokenStoredAsLiteral() {
        assertThat(adminTokenRepository.existsByToken("literal-token-123")).isTrue();
    }
}

package com.everyones_coupon.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Testcontainers
public abstract class IntegrationTestBase {

    // Using Testcontainers' org.testcontainers.containers.PostgreSQLContainer
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15.4")
            .withDatabaseName("everyones_coupon_test")
            .withUsername("test")
            .withPassword("test");
    static boolean DOCKER_AVAILABLE = true;

    @BeforeAll
    static void startContainer() {
        try {
            POSTGRES.start();
        } catch (Throwable t) {
            // Docker not available (e.g., CI environment without Docker); skip integration tests
            DOCKER_AVAILABLE = false;
        }
        Assumptions.assumeTrue(DOCKER_AVAILABLE, "Docker is not available - skipping integration tests");
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        if (!DOCKER_AVAILABLE) return;
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }
}

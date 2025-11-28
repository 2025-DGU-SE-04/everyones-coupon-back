package com.everyones_coupon.integration;

import com.everyones_coupon.domain.AdminToken;
import com.everyones_coupon.domain.Game;
import com.everyones_coupon.repository.AdminTokenRepository;
import com.everyones_coupon.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"app.admin.auto-generate:false"})
public class AdminE2ETest {

// Using TestRestTemplate with random port; port not required in this test

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AdminTokenRepository adminTokenRepository;

    @Autowired
    private GameRepository gameRepository;

    private String adminToken = "e2e-admin-token-123";
    private Game game;

    @BeforeEach
    void setup() {
        // 관리자 토큰이 DB에 존재하도록 보장
        if (!adminTokenRepository.existsByToken(adminToken)) {
            adminTokenRepository.save(AdminToken.builder().token(adminToken).description("e2e").build());
        }
        // 테스트에 사용할 게임 생성
        game = Game.builder().title("E2E Game").gameDescription("desc").couponCount(0).viewCount(0).official(false).build();
        gameRepository.save(game);
    }

    @Test
    void login_cookie_callProtected_logout_flow() {
        // 로그인
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"token\": \"%s\"}", adminToken);
        HttpEntity<String> req = new HttpEntity<>(body, headers);

        ResponseEntity<Void> loginResp = restTemplate.postForEntity("/api/admin/login", req, Void.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 쿠키가 설정되었는지 확인
        String setCookie = loginResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("ADMIN_SESSION");

        // 쿠키를 사용하여 setOfficial 엔드포인트 호출
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add(HttpHeaders.COOKIE, setCookie.split(";")[0]);
        headers2.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> body2 = new HttpEntity<>("{\"official\": true}", headers2);

        ResponseEntity<Void> officialResp = restTemplate.postForEntity("/api/admin/games/" + game.getId() + "/official", body2, Void.class);
        assertThat(officialResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        Game reloaded = gameRepository.findById(game.getId()).orElseThrow();
        assertThat(reloaded.isOfficial()).isTrue();

        // 로그아웃
        HttpEntity<Void> logoutReq = new HttpEntity<>(headers2);
        ResponseEntity<Void> logoutResp = restTemplate.postForEntity("/api/admin/logout", logoutReq, Void.class);
        assertThat(logoutResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        String logoutSetCookie = logoutResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(logoutSetCookie).contains("ADMIN_SESSION");
        assertThat(logoutSetCookie).contains("Max-Age=0");

        // 동일한 쿠키로 다시 요청하면 Unauthorized(401) 이어야 함
        ResponseEntity<Void> officialResp2 = restTemplate.postForEntity("/api/admin/games/" + game.getId() + "/official", body2, Void.class);
        assertThat(officialResp2.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

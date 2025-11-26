package com.everyones_coupon.service;

import com.everyones_coupon.domain.AdminSession;
import com.everyones_coupon.domain.Game;
import com.everyones_coupon.repository.AdminSessionRepository;
import com.everyones_coupon.repository.AdminTokenRepository;
import com.everyones_coupon.repository.CouponRepository;
import com.everyones_coupon.repository.GameRepository;
import com.everyones_coupon.storage.ImageStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminTokenRepository adminTokenRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private ImageStore imageStore;

    @Mock
    private AdminSessionRepository adminSessionRepository;

    @InjectMocks
    private AdminService adminService;

    @Captor
    private ArgumentCaptor<AdminSession> sessionCaptor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void isValidToken_true_whenExists() {
        when(adminTokenRepository.existsByToken("valid-token")).thenReturn(true);
        boolean ok = adminService.isValidToken("valid-token");
        assertThat(ok).isTrue();
    }

    @Test
    void isValidToken_false_whenNullOrNotExists() {
        when(adminTokenRepository.existsByToken("invalid-token")).thenReturn(false);
        assertThat(adminService.isValidToken(null)).isFalse();
        assertThat(adminService.isValidToken("invalid-token")).isFalse();
    }

    @Test
    void createSessionForToken_savesSessionAndReturnsId() {
        when(adminTokenRepository.existsByToken("t1")).thenReturn(true);
        String sessionId = adminService.createSessionForToken("t1");
        assertThat(sessionId).isNotNull();
        verify(adminSessionRepository, times(1)).save(sessionCaptor.capture());
        AdminSession saved = sessionCaptor.getValue();
        assertThat(saved.getToken()).isEqualTo("t1");
        assertThat(saved.getSessionId()).isEqualTo(sessionId);
        assertThat(saved.getExpiresAt()).isAfter(saved.getCreatedAt());
    }

    @Test
    void createSessionForToken_throws_whenInvalidToken() {
        when(adminTokenRepository.existsByToken("bad-token")).thenReturn(false);
        assertThatThrownBy(() -> adminService.createSessionForToken("bad-token")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getTokenForSession_throws_whenNotFound() {
        when(adminSessionRepository.findBySessionId("nope")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.getTokenForSession("nope")).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void uploadImage_throws_badRequest_whenBase64Invalid() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(Game.builder().title("G").build()));
        assertThatThrownBy(() -> adminService.uploadImage(1L, "not-base64"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void uploadImage_throws_internalServerError_whenImageStoreFails() throws Exception {
        // prepare: Game in repo
        Game game = Game.builder().title("G").build();
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        // create valid base64 image
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        String base64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());

        when(imageStore.saveImage(any(), any())).thenThrow(new IOException("disk full"));

        assertThatThrownBy(() -> adminService.uploadImage(1L, base64)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void getTokenForSession_returnsToken_whenValid() {
        AdminSession session = AdminSession.builder()
                .sessionId("s1")
                .token("t1")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(adminSessionRepository.findBySessionId("s1")).thenReturn(Optional.of(session));
        String token = adminService.getTokenForSession("s1");
        assertThat(token).isEqualTo("t1");
    }

    @Test
    void getTokenForSession_throws_whenExpired() {
        AdminSession session = AdminSession.builder()
                .sessionId("s2")
                .token("t1")
                .createdAt(LocalDateTime.now().minusDays(2))
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();
        when(adminSessionRepository.findBySessionId("s2")).thenReturn(Optional.of(session));
        assertThatThrownBy(() -> adminService.getTokenForSession("s2")).isInstanceOf(ResponseStatusException.class);
        verify(adminSessionRepository, times(1)).delete(session);
    }

    @Test
    void uploadImage_resizesAndCallsImageStore() throws Exception {
        // prepare: Game in repo
        Game game = Game.builder().title("G").build();
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        // prepare base64 image bigger than 1000x1000 (simulate landscape)
        BufferedImage img = new BufferedImage(1200, 600, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        String base64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());

        when(imageStore.saveImage(any(), any())).thenReturn("/uploads/abc.jpg");

        String url = adminService.uploadImage(1L, base64);

        assertThat(url).isEqualTo("/uploads/abc.jpg");
        // verify game image url is set
        verify(gameRepository, times(1)).save(game);
    }
}

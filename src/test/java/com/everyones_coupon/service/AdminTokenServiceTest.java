package com.everyones_coupon.service;

import com.everyones_coupon.repository.AdminTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// unused imports removed
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminTokenServiceTest {

    @Mock
    private AdminTokenRepository adminTokenRepository;

    @InjectMocks
    private AdminTokenService adminTokenService;

    @Test
    void createAdminToken_generates_and_saves_when_null() {
        when(adminTokenRepository.existsByToken(org.mockito.ArgumentMatchers.any())).thenReturn(false);
        String t = adminTokenService.createAdminToken(null, "desc");
        assertThat(t).isNotNull();
    }

    @Test
    void createAdminToken_uses_provided_token_when_not_exists() {
        when(adminTokenRepository.existsByToken("abc")).thenReturn(false);
        String t = adminTokenService.createAdminToken("abc", "desc");
        assertThat(t).isEqualTo("abc");
    }

    @Test
    void createAdminToken_saves_literal_token_as_provided() {
        when(adminTokenRepository.existsByToken("  my-literal-token  ")).thenReturn(false);
        ArgumentCaptor<com.everyones_coupon.domain.AdminToken> captor = ArgumentCaptor.forClass(com.everyones_coupon.domain.AdminToken.class);
        String t = adminTokenService.createAdminToken("  my-literal-token  ", "desc");
        assertThat(t).isEqualTo("  my-literal-token  ");
        verify(adminTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("  my-literal-token  ");
    }

    @Test
    void createAdminToken_returns_existing_when_already_exists() {
        when(adminTokenRepository.existsByToken("exists")).thenReturn(true);
        String t = adminTokenService.createAdminToken("exists", "desc");
        assertThat(t).isEqualTo("exists");
    }
}

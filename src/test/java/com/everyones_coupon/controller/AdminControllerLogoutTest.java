package com.everyones_coupon.controller;

import com.everyones_coupon.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@WebMvcTest(controllers = AdminController.class)
@TestPropertySource(properties = {"app.cookie.secure:true"})
class AdminControllerLogoutTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    void logout_with_session_cookie_invalidates_session_and_clears_cookie() throws Exception {
        doNothing().when(adminService).invalidateSession("session-123");

        mockMvc.perform(post("/api/admin/logout")
                .cookie(new jakarta.servlet.http.Cookie("ADMIN_SESSION", "session-123")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("ADMIN_SESSION")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(adminService).invalidateSession("session-123");
    }

    @Test
    void logout_with_bearer_token_invokes_invalidate_sessions_by_token() throws Exception {
        doNothing().when(adminService).invalidateSessionsForToken("token-abc");
        when(adminService.isValidToken("token-abc")).thenReturn(true);

        mockMvc.perform(post("/api/admin/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-abc"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("ADMIN_SESSION")));

        verify(adminService).invalidateSessionsForToken("token-abc");
    }
}

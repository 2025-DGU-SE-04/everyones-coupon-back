package com.everyones_coupon.controller;

import com.everyones_coupon.dto.AdminLoginRequest;
import com.everyones_coupon.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// content matcher not used

@ExtendWith(org.springframework.test.context.junit.jupiter.SpringExtension.class)
@WebMvcTest(controllers = AdminController.class)
@TestPropertySource(properties = {"app.cookie.secure:true"})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    void login_sets_admin_session_cookie() throws Exception {
        AdminLoginRequest req = new AdminLoginRequest();
        // set token via reflection or use JSON body
        String json = "{\"token\": \"valid-token\"}";

        when(adminService.isValidToken("valid-token")).thenReturn(true);
        when(adminService.createSessionForToken("valid-token")).thenReturn("session-123");

        mockMvc.perform(post("/api/admin/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("ADMIN_SESSION")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("SameSite=Lax")));
    }

        @Test
        void endpoints_require_admin_token_or_session() throws Exception {
        // adminService.validateAdminToken should throw UNAUTHORIZED on invalid token
        org.mockito.Mockito.doThrow(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED))
            .when(adminService).validateAdminToken(org.mockito.ArgumentMatchers.any());

        // make delete endpoint without token/session
        mockMvc.perform(post("/api/admin/games/1/official")
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content("{\"official\":true}"))
            .andExpect(status().isUnauthorized());
        }
}

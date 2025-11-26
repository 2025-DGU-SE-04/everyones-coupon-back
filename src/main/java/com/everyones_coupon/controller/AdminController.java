package com.everyones_coupon.controller;

import com.everyones_coupon.dto.AdminLoginRequest;
import com.everyones_coupon.dto.ImageUploadRequest;
import com.everyones_coupon.dto.OfficialRequest;
import com.everyones_coupon.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecureByDefault;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest req, HttpServletRequest request) {
        boolean ok = adminService.isValidToken(req.getToken());
        if (!ok) throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "token invalid");
        // create session (do not store admin token in cookie)
        String sessionId = adminService.createSessionForToken(req.getToken());
        boolean cookieSecure = cookieSecureByDefault || request.isSecure();
        ResponseCookie cookie = ResponseCookie.from("ADMIN_SESSION", sessionId)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(Duration.ofDays(1))
            .sameSite("Lax")
            .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @DeleteMapping("/games/{gameId}")
    public ResponseEntity<?> deleteGame(@PathVariable Long gameId, @RequestHeader(value = "Authorization", required = false) String authorization,
                                        @CookieValue(value = "ADMIN_SESSION", required = false) String adminSessionCookie) {
        String token = extractToken(authorization, adminSessionCookie);
        adminService.validateAdminToken(token);
        adminService.deleteGame(gameId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/coupons/{couponId}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long couponId, @RequestHeader(value = "Authorization", required = false) String authorization,
                                           @CookieValue(value = "ADMIN_SESSION", required = false) String adminSessionCookie) {
        String token = extractToken(authorization, adminSessionCookie);
        adminService.validateAdminToken(token);
        adminService.deleteCoupon(couponId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/games/{gameId}/official")
    public ResponseEntity<?> setOfficial(@PathVariable Long gameId, @RequestBody OfficialRequest req,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @CookieValue(value = "ADMIN_SESSION", required = false) String adminSessionCookie) {
        String token = extractToken(authorization, adminSessionCookie);
        adminService.validateAdminToken(token);
        adminService.setOfficial(gameId, req.isOfficial());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/games/{gameId}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long gameId, @RequestBody ImageUploadRequest req,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @CookieValue(value = "ADMIN_SESSION", required = false) String adminSessionCookie) {
        String token = extractToken(authorization, adminSessionCookie);
        adminService.validateAdminToken(token);
        String url = adminService.uploadImage(gameId, req.getImageData());
        return ResponseEntity.ok(url);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @CookieValue(value = "ADMIN_SESSION", required = false) String adminSessionCookie,
                                    HttpServletRequest request) {
        // If bearer token present, invalidate sessions for that token
        if (authorization != null && authorization.toLowerCase().startsWith("bearer ")) {
            String token = authorization.substring(7);
            adminService.invalidateSessionsForToken(token);
        }
        // If session cookie present, invalidate that session
        if (adminSessionCookie != null) {
            adminService.invalidateSession(adminSessionCookie);
        }

        boolean cookieSecure = cookieSecureByDefault || request.isSecure();
        ResponseCookie cookie = ResponseCookie.from("ADMIN_SESSION", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    private String extractToken(String authorization, String cookieToken) {
        if (authorization != null && authorization.toLowerCase().startsWith("bearer ")) {
            return authorization.substring(7);
        }
        if (cookieToken == null) return null;
        // cookieToken is a session id; map to admin token
        return adminService.getTokenForSession(cookieToken);
    }
}

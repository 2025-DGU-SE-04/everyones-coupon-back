package com.everyones_coupon.controller;

import com.everyones_coupon.dto.AdminLoginRequest;
import com.everyones_coupon.dto.ImageUploadRequest;
import com.everyones_coupon.dto.OfficialRequest;
import com.everyones_coupon.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest req) {
        boolean ok = adminService.login(req.getToken());
        if (!ok) throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "token invalid");
        // issue cookie
        ResponseCookie cookie = ResponseCookie.from("ADMIN_TOKEN", req.getToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
    }

    @DeleteMapping("/games/{gameId}")
    public ResponseEntity<?> deleteGame(@PathVariable Long gameId, @RequestHeader(value = "Authorization", required = false) String authorization,
                                        @CookieValue(value = "ADMIN_TOKEN", required = false) String adminTokenCookie) {
        String token = extractToken(authorization, adminTokenCookie);
        adminService.validateAdminToken(token);
        adminService.deleteGame(gameId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/coupons/{couponId}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long couponId, @RequestHeader(value = "Authorization", required = false) String authorization,
                                           @CookieValue(value = "ADMIN_TOKEN", required = false) String adminTokenCookie) {
        String token = extractToken(authorization, adminTokenCookie);
        adminService.validateAdminToken(token);
        adminService.deleteCoupon(couponId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/games/{gameId}/official")
    public ResponseEntity<?> setOfficial(@PathVariable Long gameId, @RequestBody OfficialRequest req,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @CookieValue(value = "ADMIN_TOKEN", required = false) String adminTokenCookie) {
        String token = extractToken(authorization, adminTokenCookie);
        adminService.validateAdminToken(token);
        adminService.setOfficial(gameId, req.isOfficial());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/games/{gameId}/image")
    public ResponseEntity<?> uploadImage(@PathVariable Long gameId, @RequestBody ImageUploadRequest req,
                                         @RequestHeader(value = "Authorization", required = false) String authorization,
                                         @CookieValue(value = "ADMIN_TOKEN", required = false) String adminTokenCookie) {
        String token = extractToken(authorization, adminTokenCookie);
        adminService.validateAdminToken(token);
        String url = adminService.uploadImage(gameId, req.getImageData());
        return ResponseEntity.ok(url);
    }

    private String extractToken(String authorization, String cookieToken) {
        if (authorization != null && authorization.toLowerCase().startsWith("bearer ")) {
            return authorization.substring(7);
        }
        return cookieToken;
    }
}

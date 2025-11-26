package com.everyones_coupon.service;

import com.everyones_coupon.domain.AdminToken;
import com.everyones_coupon.domain.Game;
import com.everyones_coupon.repository.AdminTokenRepository;
import com.everyones_coupon.repository.CouponRepository;
import com.everyones_coupon.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminTokenRepository adminTokenRepository;
    private final GameRepository gameRepository;
    private final CouponRepository couponRepository;

    public boolean login(String token) {
        if (token == null || token.isBlank()) return false;
        return adminTokenRepository.existsByToken(token);
    }

    @Transactional
    public void deleteGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "게임을 찾을 수 없습니다."));
        gameRepository.delete(game);
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        if (!couponRepository.existsById(couponId)) {
            throw new ResponseStatusException(NOT_FOUND, "쿠폰을 찾을 수 없습니다.");
        }
        couponRepository.deleteById(couponId);
    }

    @Transactional
    public void setOfficial(Long gameId, boolean official) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "게임을 찾을 수 없습니다."));
        if (official) {
            game.markOfficial();
        } else {
            game.setOfficial(false);
        }
        gameRepository.save(game);
    }

    @Transactional
    public String uploadImage(Long gameId, String base64Image) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "게임을 찾을 수 없습니다."));

        if (base64Image == null || base64Image.isBlank()) {
            throw new ResponseStatusException(NOT_FOUND, "이미지 데이터가 없습니다.");
        }

        // decode base64
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(NOT_FOUND, "이미지 데이터가 올바른 Base64가 아닙니다.");
        }

        // write to uploads directory
        Path uploadsDir = Paths.get("uploads");
        try {
            if (!Files.exists(uploadsDir)) Files.createDirectories(uploadsDir);
            String filename = UUID.randomUUID().toString() + ".png";
            Path out = uploadsDir.resolve(filename);
            Files.write(out, bytes);
            String url = "/uploads/" + filename; // simple path; adjust as needed
            game.setGameImageUrl(url);
            gameRepository.save(game);
            return url;
        } catch (IOException e) {
            throw new ResponseStatusException(NOT_FOUND, "이미지 저장 중 오류 발생");
        }
    }
}

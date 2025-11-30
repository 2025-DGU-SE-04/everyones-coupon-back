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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.util.Base64;
import java.util.UUID;
import com.everyones_coupon.storage.ImageStore;
import com.everyones_coupon.domain.AdminSession;
import com.everyones_coupon.repository.AdminSessionRepository;
import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
 

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminTokenRepository adminTokenRepository;
    private final GameRepository gameRepository;
    private final CouponRepository couponRepository;
    private final ImageStore imageStore;
    private final AdminSessionRepository adminSessionRepository;

    public boolean isValidToken(String token) {
        if (token == null || token.isBlank()) return false;
        return adminTokenRepository.existsByToken(token);
    }

    public String createSessionForToken(String token) {
        if (!isValidToken(token)) throw new ResponseStatusException(UNAUTHORIZED, "관리자 인증 실패");
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        AdminSession session = AdminSession.builder()
                .sessionId(sessionId)
                .token(token)
                .createdAt(now)
                .expiresAt(now.plusDays(1))
                .build();
        adminSessionRepository.save(session);
        return sessionId;
    }

    public String getTokenForSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) throw new ResponseStatusException(UNAUTHORIZED, "관리자 세션 없음");
        AdminSession session = adminSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "관리자 세션 유효하지 않음"));
        if (session.isExpired()) {
            adminSessionRepository.delete(session);
            throw new ResponseStatusException(UNAUTHORIZED, "관리자 세션 만료");
        }
        return session.getToken();
    }

    public void invalidateSession(String sessionId) {
        adminSessionRepository.findBySessionId(sessionId).ifPresent(adminSessionRepository::delete);
    }

    public void invalidateSessionsForToken(String token) {
        adminSessionRepository.deleteByToken(token);
    }

    public void validateAdminToken(String token) {
        if (token == null || token.isBlank() || !adminTokenRepository.existsByToken(token)) {
            throw new ResponseStatusException(UNAUTHORIZED, "관리자 인증에 실패했습니다.");
        }
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
        game.setOfficial(official);
        gameRepository.save(game);
    }

    @Transactional
    public String uploadImage(Long gameId, String base64Image) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "게임을 찾을 수 없습니다."));

        if (base64Image == null || base64Image.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "이미지 데이터가 없습니다.");
        }

        // 1. Base64 헤더(Metadata) 전처리: "data:image/..." 부분 제거
        // 메모리 효율을 위해 split 대신 indexOf와 substring 사용
        int commaIndex = base64Image.indexOf(",");
        if (commaIndex >= 0) {
            base64Image = base64Image.substring(commaIndex + 1);
        }

        // 2. Base64 디코딩
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "이미지 데이터가 잘못된 Base64 형식입니다.");
        }

        // 3. 이미지 처리 (리사이징 및 JPEG 압축)
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            BufferedImage inputImage = ImageIO.read(in);
            if (inputImage == null) {
                throw new ResponseStatusException(BAD_REQUEST, "유효하지 않은 이미지 파일입니다.");
            }

            // 리사이징 로직 (최대 1000px)
            int maxSize = 1000;
            int width = inputImage.getWidth();
            int height = inputImage.getHeight();
            int targetWidth = width;
            int targetHeight = height;

            if (width > maxSize || height > maxSize) {
                double ratio = Math.min((double) maxSize / width, (double) maxSize / height);
                targetWidth = (int) Math.round(width * ratio);
                targetHeight = (int) Math.round(height * ratio);
            }

            // 새 이미지 버퍼 생성 (TYPE_INT_RGB로 변환하여 JPG 호환성 확보)
            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = outputImage.createGraphics();
            
            // 고품질 리사이징 옵션 적용
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(inputImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // 4. JPEG 인코딩 (압축률 75%)
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.75f);

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                jpgWriter.setOutput(ios);
                jpgWriter.write(null, new IIOImage(outputImage, null, null), jpgWriteParam);
            } finally {
                jpgWriter.dispose(); // 리소스 해제 필수
            }

            // 5. 저장 및 DB 업데이트
            String filename = UUID.randomUUID().toString() + ".jpg";
            String url = imageStore.saveImage(baos.toByteArray(), filename);
            
            game.setGameImageUrl(url);
            gameRepository.save(game);
            
            return url;

        } catch (IOException e) {
            // 로깅 추가 권장: log.error("Image processing error", e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "이미지 처리 중 서버 오류가 발생했습니다.");
        }
    }
}

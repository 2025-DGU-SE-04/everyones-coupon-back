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
// java.nio.file imports not used after ImageStore abstraction
import java.util.Base64;
import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
// UNAUTHORIZED not currently used in this service

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminTokenRepository adminTokenRepository;
    private final GameRepository gameRepository;
    private final CouponRepository couponRepository;
    private final com.everyones_coupon.storage.ImageStore imageStore;

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

        // decode base64
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(BAD_REQUEST, "이미지 데이터가 올바른 Base64가 아닙니다.");
        }

        // process image: read, resize if necessary, re-encode as JPEG with 75% quality
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
            BufferedImage inputImage = ImageIO.read(in);
            if (inputImage == null) {
                throw new ResponseStatusException(BAD_REQUEST, "이미지 데이터가 유효하지 않습니다.");
            }

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

            BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = outputImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(inputImage, 0, 0, targetWidth, targetHeight, null);
            g2d.dispose();

            // encode as JPEG with 75% quality
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.75f);
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                jpgWriter.setOutput(ios);
                IIOImage iioImage = new IIOImage(outputImage, null, null);
                jpgWriter.write(null, iioImage, jpgWriteParam);
                jpgWriter.dispose();
            }

            String filename = UUID.randomUUID().toString() + ".jpg";
            byte[] jpgBytes = baos.toByteArray();
            String url = imageStore.saveImage(jpgBytes, filename);
            game.setGameImageUrl(url);
            gameRepository.save(game);
            return url;
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "이미지 처리 중 오류 발생");
        }
    }
}

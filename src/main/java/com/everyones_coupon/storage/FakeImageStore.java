package com.everyones_coupon.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * 개발/테스트용 Fake 이미지 저장소 구현체입니다.
 * 이미지 바이트는 실제 저장하지 않고 버리며, UUID 기반의 그럴싸한 URL 문자열을 반환합니다.
 */
@Component
@Primary
public class FakeImageStore implements ImageStore {

    @Value("${app.image.fake-baseurl:/uploads}")
    private String baseUrl = "/uploads";

    @Override
    public String saveImage(byte[] imageBytes, String filename) throws IOException {
        // 실제로는 저장하지 않음; 그럴싸한 URL을 생성해서 반환
        if (filename == null || filename.isBlank()) {
            filename = UUID.randomUUID().toString() + ".jpg";
        } else {
            // 전달된 파일명에 경로가 포함된 경우 파일명만 추출
            int idx = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
            if (idx >= 0 && idx < filename.length() - 1) {
                filename = filename.substring(idx + 1);
            }
        }
        // 확장자 없으면 기본으로 .jpg를 붙임
        if (!filename.contains(".")) {
            filename = filename + ".jpg";
        }
        // 외부에서 접근 가능한 URL처럼 보이도록 포맷팅
        String uuidPart = UUID.randomUUID().toString();
        return String.format("%s/%s-%s", baseUrl.replaceAll("/+$", ""), uuidPart, filename);
    }
}

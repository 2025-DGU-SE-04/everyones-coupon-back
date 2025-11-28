package com.everyones_coupon.storage;

import java.io.IOException;

public interface ImageStore {
    /**
     * 이미지 바이트를 저장하고 접근 가능한 공개 URL을 반환합니다.
     * 구현체는 실제 저장(파일/클라우드) 또는 저장하지 않고 가짜 URL을 반환할 수 있습니다.
     *
     * @param imageBytes 이미지 바이트 배열
     * @param filename    원하는 파일명(옵션)
     * @return 접근 가능한 이미지 URL
     */
    String saveImage(byte[] imageBytes, String filename) throws IOException;
}

package com.everyones_coupon.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FakeImageStoreTest {

    @Test
    void saveImage_returns_plausible_url() throws Exception {
        FakeImageStore store = new FakeImageStore();
        String url = store.saveImage(new byte[]{1,2,3}, null);
        assertThat(url).contains("/uploads/");
        // UUID와 파일명을 포함하는 URL 형식인지 확인
        assertThat(url).contains("-");
        assertThat(url).endsWith(".jpg");
    }

    @Test
    void saveImage_respects_provided_filename() throws Exception {
        FakeImageStore store = new FakeImageStore();
        String url = store.saveImage(new byte[]{1,2,3}, "my-file.png");
        // 주어진 파일명이 URL에 포함되는지 확인
        assertThat(url).contains("my-file.png");
    }
}

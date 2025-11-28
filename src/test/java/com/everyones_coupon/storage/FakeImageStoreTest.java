package com.everyones_coupon.storage;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FakeImageStoreTest {

    @Test
    void saveImage_returns_plausible_url() throws Exception {
        FakeImageStore store = new FakeImageStore();
        String url = store.saveImage(new byte[]{1,2,3}, null);
        assertThat(url).contains("/uploads/");
        // uuid-like and filename (contains '-' and ends with .jpg produced by default)
        assertThat(url).contains("-");
        assertThat(url).endsWith(".jpg");
    }

    @Test
    void saveImage_respects_provided_filename() throws Exception {
        FakeImageStore store = new FakeImageStore();
        String url = store.saveImage(new byte[]{1,2,3}, "my-file.png");
        assertThat(url).contains("my-file.png");
    }
}

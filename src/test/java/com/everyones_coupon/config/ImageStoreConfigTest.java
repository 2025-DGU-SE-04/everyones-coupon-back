package com.everyones_coupon.config;

import com.everyones_coupon.storage.ImageStore;
import com.everyones_coupon.storage.LocalImageStore;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ImageStoreConfigTest {

    @Test
    void localImageStore_uses_baseHostAndPath() throws Exception {
        ImageStoreConfig cfg = new ImageStoreConfig();
        // pass baseHost and basePath explicitly
        ImageStore store = cfg.localImageStore("uploads_test", "http://localhost:8080", "/uploads");
        assertThat(store).isInstanceOf(LocalImageStore.class);
        LocalImageStore s = (LocalImageStore) store;
        assertThat(s.getBaseUrl()).isEqualTo("http://localhost:8080/uploads");
    }
}

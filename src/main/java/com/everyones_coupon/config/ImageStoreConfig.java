package com.everyones_coupon.config;

import com.everyones_coupon.storage.FakeImageStore;
import com.everyones_coupon.storage.ImageStore;
import com.everyones_coupon.storage.LocalImageStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ImageStoreConfig {

    @Bean
    @Primary
    public ImageStore localImageStore(@Value("${app.image.upload-dir:uploads}") String uploadDir,
                                      @Value("${app.image.base-url:/uploads}") String baseUrl) throws Exception {
        return new LocalImageStore(uploadDir, baseUrl);
    }

    @Bean
    public ImageStore fakeImageStore(@Value("${app.image.fake-baseurl:/uploads}") String baseUrl) {
        return new FakeImageStore(baseUrl);
    }
}

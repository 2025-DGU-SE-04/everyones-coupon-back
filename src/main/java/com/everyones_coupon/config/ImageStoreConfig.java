package com.everyones_coupon.config;

import com.everyones_coupon.storage.FakeImageStore;
import com.everyones_coupon.storage.ImageStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ImageStoreConfig {

    @Bean
    @Primary
    public ImageStore fakeImageStore(@Value("${app.image.fake-baseurl:/uploads}") String baseUrl) {
        return new FakeImageStore(baseUrl);
    }
}

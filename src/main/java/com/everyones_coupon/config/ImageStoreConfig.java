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
                                      @Value("${app.image.base-host:}") String baseHost,
                                      @Value("${app.image.base-path:/uploads}") String basePath) throws Exception {
        String finalBaseUrl;
        // Prefer explicit host + path if provided
        if (baseHost != null && !baseHost.isBlank()) {
            String host = baseHost.replaceAll("/+$", "");
            String path = (basePath == null || basePath.isBlank()) ? "/uploads" : basePath;
            if (!path.startsWith("/")) path = "/" + path;
            finalBaseUrl = host + path;
        } else {
            finalBaseUrl = basePath == null || basePath.isBlank() ? "/uploads" : basePath;
        }
        return new LocalImageStore(uploadDir, finalBaseUrl);
    }

    @Bean
    public ImageStore fakeImageStore(@Value("${app.image.fake-baseurl:/uploads}") String baseUrl) {
        return new FakeImageStore(baseUrl);
    }
}

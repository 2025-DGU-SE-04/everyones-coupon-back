package com.everyones_coupon.storage;

import java.io.IOException;

public interface ImageStore {
    /**
     * Save image bytes and return a public URL to access it
     */
    String saveImage(byte[] imageBytes, String filename) throws IOException;
}

package com.baap.service;

import org.springframework.stereotype.Service;

@Service
public class OcrService {

    public String extractText(byte[] data) {
        // OCR disabled for cloud deployment (Tesseract not available)
        return "";
    }
}

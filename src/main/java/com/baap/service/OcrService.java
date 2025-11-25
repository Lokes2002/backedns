package com.baap.service;

import org.springframework.stereotype.Service;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class OcrService {

    private static final String TESSERACT_PATH =
            "C:\\Program Files\\Tesseract-OCR\\tesseract.exe";

    public String extractText(byte[] data) throws Exception {

        // Create temp image file from bytes
        File temp = File.createTempFile("ocr-", ".png");
        Files.write(temp.toPath(), data);

        // Run tesseract
        ProcessBuilder pb = new ProcessBuilder(
                TESSERACT_PATH,
                temp.getAbsolutePath(),
                "stdout",
                "-l", "eng"
        );

        pb.redirectErrorStream(true);
        Process p = pb.start();

        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        p.waitFor();

        temp.delete(); // cleanup

        return out.trim();
    }
}

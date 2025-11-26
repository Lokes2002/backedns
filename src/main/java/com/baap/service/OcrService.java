package com.baap.service;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;

@Service
public class OcrService {

    private static final String OCR_URL = "https://pythd-3.onrender.com/ocr";

    public String extractText(byte[] data) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(OCR_URL))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> res = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            if (res.statusCode() != 200) return "";
            return res.body().replace("{\"text\":", "").replace("}", "").replace("\"", "").trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

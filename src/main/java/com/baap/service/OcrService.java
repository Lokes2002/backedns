package com.baap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class OcrService {

    // ⚠ BACKEND URL replace mat karo jab tak final server confirm na ho
    private static final String OCR_URL = "https://pythd-3.onrender.com/ocr";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public String extractText(byte[] data) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(OCR_URL))
                    .header("Content-Type", "application/octet-stream")
                    .timeout(Duration.ofSeconds(30))        // safety
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            // API down ya slow — fallback safe return
            if (res.statusCode() != 200 || res.body().isBlank()) {
                return "";
            }

            JsonNode node = mapper.readTree(res.body());

            if (node.has("text")) {
                return node.get("text").asText().trim();
            }

            return "";
        } catch (Exception e) {
            System.out.println("⚠ OCR Failed: " + e.getMessage());
            return "";
        }
    }
}

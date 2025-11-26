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
public class YoloService {

    // ⚠ Backend URL final deploy ke baad replace karna
    private static final String YOLO_URL = "https://pythd-3.onrender.com/detect";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public JsonNode detect(byte[] data) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(YOLO_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            // HTTP error = ignore YOLO
            if (res.statusCode() != 200) {
                System.out.println("⚠ YOLO HTTP error: " + res.statusCode());
                return null;
            }

            String body = res.body();
            if (body == null || body.isBlank()) return null;

            String trimmed = body.trim();

            // If HTML returned, ignore
            if (trimmed.startsWith("<")) return null;

            return mapper.readTree(trimmed);

        } catch (Exception e) {
            System.out.println("⚠ YOLO failed: " + e.getMessage());
            return null; // Never crash backend
        }
    }
}

package com.baap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OcrService {

    private static final String OCR_URL = "https://pythd-3.onrender.com/ocr";
    private final ObjectMapper mapper = new ObjectMapper();

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

            JsonNode node = mapper.readTree(res.body());
            if (node.has("text")) {
                return node.get("text").asText().trim();
            }

            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}

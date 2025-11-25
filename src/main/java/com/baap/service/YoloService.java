package com.baap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;

@Service
public class YoloService {

    private final String YOLO_URL = "http://localhost:8000/detect";
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode detect(byte[] data) throws Exception {

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(YOLO_URL))
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                .build();

        HttpResponse<String> res =
                HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());

        return mapper.readTree(res.body());
    }
}

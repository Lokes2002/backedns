package com.baap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class YoloService {

   private static final String YOLO_URL = "https://pythd-3.onrender.com/detect/";


    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode detect(byte[] data) {

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(YOLO_URL))
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> res = HttpClient.newHttpClient()
                    .send(req, HttpResponse.BodyHandlers.ofString());

            // Agar 200 nahi to YOLO ko ignore kar do (app crash mat karo)
            if (res.statusCode() != 200) {
                System.out.println("YOLO HTTP error: " + res.statusCode());
                System.out.println("Body: " + res.body());
                return null;
            }

            String body = res.body();
            if (body == null) return null;

            String trimmed = body.trim();

            // Agar body HTML lag rahi hai (starts with '<') to ignore
            if (trimmed.isEmpty() || trimmed.charAt(0) == '<') {
                System.out.println("YOLO returned non-JSON (probably HTML): ");
                System.out.println(trimmed.substring(0, Math.min(200, trimmed.length())));
                return null;
            }

            // Ab hi JSON parse karo
            return mapper.readTree(trimmed);

        } catch (Exception e) {
            e.printStackTrace();
            // Error aaye toh null return karo, taaki backend 500 na de
            return null;
        }
    }
}

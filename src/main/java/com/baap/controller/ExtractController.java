package com.baap.controller;

import com.baap.service.ImageAnalysisService;
import com.baap.service.OcrService;
import com.baap.service.PdfService;
import com.baap.service.SuggestionService;
import com.baap.service.YoloService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExtractController {

    private final PdfService pdfService;
    private final OcrService ocrService;
    private final ImageAnalysisService imageService;
    private final YoloService yoloService;
    private final SuggestionService suggestionService;

    @PostMapping("/extract")
    public ResponseEntity<?> extract(@RequestParam("file") MultipartFile file) {
        try {
            // Read file once
            byte[] data = file.getBytes();
            String name = file.getOriginalFilename().toLowerCase();

            String text = "";
            Map<String, Object> img = null;
            JsonNode yolo = null;

            // --------------------- PDF ---------------------
            if (name.endsWith(".pdf")) {

                File tempPdf = File.createTempFile("pdf-", ".pdf");
                Files.write(tempPdf.toPath(), data);

                text = pdfService.extractText(tempPdf);

                tempPdf.delete();
            }

            // --------------------- IMAGE ---------------------
            else {

                text = ocrService.extractText(data);

                img = imageService.analyze(data);

                yolo = yoloService.detect(data);
            }

            // --------------------- PREPARE YOLO STRING FOR SUGGESTIONS ---------------------
            String yoloString = "";

            if (yolo != null && yolo.has("objects")) {
                StringBuilder sb = new StringBuilder();

                for (JsonNode obj : yolo.get("objects")) {
                    if (obj.has("label")) {
                        sb.append(obj.get("label").asText()).append(" ");
                    }
                }

                yoloString = sb.toString().trim();
            }

            // --------------------- GENERATE SUGGESTIONS ---------------------
            var suggestions = suggestionService.generate(text, img, yoloString);

            // --------------------- FINAL OUTPUT ---------------------
            Map<String, Object> out = new HashMap<>();
            out.put("text", text);
            out.put("imageInsights", img);
            out.put("objectsDetected", (yolo != null ? yolo.get("objects") : null));
            out.put("suggestions", suggestions);

            return ResponseEntity.ok(out);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.toString());
            return ResponseEntity.status(500).body(err);
        }
    }
}

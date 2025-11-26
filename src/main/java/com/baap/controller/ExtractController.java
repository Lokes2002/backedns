package com.baap.controller;

import com.baap.service.ImageAnalysisService;
import com.baap.service.OcrService;
import com.baap.service.PdfService;
import com.baap.service.SuggestionService;
import com.baap.service.YoloService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ------------------ MAIN API ------------------
    @PostMapping(value = "/extract", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> extract(@RequestBody byte[] data) {
        try {
            String text = "";
            Map<String, Object> imgInsights = null;
            List<Map<String, Object>> yoloResults = new ArrayList<>();

            // Detect PDF or IMAGE
            boolean isPdf = isPdf(data);

            // ---------------- PDF ----------------
            if (isPdf) {
                File tempPdf = File.createTempFile("pdf-", ".pdf");
                Files.write(tempPdf.toPath(), data);
                text = pdfService.extractText(tempPdf);
                tempPdf.delete();
            }

            // ---------------- IMAGE ----------------
            else {
                text = ocrService.extractText(data);
                imgInsights = imageService.analyze(data);

                JsonNode yoloJson = yoloService.detect(data);
                if (yoloJson != null && yoloJson.has("objects")) {
                    for (JsonNode obj : yoloJson.get("objects")) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("label", obj.get("label").asText());
                        m.put("confidence", obj.get("confidence").asDouble());
                        m.put("text", obj.has("text") ? obj.get("text").asText() : "");
                        yoloResults.add(m);
                    }
                }
            }

            // Prepare label string for suggestions
            StringBuilder labels = new StringBuilder();
            for (Map<String, Object> r : yoloResults) {
                labels.append(r.get("label")).append(" ");
            }

            // Suggestions
            var suggestions = suggestionService.generate(text, imgInsights, labels.toString().trim());

            // ------------------- FINAL OUTPUT -------------------
            Map<String, Object> out = new HashMap<>();
            out.put("results", yoloResults);         // objects + text
            out.put("imageInsights", imgInsights);   // width/height/brightness/blur etc.
            out.put("text", text);                   // extracted text
            out.put("suggestions", suggestions);     // AI tips

            return ResponseEntity.ok(out);

        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }


    // Helper — detect PDF from bytes
    private boolean isPdf(byte[] data) {
        return data.length > 4 &&
                data[0] == 0x25 && data[1] == 0x50 &&
                data[2] == 0x44 && data[3] == 0x46; // %PDF
    }
}

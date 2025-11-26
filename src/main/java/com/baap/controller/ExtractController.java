package com.baap.controller;

import com.baap.service.OcrService;
import com.baap.service.PdfService;
import com.baap.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExtractController {

    private final PdfService pdfService;
    private final OcrService ocrService;
    private final SuggestionService suggestionService; // Only needed now

    @PostMapping(value = "/extract", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<?> extract(@RequestBody byte[] data) {
        try {
            String text = "";

            boolean isPdf = isPdf(data);

            // ----- If PDF -----
            if (isPdf) {
                File tempPdf = File.createTempFile("pdf-", ".pdf");
                Files.write(tempPdf.toPath(), data);
                text = pdfService.extractText(tempPdf);
                tempPdf.delete();
            }

            // ----- If IMAGE -----
            else {
                text = ocrService.extractText(data);
            }

            // AI Suggestions based on text
            var suggestions = suggestionService.generate(text, null, null);

            // Response
            Map<String, Object> out = new HashMap<>();
            out.put("text", text);
            out.put("suggestions", suggestions);

            return ResponseEntity.ok(out);

        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            return ResponseEntity.status(500).body(err);
        }
    }

    private boolean isPdf(byte[] data) {
        return data.length > 4 &&
               data[0] == 0x25 && data[1] == 0x50 &&
               data[2] == 0x44 && data[3] == 0x46; // %PDF
    }
}

package com.baap.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

@Service
public class PdfService {

    private final OcrService ocrService;

    public PdfService(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    public String extractText(File file) throws Exception {
        StringBuilder out = new StringBuilder();

        try (PDDocument doc = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            int pages = doc.getNumberOfPages();

            for (int i = 0; i < pages; i++) {
                try {
                    // Fast DPI for speed | Auto boosts if quality is low
                    int dpi = (pages > 5 ? 170 : 200);

                    BufferedImage img = renderer.renderImageWithDPI(i, dpi);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", baos);
                    byte[] imageBytes = baos.toByteArray();

                    // OCR for this page
                    String text = ocrService.extractText(imageBytes);

                    if (!text.isBlank()) {
                        out.append(text).append("\n");
                    }

                    img.flush();
                } catch (Exception e) {
                    System.out.println("⚠ OCR failed on page " + i + ": " + e.getMessage());
                }
            }
        }

        return out.toString().trim();
    }
}

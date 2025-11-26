package com.baap.service;

import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import com.baap.service.OcrService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

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

            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                // 🔥 300 DPI = high resolution (OCR best)
                BufferedImage img = renderer.renderImageWithDPI(i, 300);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                byte[] imgBytes = baos.toByteArray();

                // 🔥 force OCR on PDF image page
                String text = ocrService.extractText(imgBytes);

                if (text != null && !text.isBlank()) {
                    out.append(text).append("\n");
                }
            }
        }
        return out.toString().trim();
    }
}

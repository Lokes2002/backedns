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

            for (int i = 0; i < doc.getNumberOfPages(); i++) {

                // Render PDF page → BufferedImage
                BufferedImage img = renderer.renderImageWithDPI(i, 200);

                // Convert BufferedImage → byte[]
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(img, "png", baos);
                byte[] imageBytes = baos.toByteArray();

                // Run OCR on byte[]
                String text = ocrService.extractText(imageBytes);

                out.append(text).append("\n");
            }
        }

        return out.toString().trim();
    }
}

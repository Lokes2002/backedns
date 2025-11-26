package com.baap.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageAnalysisService {

    public Map<String, Object> analyze(byte[] data) throws Exception {
        Map<String, Object> out = new HashMap<>();

        BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        if (img == null) {
            return out; // If PDF then skip
        }

        int w = img.getWidth();
        int h = img.getHeight();

        out.put("width", w);
        out.put("height", h);
        out.put("brightness", normalize(calcBrightness(img)));
        out.put("blurScore", calcBlur(img));

        return out;
    }

    // ----------------- BRIGHTNESS -----------------
    private double calcBrightness(BufferedImage img) {
        long sum = 0;
        int count = 0;

        // Skipping pixels for speed
        for (int y = 0; y < img.getHeight(); y += 5) {
            for (int x = 0; x < img.getWidth(); x += 5) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 255;
                int g = (rgb >> 8) & 255;
                int b = (rgb) & 255;
                sum += (r + g + b) / 3;
                count++;
            }
        }
        return sum / (double) count; // 0–255
    }

    // Convert 0–255 brightness → 0–1 float (for frontend smooth display)
    private double normalize(double brightness) {
        return Math.round((brightness / 255.0) * 1000) / 1000.0;
    }

    // ----------------- BLUR DETECTION -----------------
    private double calcBlur(BufferedImage img) {
        long sumDiff = 0;
        int count = 0;

        for (int y = 1; y < img.getHeight() - 1; y += 3) {
            for (int x = 1; x < img.getWidth() - 1; x += 3) {
                int c = img.getRGB(x, y);

                int r = (c >> 16) & 255;
                int g = (c >> 8) & 255;
                int b = (c) & 255;
                int gray = (r + g + b) / 3;

                int c2 = img.getRGB(x + 1, y + 1);
                int r2 = (c2 >> 16) & 255;
                int g2 = (c2 >> 8) & 255;
                int b2 = (c2) & 255;
                int gray2 = (r2 + g2 + b2) / 3;

                sumDiff += Math.abs(gray - gray2);
                count++;
            }
        }

        if (count == 0) return 0;

        // Higher diff = sharper image
        double sharpness = sumDiff / (double) count;
        // Convert to 0–1 where 0 = blurry and 1 = sharp
        sharpness = Math.min(1.0, sharpness / 100.0);

        return Math.round(sharpness * 1000) / 1000.0;
    }
}

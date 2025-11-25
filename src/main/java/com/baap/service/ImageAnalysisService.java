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
            return out; // PDF case → no image analysis
        }

        int w = img.getWidth();
        int h = img.getHeight();

        out.put("width", w);
        out.put("height", h);
        out.put("brightness", calcBrightness(img));
        out.put("blurScore", calcBlur(img));

        return out;
    }

    private double calcBrightness(BufferedImage img) {
        long sum = 0;
        int count = 0;
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
        return sum / (double) count;
    }

    private double calcBlur(BufferedImage img) {
        return 100;
    }
}

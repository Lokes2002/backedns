package com.baap.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SuggestionService {

    private static final List<String> GENERAL_SUGGESTIONS = Arrays.asList(
        "Add a clear call-to-action to encourage engagement",
        "Use 3-5 relevant hashtags to increase discoverability", 
        "Include emojis to make your content more visually appealing",
        "Ask an open-ended question to spark conversation",
        "Post during peak hours (9-11 AM or 7-9 PM) for better reach",
        "Tag relevant accounts or locations to increase visibility",
        "Use trending keywords related to your content theme",
        "Create a sense of urgency with time-sensitive language",
        "Share personal stories to build emotional connection",
        "Use carousel posts to show multiple aspects of your content"
    );

    public List<String> generate(String text, Map<String, Object> img, String yolo) {
        List<String> suggestions = new ArrayList<>();
        
        // Text-based analysis
        analyzeText(text, suggestions);
        
        // Image insights analysis
        analyzeImage(img, suggestions);
        
        // Object detection analysis
        analyzeObjects(yolo, suggestions);
        
        // Add general suggestions if we don't have enough specific ones
        addGeneralSuggestions(suggestions);
        
        // Remove duplicates and limit to 5 best suggestions
        return getUniqueSuggestions(suggestions);
    }

    private void analyzeText(String text, List<String> suggestions) {
        if (text == null || text.trim().isEmpty()) {
            suggestions.add("Add compelling text to describe your content and provide context");
            suggestions.add("Include a clear description of what your post is about");
            return;
        }

        String cleanText = text.trim();
        int textLength = cleanText.length();
        int wordCount = cleanText.split("\\s+").length;

        // Length-based suggestions
        if (textLength < 50) {
            suggestions.add("Expand your caption to provide more value and context to viewers");
            suggestions.add("Add more descriptive text to engage your audience better");
        } else if (textLength > 500) {
            suggestions.add("Consider making your post more concise for better readability on mobile");
        }

        // Content-based suggestions
        if (!containsQuestion(cleanText)) {
            suggestions.add("Ask a question to encourage comments and discussion");
        }
        
        if (!containsHashtags(cleanText)) {
            suggestions.add("Add relevant hashtags to increase your post's discoverability");
        } else {
            int hashtagCount = countHashtags(cleanText);
            if (hashtagCount > 10) {
                suggestions.add("Reduce hashtags to 3-5 most relevant ones for better engagement");
            }
        }

        if (!containsCallToAction(cleanText)) {
            suggestions.add("Include a clear call-to-action (e.g., 'What do you think?', 'Share your experience!')");
        }

        // Sentiment-based (simple)
        if (containsNegativeWords(cleanText)) {
            suggestions.add("Consider using more positive language to create better engagement");
        }
    }

    private void analyzeImage(Map<String, Object> img, List<String> suggestions) {
        if (img == null || img.isEmpty()) {
            return;
        }

        try {
            // Brightness analysis
            if (img.containsKey("brightness")) {
                double brightness = ((Number) img.get("brightness")).doubleValue();
                if (brightness < 0.3) {
                    suggestions.add("Increase image brightness for better visual appeal and engagement");
                } else if (brightness > 0.8) {
                    suggestions.add("Reduce image brightness to avoid overexposure and improve clarity");
                }
            }

            // Blur analysis
            if (img.containsKey("blurScore")) {
                double blurScore = ((Number) img.get("blurScore")).doubleValue();
                if (blurScore < 0.4) {
                    suggestions.add("Use sharper, high-quality images for better professional appearance");
                }
            }

            // Dimension analysis
            if (img.containsKey("width") && img.containsKey("height")) {
                int width = ((Number) img.get("width")).intValue();
                int height = ((Number) img.get("height")).intValue();
                double aspectRatio = (double) width / height;

                if (aspectRatio > 1.5) {
                    suggestions.add("Landscape images work well for Facebook and LinkedIn feeds");
                } else if (aspectRatio < 0.8) {
                    suggestions.add("Portrait images are ideal for Instagram Stories and Pinterest");
                } else {
                    suggestions.add("Square images perform well on Instagram feed and Facebook");
                }

                if (width < 1080) {
                    suggestions.add("Use higher resolution images (min 1080px width) for better quality");
                }
            }

            // Dominant colors
            if (img.containsKey("dominantColors")) {
                suggestions.add("Use your image's color palette to choose complementary text colors");
            }

        } catch (Exception e) {
            // Silent fail - don't add image suggestions if analysis fails
        }
    }

    private void analyzeObjects(String yolo, List<String> suggestions) {
        if (yolo == null || yolo.isEmpty()) {
            return;
        }

        try {
            // Simple string-based object detection
            String lowerYolo = yolo.toLowerCase();

            if (lowerYolo.contains("person")) {
                suggestions.add("People content performs well - focus on emotions and storytelling");
                suggestions.add("Consider showing faces to create personal connection with audience");
            }
            
            if (lowerYolo.contains("food") || lowerYolo.contains("pizza") || lowerYolo.contains("burger")) {
                suggestions.add("Food content is highly engaging - describe taste and experience");
                suggestions.add("Use natural lighting for food photos to enhance appeal");
            }
            
            if (lowerYolo.contains("dog") || lowerYolo.contains("cat") || lowerYolo.contains("animal")) {
                suggestions.add("Pet content gets high engagement - show their personality");
                suggestions.add("Use pet-related hashtags like #petstagram #dogsofinstagram");
            }
            
            if (lowerYolo.contains("car") || lowerYolo.contains("bicycle") || lowerYolo.contains("motorcycle")) {
                suggestions.add("Vehicle content appeals to niche audiences - highlight key features");
            }
            
            if (lowerYolo.contains("book") || lowerYolo.contains("laptop") || lowerYolo.contains("phone")) {
                suggestions.add("Lifestyle content works well - show how you use these items daily");
            }

            // Multiple objects detected
            if (countObjects(lowerYolo) > 3) {
                suggestions.add("Simplify your composition by focusing on one main subject");
            }

        } catch (Exception e) {
            // Silent fail - don't add object suggestions if analysis fails
        }
    }

    private void addGeneralSuggestions(List<String> suggestions) {
        // Add 1-2 general suggestions if we have less than 3 specific ones
        if (suggestions.size() < 3) {
            Random random = new Random();
            int needed = 3 - suggestions.size();
            
            List<String> available = new ArrayList<>(GENERAL_SUGGESTIONS);
            available.removeAll(suggestions); // Avoid duplicates
            
            for (int i = 0; i < Math.min(needed, available.size()); i++) {
                suggestions.add(available.get(random.nextInt(available.size())));
            }
        }
    }

    private List<String> getUniqueSuggestions(List<String> suggestions) {
        // Remove duplicates and return max 5 suggestions
        Set<String> unique = new LinkedHashSet<>(suggestions);
        return new ArrayList<>(unique).subList(0, Math.min(unique.size(), 5));
    }

    // Helper methods
    private boolean containsQuestion(String text) {
        return text.contains("?") || 
               text.toLowerCase().matches(".*\\b(what|why|how|when|who|which|where)\\b.*");
    }

    private boolean containsHashtags(String text) {
        return text.contains("#");
    }

    private int countHashtags(String text) {
        return text.split("#").length - 1;
    }

    private boolean containsCallToAction(String text) {
        String lower = text.toLowerCase();
        return lower.matches(".*\\b(comment|share|like|tag|click|visit|follow|subscribe|join|tell|what do you think)\\b.*");
    }

    private boolean containsNegativeWords(String text) {
        String lower = text.toLowerCase();
        return lower.matches(".*\\b(not|never|can't|won't|don't|bad|worst|terrible|awful|hate)\\b.*");
    }

    private int countObjects(String yoloText) {
        // Simple count by looking for "label" occurrences
        return yoloText.split("label").length - 1;
    }
}
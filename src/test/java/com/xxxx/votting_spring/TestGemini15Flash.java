package com.xxxx.votting_spring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestGemini15Flash {

    @Test
    public void testGemini15Flash() {
        String apiKey = "AIzaSyCbllOwavT_l9G7VJK-L7R0wrH4NT_tFGg";
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

        RestTemplate restTemplate = new RestTemplate();

        try {
            String fullUrl = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Test 1: Safe Content
            System.out.println("\n=== Test 1: Safe Content ===");
            testContent(restTemplate, fullUrl, headers, "What is your favorite color?");

            // Wait a bit between requests
            Thread.sleep(2000);

            // Test 2: Vietnamese Profanity
            System.out.println("\n=== Test 2: Vietnamese Profanity ===");
            testContent(restTemplate, fullUrl, headers, "Tôi ghét mày, địt mẹ mày");

            // Wait a bit between requests
            Thread.sleep(2000);

            // Test 3: Hate Speech
            System.out.println("\n=== Test 3: Hate Speech ===");
            testContent(restTemplate, fullUrl, headers, "I hate all people from that country");

        } catch (Exception e) {
            System.err.println("❌ Test Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testContent(RestTemplate restTemplate, String fullUrl, HttpHeaders headers, String text) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text",
                    "Analyze the following text for hate speech, violence, self-harm, sexual content, severe negativity, or profanity/vulgarity in any language (especially Vietnamese). "
                            +
                            "If the content is safe, reply with exactly 'SAFE'. " +
                            "If it violates safety standards or contains vulgar language, reply with exactly 'UNSAFE'. "
                            +
                            "Text: \"" + text + "\"");

            Map<String, Object> contentObj = new HashMap<>();
            contentObj.put("parts", Collections.singletonList(part));

            requestBody.put("contents", Collections.singletonList(contentObj));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("Input: " + text);

            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");

                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode firstCandidate = candidates.get(0);
                    JsonNode content = firstCandidate.path("content");
                    JsonNode parts = content.path("parts");

                    if (parts.isArray() && parts.size() > 0) {
                        String responseText = parts.get(0).path("text").asText().trim();
                        System.out.println("Gemini Response: " + responseText);

                        if (responseText.toUpperCase().contains("UNSAFE")) {
                            System.out.println("✅ CORRECTLY DETECTED AS UNSAFE");
                        } else if (responseText.toUpperCase().contains("SAFE")) {
                            System.out.println("✅ CORRECTLY DETECTED AS SAFE");
                        } else {
                            System.out.println("⚠️ AMBIGUOUS RESPONSE");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Request Failed: " + e.getMessage());
        }
    }
}

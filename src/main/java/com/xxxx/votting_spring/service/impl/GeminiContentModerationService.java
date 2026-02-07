package com.xxxx.votting_spring.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.votting_spring.exception.ContentViolationException;
import com.xxxx.votting_spring.service.ContentModerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GeminiContentModerationService implements ContentModerationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Override
    public void validateContent(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        try {
            // Gemini URL with Key
            String fullUrl = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Gemini Request Structure
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

            System.out.println("Sending text to Gemini for moderation: " + text);

            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            System.out.println("Gemini Response Body: " + response.getBody());

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
                        System.out.println("Gemini Parsed Content: " + responseText);

                        if (responseText.toUpperCase().contains("UNSAFE")) {
                            throw new ContentViolationException(
                                    "Content contains inappropriate material and violates safety standards.");
                        }
                    }
                }
            }
        } catch (ContentViolationException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("⚠️ Gemini Moderation Failed: " + e.getMessage());

            // If API fails, we have two choices:
            // 1. Fail safe: Allow content (current behavior)
            // 2. Fail secure: Block content
            // Currently using fail-safe approach - content is allowed if moderation API
            // fails
            // To fail secure, uncomment the line below:
            // throw new ContentViolationException("Content moderation service unavailable.
            // Please try again later.");

            System.err.println("⚠️ Content was ALLOWED despite moderation failure (fail-safe mode)");
        }
    }
}

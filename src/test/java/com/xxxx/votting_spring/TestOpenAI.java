package com.xxxx.votting_spring;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestOpenAI {

    @Test
    public void testConnection() {
        String apiKey = "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";
        String apiUrl = "https://api.openai.com/v1/chat/completions";
        String model = "gpt-3.5-turbo";

        RestTemplate restTemplate = new RestTemplate();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "Say hello");

            requestBody.put("messages", Collections.singletonList(userMessage));
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("Sending request...");
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);

            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Request Failed: " + e.getMessage());
        }
    }
}

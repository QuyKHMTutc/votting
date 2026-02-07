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

public class TestGemini {

    @Test
    public void testConnection() {
        String apiKey = "AIzaSyBYZYy5HDjadIr74W4hiq1yjICrzJGbQWc";
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

        RestTemplate restTemplate = new RestTemplate();

        try {
            String fullUrl = apiUrl + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", "Say hello to Antigravity");

            Map<String, Object> contentObj = new HashMap<>();
            contentObj.put("parts", Collections.singletonList(part));

            requestBody.put("contents", Collections.singletonList(contentObj));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("Sending request to Gemini...");
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, entity, String.class);

            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Request Failed: " + e.getMessage());
        }
    }
}

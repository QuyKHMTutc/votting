package com.xxxx.votting_spring;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ListGeminiModels {

    @Test
    public void listModels() {
        String apiKey = "AIzaSyBYZYy5HDjadIr74W4hiq1yjICrzJGbQWc";
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        try {
            System.out.println("Listing models...");
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Request Failed: " + e.getMessage());
        }
    }
}

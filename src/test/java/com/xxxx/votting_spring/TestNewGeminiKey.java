package com.xxxx.votting_spring;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class TestNewGeminiKey {

    @Test
    public void testNewApiKey() {
        String apiKey = "AIzaSyCbllOwavT_l9G7VJK-L7R0wrH4NT_tFGg";

        // Test 1: List available models (doesn't consume quota)
        String listModelsUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        try {
            System.out.println("=== Testing API Key Validity ===");
            System.out.println("API Key: " + apiKey);
            System.out.println("\n1. Listing available models...");

            ResponseEntity<String> response = restTemplate.getForEntity(listModelsUrl, String.class);

            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("\n✅ API Key is VALID!");
                System.out.println("You can use this key for content moderation.");
            }
        } catch (Exception e) {
            System.err.println("\n❌ API Key Test FAILED!");
            System.err.println("Error: " + e.getMessage());

            if (e.getMessage().contains("API_KEY_INVALID")) {
                System.err.println("\n🔧 How to fix:");
                System.err.println("1. Go to https://aistudio.google.com/apikey");
                System.err.println("2. Create a NEW API key");
                System.err.println("3. Make sure Gemini API is enabled for your project");
                System.err.println("4. Wait a few minutes for the key to activate");
            }

            e.printStackTrace();
        }
    }
}

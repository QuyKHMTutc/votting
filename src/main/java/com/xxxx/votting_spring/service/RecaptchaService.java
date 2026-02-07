package com.xxxx.votting_spring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplate restTemplate;

    public RecaptchaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean verifyRecaptcha(String recaptchaToken) {
        if (recaptchaToken == null || recaptchaToken.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret);
        requestMap.add("response", recaptchaToken);

        try {
            Map response = restTemplate.postForObject(GOOGLE_RECAPTCHA_VERIFY_URL, requestMap, Map.class);
            if (response == null) {
                return false;
            }
            return Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

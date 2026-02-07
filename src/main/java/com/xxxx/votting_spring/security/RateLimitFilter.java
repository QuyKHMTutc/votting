package com.xxxx.votting_spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxxx.votting_spring.dto.response.ApiResponse;
import com.xxxx.votting_spring.service.RateLimitingService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitingService rateLimitingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        Bucket bucket;
        if (uri.startsWith("/api/auth/register") || uri.startsWith("/api/auth/login")) {
            bucket = rateLimitingService.resolveBucket(ip);
        } else if (uri.startsWith("/api/")) {
            bucket = rateLimitingService.resolveGeneralBucket(ip);
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            // Fixed: removed status code argument
            ApiResponse<Void> apiResponse = ApiResponse.error("Too many requests. Please try again later.");
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        }
    }
}

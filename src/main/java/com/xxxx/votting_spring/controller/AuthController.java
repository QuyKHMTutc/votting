package com.xxxx.votting_spring.controller;

import com.xxxx.votting_spring.dto.response.ApiResponse;
import com.xxxx.votting_spring.dto.response.AuthResponse;
import com.xxxx.votting_spring.dto.request.LoginRequest;
import com.xxxx.votting_spring.dto.request.RegisterRequest;
import com.xxxx.votting_spring.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .ok(ApiResponse.success(response, "User registered successfully. Please verify your email."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyAccount(@RequestParam String email, @RequestParam String code) {
        authService.verifyAccount(email, code);
        return ResponseEntity.ok(ApiResponse.success(null, "Account verified successfully. You can now login."));
    }

    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse<String>> resendVerificationCode(@RequestParam String email) {
        authService.resendVerificationCode(email);
        return ResponseEntity.ok(ApiResponse.success(null, "Verification code resent successfully."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset code sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody com.xxxx.votting_spring.dto.request.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity
                .ok(ApiResponse.success(null, "Password reset successfully. Please login with your new password."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser() {
        AuthResponse response = authService.getMyself();
        return ResponseEntity.ok(ApiResponse.success(response, "Current user fetched"));
    }
}

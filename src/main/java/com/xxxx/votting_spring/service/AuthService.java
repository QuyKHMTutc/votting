package com.xxxx.votting_spring.service;

import com.xxxx.votting_spring.dto.response.AuthResponse;
import com.xxxx.votting_spring.dto.request.LoginRequest;
import com.xxxx.votting_spring.dto.request.RegisterRequest;
import com.xxxx.votting_spring.entity.User;
import com.xxxx.votting_spring.repository.UserRepository;
import com.xxxx.votting_spring.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RecaptchaService recaptchaService;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        // Validation: Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Verify reCAPTCHA
        if (request.getRecaptchaToken() != null && !request.getRecaptchaToken().isEmpty()) {
            if (!recaptchaService.verifyRecaptcha(request.getRecaptchaToken())) {
                throw new RuntimeException("Recaptcha verification failed. Please try again.");
            }
        } else {
            // throw new RuntimeException("Please complete the Captcha.");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getEmail()); // Set username as email
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(User.Role.USER); // Explicitly set role to USER

        // Generate verification code
        String code = String.valueOf((int) (Math.random() * 900000) + 100000); // 6 digits
        user.setVerificationCode(code);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        user.setEnabled(false); // Not enabled yet

        User savedUser = userRepository.save(user); // Save to generate ID

        // Send Email
        try {
            emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), code);
        } catch (Exception e) {
            // Rollback user creation if email fails
            userRepository.delete(savedUser);
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }

        // We do NOT generate token yet. User must verify first.
        return new AuthResponse(null, savedUser.getId(), savedUser.getUsername(),
                savedUser.getEmail(), savedUser.getFullName(), savedUser.getRole().name(),
                "Registration successful. Please check your email (" + savedUser.getEmail() + ") to verify account.");
    }

    public void verifyAccount(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            return; // Already verified
        }

        if (code.equals(user.getVerificationCode())) {
            if (user.getOtpExpiration() != null && LocalDateTime.now().isAfter(user.getOtpExpiration())) {
                throw new RuntimeException("Verification code has expired");
            }
            user.setEnabled(true);
            user.setVerificationCode(null);
            user.setOtpExpiration(null);
            userRepository.save(user);
        } else {
            throw new RuntimeException("Invalid verification code");
        }
    }

    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new RuntimeException("Account already verified");
        }

        // Generate new code
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setVerificationCode(code);
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), code);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate reset code
        String code = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setVerificationCode(code); // Reuse verification code field
        user.setOtpExpiration(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), code);
    }

    public void resetPassword(com.xxxx.votting_spring.dto.request.ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }

        if (user.getOtpExpiration() != null && LocalDateTime.now().isAfter(user.getOtpExpiration())) {
            throw new RuntimeException("Verification code has expired");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setVerificationCode(null); // Clear code
        user.setOtpExpiration(null);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate user using EMAIL
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // userDetails.isEnabled() is checked by
                                                                               // authenticationManager

        String token = jwtUtil.generateToken(userDetails);

        // Get user info by EMAIL
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(token, user.getId(), user.getUsername(),
                user.getEmail(), user.getFullName(), user.getRole().name(), "Login successful");
    }

    public AuthResponse getMyself() {
        Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByEmail(username) // username is email here
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new AuthResponse(null, user.getId(), user.getUsername(),
                user.getEmail(), user.getFullName(), user.getRole().name(), "User details fetched");
    }
}

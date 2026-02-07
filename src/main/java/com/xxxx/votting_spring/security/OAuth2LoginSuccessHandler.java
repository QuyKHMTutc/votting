package com.xxxx.votting_spring.security;

import com.xxxx.votting_spring.entity.User;
import com.xxxx.votting_spring.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = token.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            // Generate a username from email or random
            String username = email.split("@")[0];
            if (userRepository.existsByUsername(username)) {
                username += UUID.randomUUID().toString().substring(0, 5);
            }
            newUser.setUsername(username);
            // Set dummy password for OAuth users (they won't use it)
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setFullName(name);
            newUser.setEnabled(true);
            newUser.setRole(User.Role.USER); // Explicitly set role
            return userRepository.save(newUser);
        });

        // Generate JWT token using UserDetails adapter
        // We create a temporary UserDetails object since we have the User entity
        // IMPORTANT: We must use EMAIL as the username here because
        // CustomUserDetailsService expects email
        org.springframework.security.core.userdetails.User userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), // Use EMAIL here!
                user.getPassword(),
                Collections.emptyList());

        String jwt = jwtUtil.generateToken(userDetails);

        // Redirect to frontend with token
        // Assuming frontend runs on localhost:5173
        response.sendRedirect("http://localhost:5173/oauth2/redirect?token=" + jwt);
    }
}

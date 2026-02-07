package com.xxxx.votting_spring.config;

import com.xxxx.votting_spring.entity.User;
import com.xxxx.votting_spring.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User admin = userRepository.findByUsername("admin").orElse(null);

            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setFullName("System Admin");
                admin.setEnabled(true);
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);
                System.out.println("Admin account created: admin / admin");
            } else {
                // Ensure role is ADMIN even if user exists
                if (admin.getRole() != User.Role.ADMIN) {
                    admin.setRole(User.Role.ADMIN);
                    userRepository.save(admin);
                    System.out.println("Admin account role updated to ADMIN");
                }
            }
        };
    }
}

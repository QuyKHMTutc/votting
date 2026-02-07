package com.xxxx.votting_spring.controller;

import com.xxxx.votting_spring.dto.response.ApiResponse;
import com.xxxx.votting_spring.repository.PollRepository;
import com.xxxx.votting_spring.repository.UserRepository;
import com.xxxx.votting_spring.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private VoteRepository voteRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalPolls", pollRepository.count());
        stats.put("totalVotes", voteRepository.count());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @DeleteMapping("/polls/{id}")
    public ResponseEntity<?> deletePoll(@PathVariable Long id) {
        if (!pollRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pollRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Poll deleted successfully by admin"));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userRepository.findAll()));
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            // Prevent toggling own status or other admins (optional, but good safety)
            // For now, let's allow locking other admins if needed, but maybe not self?
            // Let's just implement toggle.
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success(user, "User status updated successfully"));
        }).orElse(ResponseEntity.notFound().build());
    }
}

package com.xxxx.votting_spring.controller;

import com.xxxx.votting_spring.dto.response.ApiResponse;
import com.xxxx.votting_spring.dto.request.VoteRequest;
import com.xxxx.votting_spring.entity.Vote;
import com.xxxx.votting_spring.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/polls")
@CrossOrigin
public class VoteController {

    @Autowired
    private VoteService voteService;

    @PostMapping("/{pollId}/vote")
    public ResponseEntity<ApiResponse<Map<String, Object>>> vote(@PathVariable Long pollId,
            @Valid @RequestBody VoteRequest request,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String username = authentication != null ? authentication.getName() : null;
        String ipAddress = httpRequest.getRemoteAddr();
        Vote vote = voteService.submitVote(pollId, request, username, ipAddress);
        return ResponseEntity.ok(ApiResponse.success(Map.of("voteId", vote.getId()), "Vote submitted successfully"));
    }

    @GetMapping("/{pollId}/my-vote")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyVote(@PathVariable Long pollId,
            Authentication authentication,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String username = authentication != null ? authentication.getName() : null;
        String ipAddress = httpRequest.getRemoteAddr();
        boolean hasVoted = voteService.hasUserVoted(pollId, username, ipAddress);

        Map<String, Object> response = new HashMap<>();
        response.put("hasVoted", hasVoted);

        if (hasVoted) {
            // Only try to get user vote if logged in, active IP retrieval for anonymous
            // users is not stored in linkable way easily
            // For now, only return detailed vote if logged in
            if (username != null) {
                Optional<Vote> vote = voteService.getUserVote(pollId, username);
                vote.ifPresent(v -> response.put("optionId", v.getOption().getId()));
            }
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

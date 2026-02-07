package com.xxxx.votting_spring.controller;

import com.xxxx.votting_spring.dto.response.ApiResponse;
import com.xxxx.votting_spring.dto.request.PollRequest;
import com.xxxx.votting_spring.dto.response.PollResponse;
import com.xxxx.votting_spring.service.PollService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
@CrossOrigin
public class PollController {

    @Autowired
    private PollService pollService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PollResponse>>> getAllPolls() {
        return ResponseEntity.ok(ApiResponse.success(pollService.getAllPolls()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PollResponse>>> getActivePolls() {
        return ResponseEntity.ok(ApiResponse.success(pollService.getActivePolls()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PollResponse>> getPollById(@PathVariable Long id) {
        PollResponse poll = pollService.getPollById(id);
        return ResponseEntity.ok(ApiResponse.success(poll));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PollResponse>> createPoll(@Valid @RequestBody PollRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        PollResponse poll = pollService.createPoll(request, username);
        return ResponseEntity.ok(ApiResponse.success(poll, "Poll created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PollResponse>> updatePoll(@PathVariable Long id,
            @Valid @RequestBody PollRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        PollResponse poll = pollService.updatePoll(id, request, username);
        return ResponseEntity.ok(ApiResponse.success(poll, "Poll updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePoll(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        pollService.deletePoll(id, username);
        return ResponseEntity.ok(ApiResponse.success("Poll deleted successfully"));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<PollResponse>> closePoll(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        PollResponse poll = pollService.closePoll(id, username);
        return ResponseEntity.ok(ApiResponse.success(poll, "Poll closed successfully"));
    }

    @GetMapping("/created")
    public ResponseEntity<ApiResponse<List<PollResponse>>> getCreatedPolls(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success(pollService.getPollsCreatedByUser(username)));
    }

    @GetMapping("/voted")
    public ResponseEntity<ApiResponse<List<PollResponse>>> getVotedPolls(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(ApiResponse.success(pollService.getPollsVotedByUser(username)));
    }
}

package com.xxxx.votting_spring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    private String description;

    @NotEmpty(message = "At least one option is required")
    @Size(min = 2, message = "Poll must have at least 2 options")
    private List<String> options;

    private LocalDateTime endDate;

    private String mode; // PUBLIC or PRIVATE

    private List<String> invitedEmails; // Optional list of emails to invite
}

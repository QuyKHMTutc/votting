package com.xxxx.votting_spring.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollResponse {

    private Long id;
    private String title;
    private String description;
    private CreatorInfo creator;
    private LocalDateTime createdAt;
    private LocalDateTime endDate;
    private Boolean isActive;
    private String mode;
    private List<OptionInfo> options;
    private Integer totalVotes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatorInfo {
        private Long id;
        private String username;
        private String fullName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionInfo {
        private Long id;
        private String optionText;
        private Integer voteCount;
        private Double percentage;
    }
}

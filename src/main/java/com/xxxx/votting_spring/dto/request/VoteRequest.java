package com.xxxx.votting_spring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotNull(message = "Option ID is required")
    private Long optionId;
}

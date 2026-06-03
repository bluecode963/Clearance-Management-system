package com.se4801.clearance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegistrarDecisionRequest(
        @NotNull(message = "Decision is required")
        RegistrarDecision decision,

        @Size(max = 500, message = "Comment must be at most 500 characters")
        String comment
) {
}

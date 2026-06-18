package com.se4801.clearance.dto.request;

import jakarta.validation.constraints.Size;

public record StudentStepResubmissionRequest(
        @Size(max = 500, message = "Correction note must be at most 500 characters")
        String correctionNote
) {
}

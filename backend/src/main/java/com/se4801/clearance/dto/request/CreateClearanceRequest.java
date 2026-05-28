package com.se4801.clearance.dto.request;

import com.se4801.clearance.model.ClearanceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateClearanceRequest(
        @NotNull(message = "Request type is required")
        ClearanceType requestType,

        @Size(max = 1000, message = "Reason must be at most 1000 characters")
        String reason
) {
}

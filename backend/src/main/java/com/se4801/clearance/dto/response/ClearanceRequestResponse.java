package com.se4801.clearance.dto.response;

import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceType;

import java.time.Instant;
import java.util.List;

public record ClearanceRequestResponse(
        Long id,
        ClearanceType requestType,
        ClearanceRequestStatus status,
        String reason,
        Instant createdAt,
        StudentSummary student,
        List<ClearanceStepResponse> steps
) {
    public record StudentSummary(
            Long studentProfileId,
            String studentId,
            String fullName,
            String department,
            String program
    ) {
    }
}

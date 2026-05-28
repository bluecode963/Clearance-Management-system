package com.se4801.clearance.dto.response;

import com.se4801.clearance.model.ClearanceStepStatus;

import java.time.Instant;

public record OfficeStepReviewResponse(
        Long stepId,
        Long clearanceRequestId,
        String requestType,
        String requestStatus,
        String studentName,
        String studentId,
        Long officeId,
        String officeName,
        ClearanceStepStatus status,
        String comment,
        Instant reviewedAt,
        String reviewedBy
) {
}

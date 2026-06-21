package com.se4801.clearance.dto.response;

import com.se4801.clearance.model.ClearanceStepStatus;

import java.time.Instant;
import java.util.List;

public record ClearanceStepResponse(
        Long id,
        Long officeId,
        String officeName,
        ClearanceStepStatus status,
        String comment,
        Instant reviewedAt,
        List<AttachmentResponse> attachments
) {
}

package com.se4801.clearance.mapper;

import com.se4801.clearance.dto.response.ClearanceStepResponse;
import com.se4801.clearance.dto.response.AttachmentResponse;
import com.se4801.clearance.model.ClearanceStep;

import java.util.List;

public final class ClearanceStepMapper {

    private ClearanceStepMapper() {
    }

    public static ClearanceStepResponse toResponse(ClearanceStep step) {
        return toResponse(step, List.of());
    }

    public static ClearanceStepResponse toResponse(ClearanceStep step, List<AttachmentResponse> attachments) {
        return new ClearanceStepResponse(
                step.getId(),
                step.getOffice().getId(),
                step.getOffice().getOfficeName(),
                step.getStatus(),
                step.getComment(),
                step.getReviewedAt(),
                attachments
        );
    }
}

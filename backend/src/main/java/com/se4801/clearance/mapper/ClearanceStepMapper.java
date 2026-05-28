package com.se4801.clearance.mapper;

import com.se4801.clearance.dto.response.ClearanceStepResponse;
import com.se4801.clearance.model.ClearanceStep;

public final class ClearanceStepMapper {

    private ClearanceStepMapper() {
    }

    public static ClearanceStepResponse toResponse(ClearanceStep step) {
        return new ClearanceStepResponse(
                step.getId(),
                step.getOffice().getId(),
                step.getOffice().getOfficeName(),
                step.getStatus(),
                step.getComment(),
                step.getReviewedAt()
        );
    }
}

package com.se4801.clearance.mapper;

import com.se4801.clearance.dto.response.OfficeStepReviewResponse;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.StudentProfile;

public final class OfficeStepReviewMapper {

    private OfficeStepReviewMapper() {
    }

    public static OfficeStepReviewResponse toResponse(ClearanceStep step) {
        ClearanceRequest request = step.getClearanceRequest();
        StudentProfile studentProfile = request.getStudentProfile();
        return new OfficeStepReviewResponse(
                step.getId(),
                request.getId(),
                request.getRequestType().name(),
                request.getStatus().name(),
                studentProfile.getUser().getFullName(),
                studentProfile.getStudentId(),
                step.getOffice().getId(),
                step.getOffice().getOfficeName(),
                step.getStatus(),
                step.getComment(),
                step.getReviewedAt(),
                step.getReviewedBy() == null ? null : step.getReviewedBy().getFullName()
        );
    }
}

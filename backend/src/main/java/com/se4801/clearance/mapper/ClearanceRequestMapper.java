package com.se4801.clearance.mapper;

import com.se4801.clearance.dto.response.ClearanceRequestResponse;
import com.se4801.clearance.dto.response.ClearanceStepResponse;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.StudentProfile;

import java.util.List;

public final class ClearanceRequestMapper {

    private ClearanceRequestMapper() {
    }

    public static ClearanceRequestResponse toResponse(
            ClearanceRequest clearanceRequest,
            List<ClearanceStepResponse> steps
    ) {
        StudentProfile studentProfile = clearanceRequest.getStudentProfile();
        return new ClearanceRequestResponse(
                clearanceRequest.getId(),
                clearanceRequest.getRequestType(),
                clearanceRequest.getStatus(),
                clearanceRequest.getReason(),
                clearanceRequest.getCreatedAt(),
                new ClearanceRequestResponse.StudentSummary(
                        studentProfile.getId(),
                        studentProfile.getStudentId(),
                        studentProfile.getUser().getFullName(),
                        studentProfile.getDepartment(),
                        studentProfile.getProgram()
                ),
                steps
        );
    }
}

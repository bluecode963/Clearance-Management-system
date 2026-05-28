package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.ReviewClearanceStepRequest;
import com.se4801.clearance.dto.request.ReviewDecision;
import com.se4801.clearance.dto.response.OfficeStepReviewResponse;
import com.se4801.clearance.dto.response.PageResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.exception.ResourceNotFoundException;
import com.se4801.clearance.mapper.OfficeStepReviewMapper;
import com.se4801.clearance.model.ApprovalLog;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.ClearanceStepStatus;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.ApprovalLogRepository;
import com.se4801.clearance.repository.ClearanceRequestRepository;
import com.se4801.clearance.repository.ClearanceStepRepository;
import com.se4801.clearance.repository.UserRepository;
import com.se4801.clearance.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeReviewService {

    private final ClearanceStepRepository clearanceStepRepository;
    private final ClearanceRequestRepository clearanceRequestRepository;
    private final ApprovalLogRepository approvalLogRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<OfficeStepReviewResponse> getAssignedSteps(
            CustomUserPrincipal principal,
            ClearanceStepStatus status,
            Pageable pageable
    ) {
        User staff = getOfficeStaff(principal);
        Page<ClearanceStep> steps = status == null
                ? clearanceStepRepository.findByOfficeId(staff.getOffice().getId(), pageable)
                : clearanceStepRepository.findByOfficeIdAndStatus(staff.getOffice().getId(), status, pageable);

        List<OfficeStepReviewResponse> content = steps.getContent()
                .stream()
                .map(OfficeStepReviewMapper::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                steps.getNumber(),
                steps.getSize(),
                steps.getTotalElements(),
                steps.getTotalPages(),
                steps.isLast()
        );
    }

    @Transactional(readOnly = true)
    public OfficeStepReviewResponse getAssignedStep(Long stepId, CustomUserPrincipal principal) {
        User staff = getOfficeStaff(principal);
        ClearanceStep step = findAssignedStep(stepId, staff);
        return OfficeStepReviewMapper.toResponse(step);
    }

    @Transactional
    public OfficeStepReviewResponse reviewStep(
            Long stepId,
            ReviewClearanceStepRequest request,
            CustomUserPrincipal principal
    ) {
        User staff = getOfficeStaff(principal);
        ClearanceStep step = findAssignedStep(stepId, staff);

        validateReviewRequest(step, request);

        ClearanceStepStatus newStatus = request.decision() == ReviewDecision.APPROVED
                ? ClearanceStepStatus.APPROVED
                : ClearanceStepStatus.REJECTED;

        step.setStatus(newStatus);
        step.setComment(normalizeComment(request.comment()));
        step.setReviewedAt(Instant.now());
        step.setReviewedBy(staff);

        ClearanceStep savedStep = clearanceStepRepository.save(step);
        saveApprovalLog(savedStep, staff, request);
        updateParentRequestStatus(savedStep.getClearanceRequest());

        return OfficeStepReviewMapper.toResponse(savedStep);
    }

    private User getOfficeStaff(CustomUserPrincipal principal) {
        if (principal.getRole() != Role.OFFICE_STAFF) {
            throw new BusinessRuleException("Only office staff can review clearance steps");
        }

        User staff = userRepository.findWithOfficeById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Office staff user not found"));

        if (staff.getOffice() == null) {
            throw new BusinessRuleException("Office staff must be assigned to an office before reviewing steps");
        }

        return staff;
    }

    private ClearanceStep findAssignedStep(Long stepId, User staff) {
        return clearanceStepRepository.findByIdAndOfficeId(stepId, staff.getOffice().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Clearance step not found for your office"));
    }

    private void validateReviewRequest(ClearanceStep step, ReviewClearanceStepRequest request) {
        if (isRegistrarStep(step)) {
            throw new BusinessRuleException("Registrar clearance step cannot be reviewed in the office staff workflow");
        }
        if (step.getStatus() == ClearanceStepStatus.APPROVED || step.getStatus() == ClearanceStepStatus.REJECTED) {
            throw new BusinessRuleException("This clearance step has already been reviewed");
        }
        if (request.decision() == ReviewDecision.REJECTED && isBlank(request.comment())) {
            throw new BusinessRuleException("Comment is required when rejecting a clearance step");
        }
    }

    private void saveApprovalLog(ClearanceStep step, User staff, ReviewClearanceStepRequest request) {
        ApprovalLog log = ApprovalLog.builder()
                .action(request.decision().name())
                .note(normalizeComment(request.comment()))
                .actor(staff)
                .clearanceRequest(step.getClearanceRequest())
                .clearanceStep(step)
                .build();
        approvalLogRepository.save(log);
    }

    private void updateParentRequestStatus(ClearanceRequest request) {
        List<ClearanceStep> steps = clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(request.getId());

        if (steps.stream().anyMatch(step -> step.getStatus() == ClearanceStepStatus.REJECTED)) {
            request.setStatus(ClearanceRequestStatus.REJECTED);
        } else if (steps.stream().filter(step -> !isRegistrarStep(step))
                .allMatch(step -> step.getStatus() == ClearanceStepStatus.APPROVED)) {
            request.setStatus(ClearanceRequestStatus.READY_FOR_REGISTRAR);
        } else {
            request.setStatus(ClearanceRequestStatus.IN_REVIEW);
        }

        clearanceRequestRepository.save(request);
    }

    private boolean isRegistrarStep(ClearanceStep step) {
        return "Registrar".equalsIgnoreCase(step.getOffice().getOfficeName());
    }

    private String normalizeComment(String comment) {
        return isBlank(comment) ? null : comment.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

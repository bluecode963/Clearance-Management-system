package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.CreateClearanceRequest;
import com.se4801.clearance.dto.request.StudentStepResubmissionRequest;
import com.se4801.clearance.dto.response.ClearanceRequestResponse;
import com.se4801.clearance.dto.response.ClearanceStepResponse;
import com.se4801.clearance.dto.response.PageResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.exception.ResourceNotFoundException;
import com.se4801.clearance.mapper.ClearanceRequestMapper;
import com.se4801.clearance.mapper.ClearanceStepMapper;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.ClearanceStepStatus;
import com.se4801.clearance.model.Office;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.StudentProfile;
import com.se4801.clearance.model.ApprovalLog;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.ApprovalLogRepository;
import com.se4801.clearance.repository.ClearanceRequestRepository;
import com.se4801.clearance.repository.ClearanceStepRepository;
import com.se4801.clearance.repository.OfficeRepository;
import com.se4801.clearance.repository.StudentProfileRepository;
import com.se4801.clearance.repository.UserRepository;
import com.se4801.clearance.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClearanceRequestService {

    private static final Set<ClearanceRequestStatus> ACTIVE_STATUSES = EnumSet.of(
            ClearanceRequestStatus.PENDING,
            ClearanceRequestStatus.IN_REVIEW,
            ClearanceRequestStatus.NEEDS_CORRECTION,
            ClearanceRequestStatus.READY_FOR_REGISTRAR
    );

    private final ClearanceRequestRepository clearanceRequestRepository;
    private final ClearanceStepRepository clearanceStepRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final OfficeRepository officeRepository;
    private final ApprovalLogRepository approvalLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClearanceRequestResponse createRequest(CreateClearanceRequest request, CustomUserPrincipal principal) {
        ensureStudent(principal);
        StudentProfile studentProfile = findStudentProfile(principal.getId());

        if (clearanceRequestRepository.existsByStudentProfileUserIdAndStatusIn(principal.getId(), ACTIVE_STATUSES)) {
            throw new BusinessRuleException("You already have an active clearance request");
        }

        List<Office> offices = officeRepository.findByActiveTrueOrderByIdAsc();
        if (offices.isEmpty()) {
            throw new BusinessRuleException("No active clearance offices are configured");
        }

        ClearanceRequest clearanceRequest = ClearanceRequest.builder()
                .requestType(request.requestType())
                .status(ClearanceRequestStatus.IN_REVIEW)
                .reason(normalizeReason(request.reason()))
                .studentProfile(studentProfile)
                .build();

        ClearanceRequest savedRequest = clearanceRequestRepository.save(clearanceRequest);

        List<ClearanceStep> steps = offices.stream()
                .map(office -> ClearanceStep.builder()
                        .clearanceRequest(savedRequest)
                        .office(office)
                        .status(resolveInitialStepStatus(office))
                        .build())
                .toList();
        clearanceStepRepository.saveAll(steps);

        return toResponse(savedRequest);
    }

    @Transactional(readOnly = true)
    public PageResponse<ClearanceRequestResponse> getMyRequests(CustomUserPrincipal principal, Pageable pageable) {
        ensureStudent(principal);
        Page<ClearanceRequest> requests = clearanceRequestRepository.findByStudentProfileUserId(
                principal.getId(),
                pageable
        );

        List<ClearanceRequestResponse> content = requests.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PageResponse<>(
                content,
                requests.getNumber(),
                requests.getSize(),
                requests.getTotalElements(),
                requests.getTotalPages(),
                requests.isLast()
        );
    }

    @Transactional(readOnly = true)
    public ClearanceRequestResponse getMyRequest(Long requestId, CustomUserPrincipal principal) {
        ensureStudent(principal);
        ClearanceRequest request = clearanceRequestRepository.findByIdAndStudentProfileUserId(
                        requestId,
                        principal.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Clearance request not found"));
        return toResponse(request);
    }

    @Transactional
    public ClearanceRequestResponse resubmitStep(
            Long stepId,
            StudentStepResubmissionRequest request,
            CustomUserPrincipal principal
    ) {
        ensureStudent(principal);
        ClearanceStep step = clearanceStepRepository.findByIdAndClearanceRequestStudentProfileUserId(
                        stepId,
                        principal.getId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("Clearance step not found for your request"));

        if (!canStudentResubmit(step)) {
            throw new BusinessRuleException("Only office steps needing correction can be resubmitted");
        }

        User student = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found"));

        step.setStatus(ClearanceStepStatus.RESUBMITTED);
        step.setComment(normalizeReason(request.correctionNote()));
        step.setReviewedAt(null);
        step.setReviewedBy(null);
        ClearanceStep savedStep = clearanceStepRepository.save(step);

        ClearanceRequest clearanceRequest = savedStep.getClearanceRequest();
        updateParentStatusAfterResubmission(clearanceRequest);
        saveResubmissionLog(savedStep, student, request);

        return toResponse(clearanceRequest);
    }

    private ClearanceRequestResponse toResponse(ClearanceRequest clearanceRequest) {
        List<ClearanceStepResponse> steps = clearanceStepRepository
                .findByClearanceRequestIdOrderByOfficeIdAsc(clearanceRequest.getId())
                .stream()
                .map(ClearanceStepMapper::toResponse)
                .toList();
        return ClearanceRequestMapper.toResponse(clearanceRequest, steps);
    }

    private StudentProfile findStudentProfile(Long userId) {
        return studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessRuleException("Student profile is required to create a clearance request"));
    }

    private void ensureStudent(CustomUserPrincipal principal) {
        if (principal.getRole() != Role.STUDENT) {
            throw new BusinessRuleException("Only students can access clearance request workflow");
        }
    }

    private ClearanceStepStatus resolveInitialStepStatus(Office office) {
        if ("Registrar".equalsIgnoreCase(office.getOfficeName())) {
            return ClearanceStepStatus.WAITING;
        }
        return ClearanceStepStatus.PENDING;
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? null : reason.trim();
    }

    private void updateParentStatusAfterResubmission(ClearanceRequest request) {
        List<ClearanceStep> steps = clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(request.getId());
        boolean hasOtherCorrection = steps.stream()
                .filter(step -> !isRegistrarStep(step))
                .anyMatch(step -> step.getStatus() == ClearanceStepStatus.NEEDS_CORRECTION
                        || step.getStatus() == ClearanceStepStatus.REJECTED);

        request.setStatus(hasOtherCorrection
                ? ClearanceRequestStatus.NEEDS_CORRECTION
                : ClearanceRequestStatus.IN_REVIEW);
        clearanceRequestRepository.save(request);
    }

    private void saveResubmissionLog(
            ClearanceStep step,
            User student,
            StudentStepResubmissionRequest request
    ) {
        ApprovalLog log = ApprovalLog.builder()
                .action("STUDENT_RESUBMITTED_STEP")
                .note(normalizeReason(request.correctionNote()))
                .actor(student)
                .clearanceRequest(step.getClearanceRequest())
                .clearanceStep(step)
                .build();
        approvalLogRepository.save(log);
    }

    private boolean isRegistrarStep(ClearanceStep step) {
        return "Registrar".equalsIgnoreCase(step.getOffice().getOfficeName());
    }

    private boolean canStudentResubmit(ClearanceStep step) {
        ClearanceRequest clearanceRequest = step.getClearanceRequest();
        if (isRegistrarStep(step)
                || clearanceRequest.getStatus() == ClearanceRequestStatus.COMPLETED
                || clearanceRequest.getStatus() == ClearanceRequestStatus.CANCELLED) {
            return false;
        }

        return step.getStatus() == ClearanceStepStatus.NEEDS_CORRECTION
                || step.getStatus() == ClearanceStepStatus.REJECTED;
    }
}

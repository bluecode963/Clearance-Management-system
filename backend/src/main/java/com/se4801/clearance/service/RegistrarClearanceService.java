package com.se4801.clearance.service;

import com.se4801.clearance.dto.request.RegistrarDecision;
import com.se4801.clearance.dto.request.RegistrarDecisionRequest;
import com.se4801.clearance.dto.response.ClearanceRequestResponse;
import com.se4801.clearance.dto.response.ClearanceStepResponse;
import com.se4801.clearance.dto.response.PageResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.exception.ResourceNotFoundException;
import com.se4801.clearance.mapper.ClearanceRequestMapper;
import com.se4801.clearance.mapper.ClearanceStepMapper;
import com.se4801.clearance.model.ApprovalLog;
import com.se4801.clearance.model.AttachmentPurpose;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.ClearanceStepStatus;
import com.se4801.clearance.model.ClearanceType;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistrarClearanceService {

    private static final Set<ClearanceRequestStatus> VIEWABLE_STATUSES = EnumSet.of(
            ClearanceRequestStatus.READY_FOR_REGISTRAR,
            ClearanceRequestStatus.COMPLETED,
            ClearanceRequestStatus.REJECTED
    );

    private final ClearanceRequestRepository clearanceRequestRepository;
    private final ClearanceStepRepository clearanceStepRepository;
    private final ApprovalLogRepository approvalLogRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    @Transactional(readOnly = true)
    public PageResponse<ClearanceRequestResponse> listRequestsForRegistrar(
            CustomUserPrincipal principal,
            ClearanceRequestStatus status,
            ClearanceType requestType,
            String studentId,
            String keyword,
            boolean includeAll,
            Pageable pageable
    ) {
        ensureRegistrar(principal);

        Page<ClearanceRequest> requests = clearanceRequestRepository.searchForRegistrar(
                status == null && !includeAll ? ClearanceRequestStatus.READY_FOR_REGISTRAR : status,
                requestType,
                normalizeFilter(studentId),
                normalizeFilter(keyword),
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
    public ClearanceRequestResponse getRequestForRegistrar(Long requestId, CustomUserPrincipal principal) {
        ensureRegistrar(principal);
        ClearanceRequest request = getRequest(requestId);

        if (!VIEWABLE_STATUSES.contains(request.getStatus())) {
            throw new BusinessRuleException("Clearance request is not ready for registrar review");
        }

        return toResponse(request);
    }

    @Transactional
    public ClearanceRequestResponse decideFinalClearance(
            Long requestId,
            RegistrarDecisionRequest decisionRequest,
            CustomUserPrincipal principal
    ) {
        return decideFinalClearance(requestId, decisionRequest, principal, null);
    }

    @Transactional
    public ClearanceRequestResponse decideFinalClearance(
            Long requestId,
            RegistrarDecisionRequest decisionRequest,
            CustomUserPrincipal principal,
            MultipartFile attachment
    ) {
        ensureRegistrar(principal);
        User registrar = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Registrar user not found"));
        ClearanceRequest request = getRequest(requestId);

        validateDecisionRequest(request, decisionRequest, attachment);

        ClearanceRequestStatus finalStatus = decisionRequest.decision() == RegistrarDecision.APPROVED
                ? ClearanceRequestStatus.COMPLETED
                : ClearanceRequestStatus.REJECTED;

        request.setStatus(finalStatus);
        clearanceRequestRepository.save(request);

        ClearanceStep registrarStep = updateRegistrarStep(request, registrar, decisionRequest);
        saveApprovalLog(request, registrar, decisionRequest);
        attachmentService.storeWorkflowFile(
                request,
                registrarStep,
                registrar,
                AttachmentPurpose.REGISTRAR_DECISION,
                attachment
        );

        return toResponse(request);
    }

    private ClearanceRequest getRequest(Long requestId) {
        return clearanceRequestRepository.findWithStudentProfileById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Clearance request not found"));
    }

    private void validateDecisionRequest(
            ClearanceRequest request,
            RegistrarDecisionRequest decisionRequest,
            MultipartFile attachment
    ) {
        if (request.getStatus() != ClearanceRequestStatus.READY_FOR_REGISTRAR) {
            throw new BusinessRuleException("Only requests ready for registrar review can receive a final decision");
        }

        if (decisionRequest.decision() == RegistrarDecision.REJECTED && isBlank(decisionRequest.comment())) {
            throw new BusinessRuleException("Comment is required when registrar rejects a clearance request");
        }
        if (decisionRequest.decision() == RegistrarDecision.APPROVED
                && !attachmentService.hasUpload(attachment)) {
            throw new BusinessRuleException("An attachment is required for registrar approval");
        }

        List<ClearanceStep> steps = clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(request.getId());
        boolean allOfficeStepsApproved = steps.stream()
                .filter(step -> !isRegistrarStep(step))
                .allMatch(step -> step.getStatus() == ClearanceStepStatus.APPROVED);

        if (!allOfficeStepsApproved) {
            throw new BusinessRuleException("All office steps must be approved before registrar final decision");
        }
    }

    private ClearanceStep updateRegistrarStep(
            ClearanceRequest request,
            User registrar,
            RegistrarDecisionRequest decisionRequest
    ) {
        ClearanceStepStatus stepStatus = decisionRequest.decision() == RegistrarDecision.APPROVED
                ? ClearanceStepStatus.APPROVED
                : ClearanceStepStatus.REJECTED;

        ClearanceStep registrarStep = clearanceStepRepository.findByClearanceRequestIdOrderByOfficeIdAsc(request.getId())
                .stream()
                .filter(this::isRegistrarStep)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("Registrar clearance step is missing"));
        registrarStep.setStatus(stepStatus);
        registrarStep.setComment(normalizeComment(decisionRequest.comment()));
        registrarStep.setReviewedBy(registrar);
        registrarStep.setReviewedAt(Instant.now());
        return clearanceStepRepository.save(registrarStep);
    }

    private void saveApprovalLog(
            ClearanceRequest request,
            User registrar,
            RegistrarDecisionRequest decisionRequest
    ) {
        ApprovalLog log = ApprovalLog.builder()
                .action(decisionRequest.decision() == RegistrarDecision.APPROVED
                        ? "REGISTRAR_APPROVED"
                        : "REGISTRAR_REJECTED")
                .note(normalizeComment(decisionRequest.comment()))
                .actor(registrar)
                .clearanceRequest(request)
                .build();
        approvalLogRepository.save(log);
    }

    private ClearanceRequestResponse toResponse(ClearanceRequest request) {
        List<ClearanceStepResponse> steps = clearanceStepRepository
                .findByClearanceRequestIdOrderByOfficeIdAsc(request.getId())
                .stream()
                .map(step -> ClearanceStepMapper.toResponse(step, attachmentService.getStepAttachments(step.getId())))
                .toList();
        return ClearanceRequestMapper.toResponse(request, steps);
    }

    private void ensureRegistrar(CustomUserPrincipal principal) {
        if (principal.getRole() != Role.REGISTRAR) {
            throw new BusinessRuleException("Only registrar users can access final clearance review");
        }
    }

    private boolean isRegistrarStep(ClearanceStep step) {
        return "Registrar".equalsIgnoreCase(step.getOffice().getOfficeName());
    }

    private String normalizeFilter(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeComment(String comment) {
        return isBlank(comment) ? null : comment.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

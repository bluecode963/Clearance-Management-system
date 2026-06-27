package com.se4801.clearance.controller;

import com.se4801.clearance.dto.request.ReviewClearanceStepRequest;
import com.se4801.clearance.dto.response.OfficeStepReviewResponse;
import com.se4801.clearance.dto.response.PageResponse;
import com.se4801.clearance.model.ClearanceStepStatus;
import com.se4801.clearance.security.CustomUserPrincipal;
import com.se4801.clearance.service.OfficeReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/office/clearance-steps")
@RequiredArgsConstructor
public class OfficeReviewController {

    private static final int MAX_PAGE_SIZE = 50;

    private final OfficeReviewService officeReviewService;

    @GetMapping
    @PreAuthorize("hasRole('OFFICE_STAFF')")
    public ResponseEntity<PageResponse<OfficeStepReviewResponse>> getAssignedSteps(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ClearanceStepStatus status,
            @RequestParam(defaultValue = "false") boolean includeAll
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        return ResponseEntity.ok(officeReviewService.getAssignedSteps(principal, status, includeAll, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OFFICE_STAFF')")
    public ResponseEntity<OfficeStepReviewResponse> getAssignedStep(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(officeReviewService.getAssignedStep(id, principal));
    }

    @PatchMapping(value = "/{id}/review", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('OFFICE_STAFF')")
    public ResponseEntity<OfficeStepReviewResponse> reviewStep(
            @PathVariable Long id,
            @Valid @RequestBody ReviewClearanceStepRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(officeReviewService.reviewStep(id, request, principal));
    }

    @PatchMapping(value = "/{id}/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('OFFICE_STAFF')")
    public ResponseEntity<OfficeStepReviewResponse> reviewStepWithAttachments(
            @PathVariable Long id,
            @Valid @RequestPart("request") ReviewClearanceStepRequest request,
            @RequestPart(required = false) MultipartFile attachment,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(officeReviewService.reviewStep(id, request, principal, attachment));
    }
}

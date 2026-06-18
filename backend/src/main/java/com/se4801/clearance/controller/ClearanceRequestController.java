package com.se4801.clearance.controller;

import com.se4801.clearance.dto.request.CreateClearanceRequest;
import com.se4801.clearance.dto.request.StudentStepResubmissionRequest;
import com.se4801.clearance.dto.response.ClearanceRequestResponse;
import com.se4801.clearance.dto.response.PageResponse;
import com.se4801.clearance.security.CustomUserPrincipal;
import com.se4801.clearance.service.ClearanceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clearance-requests")
@RequiredArgsConstructor
public class ClearanceRequestController {

    private static final int MAX_PAGE_SIZE = 50;

    private final ClearanceRequestService clearanceRequestService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClearanceRequestResponse> createClearanceRequest(
            @Valid @RequestBody CreateClearanceRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clearanceRequestService.createRequest(request, principal));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<PageResponse<ClearanceRequestResponse>> getMyClearanceRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return ResponseEntity.ok(clearanceRequestService.getMyRequests(principal, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClearanceRequestResponse> getMyClearanceRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(clearanceRequestService.getMyRequest(id, principal));
    }

    @PatchMapping("/steps/{stepId}/resubmit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ClearanceRequestResponse> resubmitStep(
            @PathVariable Long stepId,
            @Valid @RequestBody StudentStepResubmissionRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(clearanceRequestService.resubmitStep(stepId, request, principal));
    }
}

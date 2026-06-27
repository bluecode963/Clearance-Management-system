package com.se4801.clearance.controller;

import com.se4801.clearance.dto.request.RegistrarDecisionRequest;
import com.se4801.clearance.dto.response.ClearanceRequestResponse;
import com.se4801.clearance.dto.response.PageResponse;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceType;
import com.se4801.clearance.security.CustomUserPrincipal;
import com.se4801.clearance.service.RegistrarClearanceService;
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
@RequestMapping("/api/registrar/clearance-requests")
@RequiredArgsConstructor
public class RegistrarClearanceController {

    private static final int MAX_PAGE_SIZE = 50;

    private final RegistrarClearanceService registrarClearanceService;

    @GetMapping
    @PreAuthorize("hasRole('REGISTRAR')")
    public ResponseEntity<PageResponse<ClearanceRequestResponse>> getRegistrarRequests(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(required = false) ClearanceRequestStatus status,
            @RequestParam(required = false) ClearanceType requestType,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean includeAll,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.ASC, "createdAt")
        );
        return ResponseEntity.ok(registrarClearanceService.listRequestsForRegistrar(
                principal,
                status,
                requestType,
                studentId,
                keyword,
                includeAll,
                pageable
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('REGISTRAR')")
    public ResponseEntity<ClearanceRequestResponse> getRegistrarRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(registrarClearanceService.getRequestForRegistrar(id, principal));
    }

    @PatchMapping(value = "/{id}/decision", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('REGISTRAR')")
    public ResponseEntity<ClearanceRequestResponse> decideFinalClearance(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarDecisionRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(registrarClearanceService.decideFinalClearance(id, request, principal));
    }

    @PatchMapping(value = "/{id}/decision", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('REGISTRAR')")
    public ResponseEntity<ClearanceRequestResponse> decideFinalClearanceWithAttachments(
            @PathVariable Long id,
            @Valid @RequestPart("request") RegistrarDecisionRequest request,
            @RequestPart(required = false) MultipartFile attachment,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(registrarClearanceService.decideFinalClearance(
                id, request, principal, attachment
        ));
    }
}

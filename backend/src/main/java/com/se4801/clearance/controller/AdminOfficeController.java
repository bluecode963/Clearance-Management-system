package com.se4801.clearance.controller;

import com.se4801.clearance.dto.response.OfficeResponse;
import com.se4801.clearance.service.AdminOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/offices")
@RequiredArgsConstructor
public class AdminOfficeController {

    private final AdminOfficeService adminOfficeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OfficeResponse>> getActiveOffices() {
        return ResponseEntity.ok(adminOfficeService.getActiveOffices());
    }
}

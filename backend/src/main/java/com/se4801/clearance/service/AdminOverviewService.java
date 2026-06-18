package com.se4801.clearance.service;

import com.se4801.clearance.dto.response.AdminOverviewResponse;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.repository.ClearanceRequestRepository;
import com.se4801.clearance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOverviewService {

    private final UserRepository userRepository;
    private final ClearanceRequestRepository clearanceRequestRepository;

    public AdminOverviewResponse getOverview() {
        return new AdminOverviewResponse(
                userRepository.count(),
                userRepository.countByRole(Role.STUDENT),
                userRepository.countByRole(Role.OFFICE_STAFF),
                userRepository.countByRole(Role.REGISTRAR),
                userRepository.countByRole(Role.ADMIN),
                clearanceRequestRepository.count(),
                clearanceRequestRepository.countByStatus(ClearanceRequestStatus.PENDING),
                clearanceRequestRepository.countByStatus(ClearanceRequestStatus.IN_REVIEW),
                clearanceRequestRepository.countByStatus(ClearanceRequestStatus.READY_FOR_REGISTRAR),
                clearanceRequestRepository.countByStatus(ClearanceRequestStatus.COMPLETED),
                clearanceRequestRepository.countByStatus(ClearanceRequestStatus.REJECTED)
        );
    }
}

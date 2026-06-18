package com.se4801.clearance.dto.response;

public record AdminOverviewResponse(
        long totalUsers,
        long totalStudents,
        long totalOfficeStaff,
        long totalRegistrars,
        long totalAdmins,
        long totalClearanceRequests,
        long pendingRequests,
        long inReviewRequests,
        long readyForRegistrarRequests,
        long completedRequests,
        long rejectedRequests
) {
}

package com.se4801.clearance.repository;

import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.ClearanceStepStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClearanceStepRepository extends JpaRepository<ClearanceStep, Long> {
    boolean existsByClearanceRequestIdAndOfficeId(Long clearanceRequestId, Long officeId);

    @EntityGraph(attributePaths = {"office", "clearanceRequest", "clearanceRequest.studentProfile", "clearanceRequest.studentProfile.user", "reviewedBy"})
    Page<ClearanceStep> findByOfficeId(Long officeId, Pageable pageable);

    @EntityGraph(attributePaths = {"office", "clearanceRequest", "clearanceRequest.studentProfile", "clearanceRequest.studentProfile.user", "reviewedBy"})
    Page<ClearanceStep> findByOfficeIdAndStatus(Long officeId, ClearanceStepStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"office", "clearanceRequest", "clearanceRequest.studentProfile", "clearanceRequest.studentProfile.user", "reviewedBy"})
    Optional<ClearanceStep> findByIdAndOfficeId(Long id, Long officeId);

    @EntityGraph(attributePaths = {"office", "clearanceRequest", "clearanceRequest.studentProfile", "clearanceRequest.studentProfile.user", "reviewedBy"})
    Optional<ClearanceStep> findByIdAndClearanceRequestStudentProfileUserId(Long id, Long userId);

    @EntityGraph(attributePaths = "office")
    List<ClearanceStep> findByClearanceRequestIdOrderByOfficeIdAsc(Long clearanceRequestId);

    default List<ClearanceStep> findByClearanceRequestId(Long clearanceRequestId) {
        return findByClearanceRequestIdOrderByOfficeIdAsc(clearanceRequestId);
    }
}

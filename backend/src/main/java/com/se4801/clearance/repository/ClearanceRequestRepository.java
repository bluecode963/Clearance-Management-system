package com.se4801.clearance.repository;

import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Collection;
import java.util.Optional;

public interface ClearanceRequestRepository extends JpaRepository<ClearanceRequest, Long> {
    @EntityGraph(attributePaths = {"studentProfile", "studentProfile.user"})
    Page<ClearanceRequest> findByStudentProfileUserId(Long userId, Pageable pageable);

    boolean existsByStudentProfileUserIdAndStatusIn(Long userId, Collection<ClearanceRequestStatus> statuses);

    @EntityGraph(attributePaths = {"studentProfile", "studentProfile.user"})
    Optional<ClearanceRequest> findByIdAndStudentProfileUserId(Long id, Long userId);
}

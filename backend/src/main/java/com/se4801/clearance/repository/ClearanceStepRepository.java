package com.se4801.clearance.repository;

import com.se4801.clearance.model.ClearanceStep;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClearanceStepRepository extends JpaRepository<ClearanceStep, Long> {
    Page<ClearanceStep> findByOfficeId(Long officeId, Pageable pageable);

    @EntityGraph(attributePaths = "office")
    List<ClearanceStep> findByClearanceRequestIdOrderByOfficeIdAsc(Long clearanceRequestId);

    default List<ClearanceStep> findByClearanceRequestId(Long clearanceRequestId) {
        return findByClearanceRequestIdOrderByOfficeIdAsc(clearanceRequestId);
    }
}

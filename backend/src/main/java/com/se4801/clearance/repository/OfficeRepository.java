package com.se4801.clearance.repository;

import com.se4801.clearance.model.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfficeRepository extends JpaRepository<Office, Long> {
    Optional<Office> findByOfficeName(String officeName);

    List<Office> findByActiveTrue();

    List<Office> findByActiveTrueOrderByIdAsc();
}

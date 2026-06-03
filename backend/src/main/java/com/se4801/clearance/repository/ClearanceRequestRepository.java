package com.se4801.clearance.repository;

import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceRequestStatus;
import com.se4801.clearance.model.ClearanceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface ClearanceRequestRepository extends JpaRepository<ClearanceRequest, Long> {
    @EntityGraph(attributePaths = {"studentProfile", "studentProfile.user"})
    Page<ClearanceRequest> findByStudentProfileUserId(Long userId, Pageable pageable);

    boolean existsByStudentProfileUserIdAndStatusIn(Long userId, Collection<ClearanceRequestStatus> statuses);

    @EntityGraph(attributePaths = {"studentProfile", "studentProfile.user"})
    Optional<ClearanceRequest> findByIdAndStudentProfileUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"studentProfile", "studentProfile.user"})
    @Query("""
            select request
            from ClearanceRequest request
            join request.studentProfile profile
            join profile.user student
            where (:status is null or request.status = :status)
              and (:requestType is null or request.requestType = :requestType)
              and (:studentId is null or lower(profile.studentId) like lower(concat('%', :studentId, '%')))
              and (:keyword is null
                   or lower(student.fullName) like lower(concat('%', :keyword, '%'))
                   or lower(student.email) like lower(concat('%', :keyword, '%'))
                   or lower(profile.studentId) like lower(concat('%', :keyword, '%')))
            """)
    Page<ClearanceRequest> searchForRegistrar(
            @Param("status") ClearanceRequestStatus status,
            @Param("requestType") ClearanceType requestType,
            @Param("studentId") String studentId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"studentProfile", "studentProfile.user"})
    @Query("select request from ClearanceRequest request where request.id = :id")
    Optional<ClearanceRequest> findWithStudentProfileById(@Param("id") Long id);
}

package com.se4801.clearance.repository;

import com.se4801.clearance.model.Attachment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @EntityGraph(attributePaths = "uploadedBy")
    List<Attachment> findByClearanceStepIdOrderByUploadedAtAsc(Long clearanceStepId);

    @EntityGraph(attributePaths = {
            "clearanceRequest.studentProfile.user",
            "clearanceStep.office",
            "uploadedBy"
    })
    Optional<Attachment> findWithAccessDetailsById(Long id);
}

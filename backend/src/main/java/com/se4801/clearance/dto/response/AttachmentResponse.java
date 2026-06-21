package com.se4801.clearance.dto.response;

import com.se4801.clearance.model.AttachmentKind;
import com.se4801.clearance.model.AttachmentPurpose;

import java.time.Instant;

public record AttachmentResponse(
        Long id,
        String fileName,
        String contentType,
        AttachmentKind attachmentKind,
        AttachmentPurpose purpose,
        Instant uploadedAt,
        String uploadedBy,
        String downloadUrl
) {
}

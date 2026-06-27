package com.se4801.clearance.service;

import com.se4801.clearance.dto.response.AttachmentDownload;
import com.se4801.clearance.dto.response.AttachmentResponse;
import com.se4801.clearance.exception.BusinessRuleException;
import com.se4801.clearance.exception.ResourceNotFoundException;
import com.se4801.clearance.model.Attachment;
import com.se4801.clearance.model.AttachmentKind;
import com.se4801.clearance.model.AttachmentPurpose;
import com.se4801.clearance.model.ClearanceRequest;
import com.se4801.clearance.model.ClearanceStep;
import com.se4801.clearance.model.Role;
import com.se4801.clearance.model.User;
import com.se4801.clearance.repository.AttachmentRepository;
import com.se4801.clearance.repository.UserRepository;
import com.se4801.clearance.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.directory:uploads}")
    private String uploadDirectory;

    public boolean hasUpload(MultipartFile attachment) {
        return hasContent(attachment);
    }

    public void storeWorkflowFile(
            ClearanceRequest clearanceRequest,
            ClearanceStep clearanceStep,
            User uploader,
            AttachmentPurpose purpose,
            MultipartFile attachment
    ) {
        if (!hasContent(attachment)) {
            return;
        }
        storeOne(clearanceRequest, clearanceStep, uploader, purpose, attachment, resolveKind(attachment));
    }

    public List<AttachmentResponse> getStepAttachments(Long stepId) {
        return attachmentRepository.findByClearanceStepIdOrderByUploadedAtAsc(stepId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AttachmentDownload loadForDownload(Long attachmentId, CustomUserPrincipal principal) {
        Attachment attachment = attachmentRepository.findWithAccessDetailsById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        ensureCanDownload(attachment, principal);

        Path root = uploadRoot();
        Path file = root.resolve(attachment.getFilePath()).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Attachment file not found");
        }

        try {
            Resource resource = new UrlResource(file.toUri());
            return new AttachmentDownload(resource, attachment.getFileName(), attachment.getContentType());
        } catch (MalformedURLException exception) {
            throw new ResourceNotFoundException("Attachment file not found");
        }
    }

    private void storeOne(
            ClearanceRequest clearanceRequest,
            ClearanceStep clearanceStep,
            User uploader,
            AttachmentPurpose purpose,
            MultipartFile file,
            AttachmentKind kind
    ) {
        if (!hasContent(file)) {
            return;
        }

        validate(file, kind);
        String originalName = safeOriginalName(file);
        String storedName = UUID.randomUUID() + extension(originalName);
        Path destination = uploadRoot().resolve(storedName).normalize();

        try {
            Files.createDirectories(uploadRoot());
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessRuleException("Could not store attachment");
        }

        try {
            attachmentRepository.save(Attachment.builder()
                    .fileName(originalName)
                    .filePath(storedName)
                    .contentType(normalizeContentType(file.getContentType()))
                    .attachmentKind(kind)
                    .purpose(purpose)
                    .clearanceRequest(clearanceRequest)
                    .clearanceStep(clearanceStep)
                    .uploadedBy(uploader)
                    .build());
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {
                // The database error remains the primary failure.
            }
            throw exception;
        }
    }

    private void validate(MultipartFile file, AttachmentKind kind) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessRuleException("Each attachment must be 10 MB or smaller");
        }

        String contentType = normalizeContentType(file.getContentType());
        Set<String> allowed = kind == AttachmentKind.IMAGE ? IMAGE_TYPES : DOCUMENT_TYPES;
        if (!allowed.contains(contentType)) {
            throw new BusinessRuleException(kind == AttachmentKind.IMAGE
                    ? "Picture must be JPG, PNG, or WEBP"
                    : "Document must be PDF, DOC, DOCX, or TXT");
        }
    }

    private AttachmentKind resolveKind(MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        if (IMAGE_TYPES.contains(contentType)) {
            return AttachmentKind.IMAGE;
        }
        if (DOCUMENT_TYPES.contains(contentType)) {
            return AttachmentKind.DOCUMENT;
        }
        throw new BusinessRuleException("Attachment must be PDF, DOC, DOCX, TXT, JPG, PNG, or WEBP");
    }

    private void ensureCanDownload(Attachment attachment, CustomUserPrincipal principal) {
        if (principal.getRole() == Role.ADMIN || principal.getRole() == Role.REGISTRAR) {
            return;
        }
        if (principal.getRole() == Role.STUDENT
                && attachment.getClearanceRequest().getStudentProfile().getUser().getId().equals(principal.getId())) {
            return;
        }
        if (principal.getRole() == Role.OFFICE_STAFF) {
            User staff = userRepository.findWithOfficeById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Office staff user not found"));
            if (staff.getOffice() != null
                    && attachment.getClearanceStep() != null
                    && attachment.getClearanceStep().getOffice().getId().equals(staff.getOffice().getId())) {
                return;
            }
        }
        throw new AccessDeniedException("You cannot access this attachment");
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        User uploadedBy = attachment.getUploadedBy();
        return new AttachmentResponse(
                attachment.getId(), attachment.getFileName(), attachment.getContentType(),
                resolveFileSize(attachment),
                attachment.getAttachmentKind(), attachment.getPurpose(), attachment.getUploadedAt(),
                uploadedBy == null ? null : uploadedBy.getFullName(),
                uploadedBy == null ? null : uploadedBy.getRole().name(),
                "/api/attachments/" + attachment.getId()
        );
    }

    private Long resolveFileSize(Attachment attachment) {
        try {
            Path file = uploadRoot().resolve(attachment.getFilePath()).normalize();
            if (!file.startsWith(uploadRoot()) || !Files.isRegularFile(file)) {
                return null;
            }
            return Files.size(file);
        } catch (IOException exception) {
            return null;
        }
    }

    private Path uploadRoot() {
        return Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    private boolean hasContent(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private String safeOriginalName(MultipartFile file) {
        String original = file.getOriginalFilename();
        String name = original == null ? "attachment" : Paths.get(original).getFileName().toString();
        if (name.isBlank() || name.length() > 255) {
            throw new BusinessRuleException("Attachment file name is invalid");
        }
        return name;
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "application/octet-stream" : contentType.toLowerCase(Locale.ROOT);
    }
}

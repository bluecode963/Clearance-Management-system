ALTER TABLE attachments
    ADD COLUMN content_type VARCHAR(100) NOT NULL DEFAULT 'application/octet-stream',
    ADD COLUMN attachment_kind VARCHAR(20) NOT NULL DEFAULT 'DOCUMENT',
    ADD COLUMN purpose VARCHAR(40) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN clearance_step_id BIGINT,
    ADD COLUMN uploaded_by_id BIGINT;

ALTER TABLE attachments
    ADD CONSTRAINT fk_attachments_step
        FOREIGN KEY (clearance_step_id) REFERENCES clearance_steps (id),
    ADD CONSTRAINT fk_attachments_uploaded_by
        FOREIGN KEY (uploaded_by_id) REFERENCES users (id),
    ADD CONSTRAINT chk_attachments_kind
        CHECK (attachment_kind IN ('DOCUMENT', 'IMAGE')),
    ADD CONSTRAINT chk_attachments_purpose
        CHECK (purpose IN ('LEGACY', 'STUDENT_CORRECTION', 'OFFICE_REVIEW', 'REGISTRAR_DECISION'));

CREATE INDEX idx_attachments_step_id ON attachments (clearance_step_id);
CREATE INDEX idx_attachments_uploaded_by_id ON attachments (uploaded_by_id);

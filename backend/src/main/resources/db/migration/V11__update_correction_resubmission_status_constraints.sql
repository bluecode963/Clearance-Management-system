ALTER TABLE clearance_requests
    DROP CONSTRAINT IF EXISTS chk_clearance_requests_status;

ALTER TABLE clearance_requests
    ADD CONSTRAINT chk_clearance_requests_status
        CHECK (status IN (
            'PENDING',
            'IN_REVIEW',
            'NEEDS_CORRECTION',
            'READY_FOR_REGISTRAR',
            'COMPLETED',
            'REJECTED',
            'CANCELLED'
        ));

ALTER TABLE clearance_steps
    DROP CONSTRAINT IF EXISTS chk_clearance_steps_status;

ALTER TABLE clearance_steps
    ADD CONSTRAINT chk_clearance_steps_status
        CHECK (status IN (
            'PENDING',
            'APPROVED',
            'NEEDS_CORRECTION',
            'RESUBMITTED',
            'REJECTED',
            'WAITING'
        ));

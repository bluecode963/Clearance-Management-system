ALTER TABLE users
ADD COLUMN office_id BIGINT;

ALTER TABLE users
ADD CONSTRAINT fk_users_office
FOREIGN KEY (office_id) REFERENCES offices (id);

CREATE INDEX idx_users_office_id ON users (office_id);

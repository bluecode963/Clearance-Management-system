-- Local development/demo bootstrap admin.
-- Email: admin@test.com
-- Password: admin123
-- The password value below is BCrypt-hashed and the insert is guarded to avoid duplicates.
INSERT INTO users (full_name, email, password_hash, role, active)
SELECT
    'System Admin',
    'admin@test.com',
    '$2a$10$R7YitHZGQ7JS1d4sC33NgOS1pQhvB9rXTeU.CqtvZRIJQ4F7c06dS',
    'ADMIN',
    TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@test.com'
);

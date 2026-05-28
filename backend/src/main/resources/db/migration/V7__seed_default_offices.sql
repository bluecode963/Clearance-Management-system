INSERT INTO offices (office_name, description, active)
VALUES
    ('Library', 'Library clearance office', TRUE),
    ('Finance', 'Finance clearance office', TRUE),
    ('Department', 'Academic department clearance office', TRUE),
    ('Dormitory', 'Dormitory clearance office', TRUE),
    ('Registrar', 'Registrar final clearance office', TRUE)
ON CONFLICT (office_name) DO NOTHING;

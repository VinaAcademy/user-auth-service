-- Seed roles
INSERT INTO roles (code, name, created_date)
VALUES ('ADMIN', 'Administrator', now()),
       ('STAFF', 'Staff', now()),
       ('INSTRUCTOR', 'Instructor', now()),
       ('STUDENT', 'Student', now()) ON CONFLICT (code) DO NOTHING;

-- Seed users
INSERT INTO users (id, username, email, password, is_enabled, created_date)
VALUES ('11111111-1111-1111-1111-111111111111', 'admin', 'locn562836@gmail.com',
        '$2a$12$ovtewdIgCylwaTUnRZSnQO.XWgX72PqZTNr9QfBWbVokcHhz344Ye', true, now()),
       ('22222222-2222-2222-2222-222222222222', 'staff', 'huuloc2155@gmail.com',
        '$2a$12$kI8bGBjYM/BmlAa1x6YB0ewhXWaPnXg4KHhNTsLq2yX//HL8DnYsm', true, now()),
       ('33333333-3333-3333-3333-333333333333', 'instructor', 'linhpht263@outlook.com.vn',
        '$2a$12$hpUVyrkJTdkdjMQCrrugf.Fxra3hNFPP9lg8bVnKumDAGtYSxBA3m', true, now()),
       ('44444444-4444-4444-4444-444444444444', 'student', 'trihung987@gmail.com',
        '$2a$12$nvr8JYL34Ioax6ag43GtSOeGgH9Yo/FaOTeRu9NLZ9tgDWO9jbtL2', true, now()) ON CONFLICT (email) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT '11111111-1111-1111-1111-111111111111'::uuid, r.id
FROM roles r
WHERE r.code = 'ADMIN' OR r.code = 'STAFF' OR r.code = 'INSTRUCTOR' OR r.code = 'STUDENT'
UNION
SELECT '22222222-2222-2222-2222-222222222222'::uuid, r.id
FROM roles r
WHERE r.code = 'STAFF' OR r.code = 'INSTRUCTOR' OR r.code = 'STUDENT'
UNION
SELECT '33333333-3333-3333-3333-333333333333'::uuid, r.id
FROM roles r
WHERE r.code = 'INSTRUCTOR' OR r.code = 'STUDENT'
UNION
SELECT '44444444-4444-4444-4444-444444444444'::uuid, r.id
FROM roles r
WHERE r.code = 'STUDENT' ON CONFLICT DO NOTHING;

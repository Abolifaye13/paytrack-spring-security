
UPDATE app_users
SET password = '$2a$10$6DiXjl789cdGIGNBl.cy7eClm7uU3Mnip6WySmURXFBPgohQHVIoi'
WHERE email IN (
    'adminA@paytrack.com',
    'adminB@paytrack.com'
);

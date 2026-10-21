-- ==============================================================================
-- NEXT GO: Driver Authentication, Profile Isolation & Legacy Reconciliation
-- Target Database: Microsoft SQL Server (NEXTGO_DB)
-- ==============================================================================
-- PURPOSE:
--   1. Verify that 'users.password' column has sufficient width (VARCHAR(255))
--      for BCrypt hashed passwords (which are 60 characters long).
--   2. Provide diagnostic queries to inspect legacy unlinked driver records
--      (user_id IS NULL) and duplicate driver profiles linked to a single account.
--   3. Provide a safe, explicit manual reconciliation procedure for conflicts
--      (such as User 2 linked to both Driver #7 and Driver #8).
--   4. NEVER resets, drops, or truncates any tables.
--   5. NEVER deletes driver records or automatically merges them.
-- ==============================================================================

USE NEXTGO_DB;
GO

-- ------------------------------------------------------------------------------
-- Step 1: Ensure users.password column can store BCrypt hashes (>= 60 chars)
-- ------------------------------------------------------------------------------
IF EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'password' AND CHARACTER_MAXIMUM_LENGTH < 100
)
BEGIN
    PRINT 'Widening users.password to NVARCHAR(255) for secure BCrypt hashing...';
    ALTER TABLE users ALTER COLUMN password NVARCHAR(255) NOT NULL;
    PRINT 'users.password widened successfully.';
END
ELSE
BEGIN
    PRINT 'users.password is already sufficient for BCrypt hashing.';
END
GO

-- ------------------------------------------------------------------------------
-- Step 2: DIAGNOSTIC AUDIT QUERIES
-- Run these queries to inspect existing driver profiles and account relationships
-- ------------------------------------------------------------------------------

PRINT '=== AUDIT: Unlinked Drivers (user_id IS NULL) ===';
SELECT 
    d.id AS driver_id, 
    d.driver_name, 
    d.phone_number, 
    d.vehicle_number, 
    d.vehicle_type, 
    d.status,
    d.user_id
FROM driver d
WHERE d.user_id IS NULL;
GO

PRINT '=== AUDIT: Accounts with Multiple Driver Profiles (Conflicts) ===';
SELECT 
    u.id AS user_id, 
    u.full_name, 
    u.email, 
    COUNT(d.id) AS profile_count,
    STRING_AGG(CAST(d.id AS VARCHAR), ', ') AS conflicting_driver_ids
FROM users u
JOIN driver d ON d.user_id = u.id
GROUP BY u.id, u.full_name, u.email
HAVING COUNT(d.id) > 1;
GO

PRINT '=== AUDIT: Bookings Associated with Conflicting Drivers ===';
SELECT 
    b.id AS booking_id, 
    b.passenger_name, 
    b.pickup_location, 
    b.drop_location, 
    b.status AS booking_status, 
    b.driver_id, 
    d.driver_name, 
    d.user_id
FROM booking b
JOIN driver d ON b.driver_id = d.id
WHERE d.user_id IN (
    SELECT user_id FROM driver GROUP BY user_id HAVING COUNT(*) > 1
);
GO

-- ------------------------------------------------------------------------------
-- Step 3: SAFE MANUAL RECONCILIATION PROCEDURES
-- ------------------------------------------------------------------------------
-- DO NOT run automated scripts to arbitrarily choose or delete conflicting profiles.
-- Follow these explicit, verified steps:
--
-- Scenario A: User 2 (couple1@mail.com) has Driver #7 and Driver #8.
--   1. Check which vehicle or profile is actively driven by User 2:
--      SELECT * FROM driver WHERE id IN (7, 8);
--      SELECT * FROM booking WHERE driver_id IN (7, 8);
--
--   2. Option 1 - Reassign the second profile to its real separate user account:
--      If Driver #8 belongs to a colleague with user account ID 5:
--        UPDATE driver SET user_id = 5 WHERE id = 8;
--
--   3. Option 2 - Safely unlink the unneeded or secondary profile:
--      UPDATE driver SET user_id = NULL WHERE id = 8;
--      (The driver profile remains in the database; no booking history is lost).
--
-- Scenario B: Unlinked legacy drivers (Drivers #1, #2, #3):
--   If a driver registers a new user account (e.g., User ID 4) and requests
--   linking to their verified legacy driver record (e.g., Driver #1):
--     UPDATE driver SET user_id = 4 WHERE id = 1 AND user_id IS NULL;
-- ------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------
-- Step 4: OPTIONAL UNIQUE CONSTRAINT (Run ONLY after all conflicts are resolved)
-- ------------------------------------------------------------------------------
-- After running Step 2 and verifying 0 conflicts exist, you can optionally enforce
-- a filtered unique index so each user can have at most one linked driver profile:
/*
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_driver_user_id')
BEGIN
    IF NOT EXISTS (
        SELECT user_id FROM driver WHERE user_id IS NOT NULL GROUP BY user_id HAVING COUNT(*) > 1
    )
    BEGIN
        PRINT 'Creating unique filtered index UQ_driver_user_id on driver(user_id)...';
        CREATE UNIQUE NONCLUSTERED INDEX UQ_driver_user_id 
        ON driver(user_id) 
        WHERE user_id IS NOT NULL;
        PRINT 'Index created successfully.';
    END
    ELSE
    BEGIN
        PRINT 'CANNOT create unique index: duplicate profiles still exist. Please reconcile them first.';
    END
END
GO
*/

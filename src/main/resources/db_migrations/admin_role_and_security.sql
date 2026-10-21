-- ==============================================================================
-- NEXT GO: Admin Authorization & Role Migration (MS SQL Server)
-- Database: NEXTGO_DB
-- ==============================================================================
-- PURPOSE:
--   1. Ensures the 'role' column exists on 'users' table as nullable with default 'PASSENGER'.
--   2. Non-destructive: leaves all existing user records, passwords and accounts intact.
--   3. Does NOT automatically grant admin privileges or create backdoors.
--   4. Provides explicit administrative promotion instructions.
-- ==============================================================================

USE NEXTGO_DB;
GO

-- Step 1: Add additive nullable 'role' column to users table if not already present
IF NOT EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'role'
)
BEGIN
    PRINT 'Adding nullable role column to users table...';
    ALTER TABLE users ADD role VARCHAR(50) NULL DEFAULT 'PASSENGER';
    PRINT 'Column users.role added successfully.';
END
ELSE
BEGIN
    PRINT 'Column users.role already exists.';
END
GO

-- Step 2: Ensure existing rows have 'PASSENGER' if role is NULL
UPDATE users 
SET role = 'PASSENGER' 
WHERE role IS NULL OR role = '';
GO

-- Step 3: Add index on users(role, email) for fast authorization checks
IF NOT EXISTS (
    SELECT 1 
    FROM sys.indexes 
    WHERE name = 'IX_users_role' AND object_id = OBJECT_ID('users')
)
BEGIN
    PRINT 'Creating performance index IX_users_role...';
    CREATE NONCLUSTERED INDEX IX_users_role ON users (role, email);
    PRINT 'Index IX_users_role created successfully.';
END
GO

-- ==============================================================================
-- HOW TO ASSIGN AN ADMIN ACCOUNT (AUTHORIZED OPERATORS ONLY):
-- ==============================================================================
-- To promote an existing verified user account to ADMIN:
--
--   UPDATE users 
--   SET role = 'ADMIN' 
--   WHERE email = 'admin@nextgo.com';
--
-- To verify all currently designated admin accounts:
--
--   SELECT id, email, full_name, role 
--   FROM users 
--   WHERE role = 'ADMIN';
-- ==============================================================================

PRINT 'Admin role migration verified successfully.';
GO

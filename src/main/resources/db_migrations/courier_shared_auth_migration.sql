-- =========================================================================
-- Courier Authentication Integration: Shared User Mapping Migration
-- Non-destructive additive migration for Microsoft SQL Server
-- =========================================================================

SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
GO

-- 1. Ensure shared_user_id column exists on courier_users
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('courier_users') 
      AND name = 'shared_user_id'
)
BEGIN
    ALTER TABLE courier_users ADD shared_user_id BIGINT NULL;
    PRINT 'Added shared_user_id column to courier_users.';
END
GO

-- 2. Ensure unique filtered index on shared_user_id (allows multiple legacy NULL rows in SQL Server)
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE object_id = OBJECT_ID('courier_users') 
      AND name = 'UQ_courier_users_shared_user_id'
)
BEGIN
    CREATE UNIQUE NONCLUSTERED INDEX UQ_courier_users_shared_user_id 
    ON courier_users (shared_user_id) 
    WHERE shared_user_id IS NOT NULL;
    PRINT 'Added UNIQUE filtered index UQ_courier_users_shared_user_id.';
END
GO

-- 3. Allow password_hash to be NULL for shared-login provisioned Courier profiles
IF EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('courier_users') 
      AND name = 'password_hash' 
      AND is_nullable = 0
)
BEGIN
    ALTER TABLE courier_users ALTER COLUMN password_hash NVARCHAR(255) NULL;
    PRINT 'Altered password_hash column to allow NULL.';
END
GO

-- ==============================================================================
-- NEXT GO: Passenger Booking Ownership Migration (MS SQL Server)
-- ==============================================================================
-- PURPOSE:
--   1. Ensures the 'passenger_id' foreign key column exists on 'booking' table
--      as NULLABLE to safely preserve existing legacy records.
--   2. Adds foreign key constraint referencing 'users(id)' if not already present.
--   3. Does NOT delete any bookings or drop/reset the database.
--   4. Does NOT automatically guess or reassign old legacy bookings by passenger_name.
--      Legacy bookings remain with passenger_id = NULL (hidden from ordinary history)
--      until explicitly linked.
-- ==============================================================================

USE NEXTGO_DB;
GO

-- Step 1: Add additive nullable ownership column if it doesn't already exist
IF NOT EXISTS (
    SELECT 1 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'booking' AND COLUMN_NAME = 'passenger_id'
)
BEGIN
    PRINT 'Adding nullable passenger_id column to booking table...';
    ALTER TABLE booking ADD passenger_id BIGINT NULL;
    PRINT 'passenger_id column added successfully.';
END
ELSE
BEGIN
    PRINT 'passenger_id column already exists on booking table.';
END
GO

-- Step 2: Add foreign key constraint to users(id) if not already present
IF NOT EXISTS (
    SELECT 1 
    FROM sys.foreign_keys 
    WHERE name = 'FK_booking_passenger_user'
)
BEGIN
    PRINT 'Creating foreign key constraint FK_booking_passenger_user...';
    ALTER TABLE booking 
    ADD CONSTRAINT FK_booking_passenger_user 
    FOREIGN KEY (passenger_id) REFERENCES users(id);
    PRINT 'Foreign key constraint FK_booking_passenger_user created successfully.';
END
ELSE
BEGIN
    PRINT 'Foreign key constraint FK_booking_passenger_user already exists.';
END
GO

-- ==============================================================================
-- MANUAL RECORD LINKAGE (FOR VERIFIED HISTORICAL BOOKINGS ONLY)
-- ==============================================================================
-- WARNING: Do not execute automated bulk updates matching on passengerName.
-- Only execute explicit manual assignments for booking IDs that have been 
-- individually verified with the account owner.
--
-- Example: Link verified legacy booking IDs 1, 2, and 3 to User ID 1 (Dinuja):
--
--   UPDATE booking 
--   SET passenger_id = 1 
--   WHERE id IN (1, 2, 3);
--
-- To verify unlinked legacy bookings:
--   SELECT id, passenger_name, pickup_location, drop_location, status, passenger_id 
--   FROM booking 
--   WHERE passenger_id IS NULL;
-- ==============================================================================

-- ==============================================================================
-- NEXT GO: Vehicle Rental Module Availability, Extensions & Ownership Migration
-- Database: Microsoft SQL Server (NEXTGO_DB)
-- ==============================================================================
-- PURPOSE:
--   1. Add additive, nullable columns to 'rental_bookings' for:
--      - customer_user_id: links booking to authenticated User account (NULL for legacy)
--      - daily_rate: preserves agreed daily rate at time of booking
--      - extended_from_date: tracks original return date if booking was extended
--      - extension_cost: tracks incremental charge for the extension
--      - extended_at: audit timestamp of when the extension occurred
--   2. Adds Foreign Key FK_rental_bookings_customer_user referencing users(id).
--   3. Adds performance indices for interval overlap queries and user history queries.
--   4. Fully additive and non-destructive:
--      - Does NOT drop or reset any tables.
--      - Does NOT delete any records.
--      - Leaves legacy bookings intact with NULL customer_user_id and original prices.
-- ==============================================================================

USE NEXTGO_DB;
GO

-- 1. customer_user_id (Nullable BIGINT)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'rent_bookings' AND COLUMN_NAME = 'customer_user_id'
)
BEGIN
    PRINT 'Adding nullable customer_user_id column to rent_bookings table...';
    ALTER TABLE rent_bookings ADD customer_user_id BIGINT NULL;
    PRINT 'customer_user_id column added successfully.';
END
ELSE
BEGIN
    PRINT 'customer_user_id column already exists on rent_bookings table.';
END
GO

-- 2. Foreign Key FK_rent_bookings_customer_user
IF NOT EXISTS (
    SELECT 1 FROM sys.foreign_keys 
    WHERE name = 'FK_rent_bookings_customer_user'
)
BEGIN
    PRINT 'Creating foreign key constraint FK_rent_bookings_customer_user...';
    ALTER TABLE rent_bookings 
    ADD CONSTRAINT FK_rent_bookings_customer_user 
    FOREIGN KEY (customer_user_id) REFERENCES users(id);
    PRINT 'Foreign key constraint FK_rent_bookings_customer_user created successfully.';
END
ELSE
BEGIN
    PRINT 'Foreign key constraint FK_rent_bookings_customer_user already exists.';
END
GO

-- 3. daily_rate (Nullable DECIMAL(18,2) to snapshot agreed rate)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'rent_bookings' AND COLUMN_NAME = 'daily_rate'
)
BEGIN
    PRINT 'Adding nullable daily_rate column to rent_bookings table...';
    ALTER TABLE rent_bookings ADD daily_rate DECIMAL(18,2) NULL;
    PRINT 'daily_rate column added successfully.';
END
ELSE
BEGIN
    PRINT 'daily_rate column already exists on rent_bookings table.';
END
GO

-- 4. extended_from_date (Nullable DATE)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'rent_bookings' AND COLUMN_NAME = 'extended_from_date'
)
BEGIN
    PRINT 'Adding nullable extended_from_date column to rent_bookings table...';
    ALTER TABLE rent_bookings ADD extended_from_date DATE NULL;
    PRINT 'extended_from_date column added successfully.';
END
ELSE
BEGIN
    PRINT 'extended_from_date column already exists on rent_bookings table.';
END
GO

-- 5. extension_cost (Nullable DECIMAL(18,2))
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'rent_bookings' AND COLUMN_NAME = 'extension_cost'
)
BEGIN
    PRINT 'Adding nullable extension_cost column to rent_bookings table...';
    ALTER TABLE rent_bookings ADD extension_cost DECIMAL(18,2) NULL;
    PRINT 'extension_cost column added successfully.';
END
ELSE
BEGIN
    PRINT 'extension_cost column already exists on rent_bookings table.';
END
GO

-- 6. extended_at (Nullable DATETIME2)
IF NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_NAME = 'rent_bookings' AND COLUMN_NAME = 'extended_at'
)
BEGIN
    PRINT 'Adding nullable extended_at column to rent_bookings table...';
    ALTER TABLE rent_bookings ADD extended_at DATETIME2 NULL;
    PRINT 'extended_at column added successfully.';
END
ELSE
BEGIN
    PRINT 'extended_at column already exists on rent_bookings table.';
END
GO

-- 7. Add helpful non-clustered indices for fast availability lookups and user booking lookups
IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'IX_rent_bookings_availability' AND object_id = OBJECT_ID('rent_bookings')
)
BEGIN
    PRINT 'Creating performance index IX_rent_bookings_availability...';
    CREATE NONCLUSTERED INDEX IX_rent_bookings_availability 
    ON rent_bookings (vehicle_id, booking_status, start_date, end_date);
    PRINT 'Index IX_rent_bookings_availability created successfully.';
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes 
    WHERE name = 'IX_rent_bookings_customer_user' AND object_id = OBJECT_ID('rent_bookings')
)
BEGIN
    PRINT 'Creating performance index IX_rent_bookings_customer_user...';
    CREATE NONCLUSTERED INDEX IX_rent_bookings_customer_user 
    ON rent_bookings (customer_user_id, id DESC);
    PRINT 'Index IX_rent_bookings_customer_user created successfully.';
END
GO

PRINT 'Vehicle Rental migration completed safely and non-destructively.';
GO

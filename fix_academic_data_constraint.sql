-- SQL script to fix the academic_data table constraints
-- Run this script in your PostgreSQL database to fix the constraint error

-- Step 1: Drop the existing constraint that's causing the error
ALTER TABLE academic_data DROP CONSTRAINT IF EXISTS academic_data_trimester_check;

-- Step 2: Drop any existing period constraint if it exists
ALTER TABLE academic_data DROP CONSTRAINT IF EXISTS academic_data_period_check;

-- Step 3: (Optional) Recreate the constraints with correct enum values
-- This ensures data integrity while allowing our enum values
ALTER TABLE academic_data 
    ADD CONSTRAINT academic_data_trimester_check 
    CHECK (trimester IN ('FIRST', 'SECOND', 'THIRD'));

ALTER TABLE academic_data 
    ADD CONSTRAINT academic_data_period_check 
    CHECK (period IN ('PERIOD_1', 'PERIOD_2', 'PERIOD_3', 'FINAL_SEMESTER'));


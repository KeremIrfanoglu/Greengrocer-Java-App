-- SQL Script: Update Product Unit Types
-- Run this in MySQL Workbench or command line

-- Step 1: Add unit_type column (ignore error if already exists)
ALTER TABLE ProductInfo ADD COLUMN unit_type VARCHAR(10) DEFAULT 'kg';

-- Step 2: Update products that should be sold by piece
UPDATE ProductInfo SET unit_type = 'pcs' WHERE name IN ('Milk', 'Yogurt', 'Bread', 'Croissant', 'Orange Juice', 'Cola', 'Chips', 'Cookies');

-- Step 3: Verify the changes
SELECT id, name, type, unit_type FROM ProductInfo ORDER BY type, name;

-- Add invoice_data column to OrderInfo table for storing invoice PDFs
-- Run this in MySQL

ALTER TABLE OrderInfo ADD COLUMN invoice_data LONGBLOB;

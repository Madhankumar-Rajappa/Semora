-- ============================================================================
-- CampusTrack — Optional Database Cleanup Script (Obsolete Tables)
-- ============================================================================
--
-- IMPORTANT SAFETY NOTICE:
-- 1. DO NOT RUN THIS SCRIPT WITHOUT MAKING A FULL DATABASE BACKUP FIRST.
--    Backup command:
--      mysqldump -u root -p campustrack > campustrack_backup_pre_cleanup.sql
--
-- 2. This script drops ONLY the tables exclusively created for removed modules:
--      - assignments   (Assignment & deadline tracking)
--      - records       (Certificate & document upload records)
--      - activities    (Projects, hackathons, and extracurriculars)
--      - semester_results (Legacy summary table superseded by subject_results)
--
-- 3. RETAINED AND PROTECTED TABLES:
--      - students      (Student identity and credentials)
--      - semesters     (Semester progress, numbers, academic years, status)
--      - subjects      (Coursework subjects, codes, credits, and types)
--      - assessments   (Continuous assessments and marks)
--      - subject_results (Official and calculated semester results per subject)
-- ============================================================================

USE `campustrack`;

-- Optional safety confirmation transaction
SET FOREIGN_KEY_CHECKS = 0;

-- Drop obsolete tables if they exist
DROP TABLE IF EXISTS `assignments`;
DROP TABLE IF EXISTS `records`;
DROP TABLE IF EXISTS `activities`;
DROP TABLE IF EXISTS `semester_results`;

SET FOREIGN_KEY_CHECKS = 1;

-- Verification query
SELECT TABLE_NAME, TABLE_ROWS 
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'campustrack';

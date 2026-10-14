-- ============================================================================
-- CampusTrack — Phase 2 Database Incremental Migration
-- Module: Subject Management, Marks & Assessments, and Semester Results
-- ============================================================================
-- BACKUP RECOMMENDATION:
-- Before applying this migration on production, take a full database backup:
-- mysqldump -u root -p campustrack > campustrack_backup_pre_phase2.sql
-- ============================================================================

USE `campustrack`;

-- 1. Enhance `subjects` table with subject_type, faculty_name, description, and uniqueness
-- Add subject_type column if not exists
SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'subjects' AND column_name = 'subject_type');
SET @sql := IF(@exist = 0, 'ALTER TABLE `subjects` ADD COLUMN `subject_type` VARCHAR(50) NOT NULL DEFAULT "Theory" AFTER `credits`', 'SELECT "Column subject_type exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add faculty_name column if not exists
SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'subjects' AND column_name = 'faculty_name');
SET @sql := IF(@exist = 0, 'ALTER TABLE `subjects` ADD COLUMN `faculty_name` VARCHAR(150) DEFAULT NULL AFTER `subject_type`', 'SELECT "Column faculty_name exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add description column if not exists
SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'subjects' AND column_name = 'description');
SET @sql := IF(@exist = 0, 'ALTER TABLE `subjects` ADD COLUMN `description` TEXT DEFAULT NULL AFTER `faculty_name`', 'SELECT "Column description exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Sync instructor into faculty_name
UPDATE `subjects` SET `faculty_name` = `instructor` WHERE `faculty_name` IS NULL AND `instructor` IS NOT NULL;

-- Enforce unique subject_code per semester
SET @exist := (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'subjects' AND index_name = 'uq_semester_subject_code');
SET @sql := IF(@exist = 0, 'ALTER TABLE `subjects` ADD UNIQUE KEY `uq_semester_subject_code` (`semester_id`, `subject_code`)', 'SELECT "Index uq_semester_subject_code exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


-- 2. Enhance `assessments` table with assessment_name, assessment_category, weightage_percentage
ALTER TABLE `assessments` MODIFY `assessment_type` VARCHAR(100) DEFAULT NULL;
SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'assessments' AND column_name = 'assessment_name');
SET @sql := IF(@exist = 0, 'ALTER TABLE `assessments` ADD COLUMN `assessment_name` VARCHAR(150) DEFAULT NULL AFTER `student_id`', 'SELECT "Column assessment_name exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'assessments' AND column_name = 'assessment_category');
SET @sql := IF(@exist = 0, 'ALTER TABLE `assessments` ADD COLUMN `assessment_category` VARCHAR(100) NOT NULL DEFAULT "Assignment" AFTER `assessment_name`', 'SELECT "Column assessment_category exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'assessments' AND column_name = 'weightage_percentage');
SET @sql := IF(@exist = 0, 'ALTER TABLE `assessments` ADD COLUMN `weightage_percentage` DECIMAL(5,2) DEFAULT NULL AFTER `obtained_marks`', 'SELECT "Column weightage_percentage exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @exist := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'assessments' AND column_name = 'updated_at');
SET @sql := IF(@exist = 0, 'ALTER TABLE `assessments` ADD COLUMN `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP', 'SELECT "Column updated_at exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Populate assessment_name and assessment_category from title and assessment_type
UPDATE `assessments` SET `assessment_name` = `title` WHERE `assessment_name` IS NULL AND `title` IS NOT NULL;
UPDATE `assessments` SET `assessment_category` = CASE 
    WHEN `assessment_type` = 'internal' THEN 'Internal Test 1'
    WHEN `assessment_type` = 'lab' THEN 'Laboratory Assessment'
    WHEN `assessment_type` = 'quiz' THEN 'Quiz'
    WHEN `assessment_type` = 'assignment' THEN 'Assignment'
    WHEN `assessment_type` = 'exam' THEN 'Mid-Semester Examination'
    ELSE 'Other'
END WHERE `assessment_category` = 'Assignment' AND `assessment_type` IS NOT NULL;
UPDATE `assessments` SET `weightage_percentage` = `weightage` WHERE `weightage_percentage` IS NULL AND `weightage` IS NOT NULL;


-- 3. Create `subject_results` table for Official vs Calculated Semester Results
CREATE TABLE IF NOT EXISTS `subject_results` (
  `result_id` INT AUTO_INCREMENT PRIMARY KEY,
  `subject_id` INT NOT NULL UNIQUE,
  `end_semester_marks` DECIMAL(5,2) DEFAULT NULL,
  `examination_maximum_marks` DECIMAL(5,2) DEFAULT 100.00,
  `final_marks` DECIMAL(5,2) DEFAULT NULL,
  `final_grade` VARCHAR(10) DEFAULT NULL,
  `grade_point` DECIMAL(4,2) DEFAULT NULL,
  `result_status` VARCHAR(30) NOT NULL DEFAULT 'Awaiting Result', -- 'Pass', 'Fail', 'Awaiting Result'
  `result_source` VARCHAR(20) NOT NULL DEFAULT 'MANUAL', -- 'MANUAL' or 'CALCULATED'
  `remarks` TEXT DEFAULT NULL,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`subject_id`) REFERENCES `subjects`(`id`) ON DELETE CASCADE,
  INDEX `idx_subject_results_subject` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Migrate existing completed subject grades into subject_results
INSERT IGNORE INTO `subject_results` (`subject_id`, `end_semester_marks`, `final_marks`, `final_grade`, `grade_point`, `result_status`, `result_source`)
SELECT 
    `id` AS `subject_id`,
    `final_marks` AS `end_semester_marks`,
    `final_marks`,
    `grade` AS `final_grade`,
    `grade_point`,
    IF(`grade` = 'RA' OR `grade` = 'F', 'Fail', IF(`grade` IS NOT NULL, 'Pass', 'Awaiting Result')) AS `result_status`,
    'MANUAL' AS `result_source`
FROM `subjects`
WHERE `grade` IS NOT NULL;

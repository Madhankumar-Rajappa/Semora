-- ============================================================================
-- CampusTrack — Scope Restricted Schema (Phase 1 & Phase 2 Only)
-- Retained Modules:
--   1. Phase 1: Semester Management
--   2. Phase 2: Subject Management, Marks & Semester Results
-- MySQL 8.0+ Compatible
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `campustrack` 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE `campustrack`;

-- Disable foreign key checks for clean recreation
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `subject_results`;
DROP TABLE IF EXISTS `assessments`;
DROP TABLE IF EXISTS `subjects`;
DROP TABLE IF EXISTS `semesters`;
DROP TABLE IF EXISTS `students`;
SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- Table: students
-- Primary student profile and authentication credentials
-- ----------------------------------------------------------------------------
CREATE TABLE `students` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `registration_number` VARCHAR(50) NOT NULL UNIQUE,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `department` VARCHAR(100) NOT NULL,
  `college` VARCHAR(150) NOT NULL,
  `admission_year` INT NOT NULL,
  `current_academic_year` VARCHAR(30) NOT NULL DEFAULT '2024-2025',
  `grading_scale` VARCHAR(20) NOT NULL DEFAULT '10-POINT',
  `avatar_url` VARCHAR(500) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_students_email` (`email`),
  INDEX `idx_students_reg_no` (`registration_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: semesters
-- Semester progress records from 1 through graduation
-- Primary key: id (semester_id)
-- Unique constraint on (student_id, semester_number)
-- ----------------------------------------------------------------------------
CREATE TABLE `semesters` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` INT NOT NULL,
  `semester_number` INT NOT NULL,
  `academic_year` VARCHAR(20) NOT NULL, -- e.g. "2024-2025"
  `status` VARCHAR(20) NOT NULL DEFAULT 'Active', -- 'Active' or 'Completed'
  `sgpa` DECIMAL(4,2) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uq_student_semester` (`student_id`, `semester_number`),
  INDEX `idx_semesters_student` (`student_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: subjects
-- Subjects enrolled per semester with credits, types, and faculty
-- Primary key: id (subject_id)
-- Unique constraint on (semester_id, subject_code)
-- ----------------------------------------------------------------------------
CREATE TABLE `subjects` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `semester_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `subject_code` VARCHAR(25) NOT NULL,
  `subject_name` VARCHAR(150) NOT NULL,
  `credits` INT NOT NULL DEFAULT 3,
  `subject_type` VARCHAR(50) NOT NULL DEFAULT 'Theory', -- Theory, Laboratory, Project, Other
  `faculty_name` VARCHAR(150) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `instructor` VARCHAR(100) DEFAULT NULL,
  `grade` VARCHAR(10) DEFAULT NULL, -- e.g. 'O', 'A+', 'A', 'B+', 'B', 'RA'
  `final_marks` DECIMAL(5,2) DEFAULT NULL, -- out of 100
  `grade_point` DECIMAL(4,2) DEFAULT NULL, -- e.g. 10.0, 9.0, 8.0
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`semester_id`) REFERENCES `semesters`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uq_semester_subject_code` (`semester_id`, `subject_code`),
  INDEX `idx_subjects_sem_student` (`semester_id`, `student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: assessments
-- Continuous assessment marks (Internals, Lab marks, Quizzes, Examinations)
-- Primary key: id (assessment_id)
-- ----------------------------------------------------------------------------
CREATE TABLE `assessments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `subject_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `assessment_name` VARCHAR(150) DEFAULT NULL,
  `assessment_category` VARCHAR(100) NOT NULL DEFAULT 'Assignment',
  `assessment_type` VARCHAR(100) DEFAULT NULL,
  `title` VARCHAR(120) DEFAULT NULL,
  `max_marks` DECIMAL(5,2) NOT NULL DEFAULT 100.00,
  `obtained_marks` DECIMAL(5,2) NOT NULL,
  `weightage_percentage` DECIMAL(5,2) DEFAULT NULL,
  `weightage` DECIMAL(5,2) DEFAULT 0.00,
  `assessment_date` DATE DEFAULT NULL,
  `remarks` TEXT DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`subject_id`) REFERENCES `subjects`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  INDEX `idx_assessments_subject` (`subject_id`),
  INDEX `idx_assessments_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: subject_results
-- Official vs Calculated Semester Results per subject
-- Primary key: result_id
-- ----------------------------------------------------------------------------
CREATE TABLE `subject_results` (
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

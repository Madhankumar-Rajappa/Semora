-- ============================================================================
-- CampusTrack — Personal College Life Tracker
-- Database Schema (MySQL 8.0+ compatible)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `campustrack` 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE `campustrack`;

-- Disable foreign key checks for clean recreation
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `activities`;
DROP TABLE IF EXISTS `records`;
DROP TABLE IF EXISTS `semester_results`;
DROP TABLE IF EXISTS `assignments`;
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
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `department` VARCHAR(100) NOT NULL,
  `college` VARCHAR(150) NOT NULL,
  `admission_year` INT NOT NULL,
  `grading_scale` VARCHAR(20) NOT NULL DEFAULT '10-POINT',
  `avatar_url` VARCHAR(500) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_students_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: semesters
-- Semester progress records from 1 through graduation
-- Primary key: id (semester_id auto-increment)
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
-- Granular assessment marks (Internals, Lab marks, Quizzes, Examinations)
-- Primary key: id (assessment_id)
-- ----------------------------------------------------------------------------
CREATE TABLE `assessments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `subject_id` INT NOT NULL,
  `student_id` INT NOT NULL,
  `assessment_name` VARCHAR(150) DEFAULT NULL,
  `assessment_category` VARCHAR(100) NOT NULL DEFAULT 'Assignment',
  `assessment_type` VARCHAR(50) DEFAULT NULL,
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

-- ----------------------------------------------------------------------------
-- Table: assignments
-- Assignments with priorities, due dates, statuses, and attachments
-- ----------------------------------------------------------------------------
CREATE TABLE `assignments` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` INT NOT NULL,
  `semester_id` INT NOT NULL,
  `subject_id` INT DEFAULT NULL,
  `title` VARCHAR(200) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `due_date` DATETIME NOT NULL,
  `priority` ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
  `status` ENUM('Pending', 'In Progress', 'Submitted', 'Completed') NOT NULL DEFAULT 'Pending',
  `attachment_url` VARCHAR(500) DEFAULT NULL,
  `submitted_at` DATETIME DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`semester_id`) REFERENCES `semesters`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`subject_id`) REFERENCES `subjects`(`id`) ON DELETE SET NULL,
  INDEX `idx_assignments_student_status` (`student_id`, `status`),
  INDEX `idx_assignments_due` (`due_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: semester_results
-- Official cumulative evaluation per semester (SGPA, CGPA, Credits)
-- ----------------------------------------------------------------------------
CREATE TABLE `semester_results` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` INT NOT NULL,
  `semester_id` INT NOT NULL UNIQUE,
  `total_credits` INT NOT NULL DEFAULT 0,
  `earned_credits` INT NOT NULL DEFAULT 0,
  `sgpa` DECIMAL(4,2) NOT NULL DEFAULT 0.00,
  `cgpa` DECIMAL(4,2) NOT NULL DEFAULT 0.00,
  `result_status` VARCHAR(30) NOT NULL DEFAULT 'PASS',
  `remarks` VARCHAR(255) DEFAULT NULL,
  `published_date` DATE DEFAULT NULL,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`semester_id`) REFERENCES `semesters`(`id`) ON DELETE CASCADE,
  INDEX `idx_results_student` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: records
-- Certificates, result sheets, lab records, course credentials
-- ----------------------------------------------------------------------------
CREATE TABLE `records` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` INT NOT NULL,
  `semester_id` INT DEFAULT NULL,
  `category` ENUM('certificate', 'result_sheet', 'lab_record', 'course_completion', 'id_card', 'other') NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `issuer` VARCHAR(150) DEFAULT NULL,
  `issue_date` DATE DEFAULT NULL,
  `credential_id` VARCHAR(100) DEFAULT NULL,
  `file_url` VARCHAR(500) NOT NULL,
  `file_name` VARCHAR(255) DEFAULT NULL,
  `file_size_kb` INT DEFAULT 0,
  `file_type` VARCHAR(60) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`semester_id`) REFERENCES `semesters`(`id`) ON DELETE SET NULL,
  INDEX `idx_records_student_cat` (`student_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Table: activities
-- Projects, internships, hackathons, seminars, extracurriculars
-- ----------------------------------------------------------------------------
CREATE TABLE `activities` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `student_id` INT NOT NULL,
  `semester_id` INT DEFAULT NULL,
  `category` ENUM('project', 'internship', 'hackathon', 'workshop', 'seminar', 'competition', 'sports', 'extracurricular') NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `role_or_position` VARCHAR(120) DEFAULT NULL,
  `organization` VARCHAR(150) DEFAULT NULL,
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `github_url` VARCHAR(500) DEFAULT NULL,
  `demo_url` VARCHAR(500) DEFAULT NULL,
  `certificate_url` VARCHAR(500) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `skills` VARCHAR(255) DEFAULT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`student_id`) REFERENCES `students`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`semester_id`) REFERENCES `semesters`(`id`) ON DELETE SET NULL,
  INDEX `idx_activities_student_cat` (`student_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

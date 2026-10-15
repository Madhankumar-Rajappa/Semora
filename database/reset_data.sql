-- Wipe all existing student details and records across all tables
USE `campustrack`;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `activities`;
TRUNCATE TABLE `assessments`;
TRUNCATE TABLE `assignments`;
TRUNCATE TABLE `records`;
TRUNCATE TABLE `semester_results`;
TRUNCATE TABLE `subject_results`;
TRUNCATE TABLE `subjects`;
TRUNCATE TABLE `semesters`;
TRUNCATE TABLE `students`;

SET FOREIGN_KEY_CHECKS = 1;

-- Verify 0 records remain
SELECT 'students' AS table_name, COUNT(*) AS count FROM students
UNION ALL SELECT 'semesters', COUNT(*) FROM semesters
UNION ALL SELECT 'subjects', COUNT(*) FROM subjects
UNION ALL SELECT 'subject_results', COUNT(*) FROM subject_results
UNION ALL SELECT 'assessments', COUNT(*) FROM assessments
UNION ALL SELECT 'assignments', COUNT(*) FROM assignments
UNION ALL SELECT 'activities', COUNT(*) FROM activities
UNION ALL SELECT 'records', COUNT(*) FROM records
UNION ALL SELECT 'semester_results', COUNT(*) FROM semester_results;


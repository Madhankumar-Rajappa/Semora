-- ============================================================================
-- CampusTrack — Incremental Authentication Migration
-- Adds registration_number (UNIQUE) and current_academic_year to students table.
-- ============================================================================

USE `campustrack`;

-- 1. Add registration_number if it does not exist
SET @dbname = DATABASE();
SET @tablename = "students";
SET @columnname = "registration_number";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  "SELECT 1",
  "ALTER TABLE students ADD COLUMN registration_number VARCHAR(50) NULL AFTER id;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 2. Populate default registration numbers for existing accounts (Student 1 and Student 2)
UPDATE `students` 
SET `registration_number` = CONCAT('REG-', admission_year, '-00', id)
WHERE `registration_number` IS NULL OR `registration_number` = '';

-- 3. Enforce UNIQUE constraint on registration_number if not already present
SET @constraintname = "uq_students_reg_no";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND CONSTRAINT_NAME = @constraintname
  ) > 0,
  "SELECT 1",
  "ALTER TABLE students ADD CONSTRAINT uq_students_reg_no UNIQUE (registration_number);"
));
PREPARE alterConstraint FROM @preparedStatement;
EXECUTE alterConstraint;
DEALLOCATE PREPARE alterConstraint;

-- 4. Add current_academic_year if it does not exist
SET @columnname = "current_academic_year";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  "SELECT 1",
  "ALTER TABLE students ADD COLUMN current_academic_year VARCHAR(30) NOT NULL DEFAULT '2024-2025' AFTER admission_year;"
));
PREPARE alterIfNotExists2 FROM @preparedStatement;
EXECUTE alterIfNotExists2;
DEALLOCATE PREPARE alterIfNotExists2;

-- Verify table definition
DESCRIBE `students`;
SELECT `id`, `registration_number`, `name`, `email`, `department`, `college`, `current_academic_year` FROM `students`;

-- ============================================================================
-- CampusTrack — Demo Seed Data
-- ============================================================================

USE `campustrack`;

-- Clean seed data
DELETE FROM `activities`;
DELETE FROM `records`;
DELETE FROM `semester_results`;
DELETE FROM `assignments`;
DELETE FROM `assessments`;
DELETE FROM `subjects`;
DELETE FROM `semesters`;
DELETE FROM `students`;

-- ----------------------------------------------------------------------------
-- Student 1: Alex Morgan (alex.morgan@university.edu / Password123!)
-- Password Hash using SHA-256 with salt or BCrypt compatible hash
-- ----------------------------------------------------------------------------
INSERT INTO `students` (`id`, `name`, `email`, `password_hash`, `department`, `college`, `admission_year`, `grading_scale`) 
VALUES (
  1, 
  'Alex Morgan', 
  'alex.morgan@university.edu', 
  'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', -- SHA-256 for 'Password123!'
  'Computer Science & Engineering', 
  'Apex Institute of Technology', 
  2022, 
  '10-POINT'
), (
  2,
  'Jordan Lee',
  'jordan.lee@university.edu',
  'a109e36947ad56de1dca1cc49f0ef8ac9ad9a7b1aa0df41fb3c4cb73c1ff01ea', -- SHA-256 for 'Password123!'
  'Electronics & Communication',
  'Apex Institute of Technology',
  2023,
  '10-POINT'
);

-- ----------------------------------------------------------------------------
-- Semesters for Alex Morgan (Semester 1 through Semester 5)
-- ----------------------------------------------------------------------------
INSERT INTO `semesters` (`id`, `student_id`, `semester_number`, `academic_year`, `status`, `sgpa`) VALUES
(1, 1, 1, '2022-2023 Fall', 'Completed', 8.65),
(2, 1, 2, '2022-2023 Spring', 'Completed', 8.90),
(3, 1, 3, '2023-2024 Fall', 'Completed', 9.15),
(4, 1, 4, '2023-2024 Spring', 'Completed', 9.30),
(5, 1, 5, '2024-2025 Fall', 'Active', NULL);

-- ----------------------------------------------------------------------------
-- Semesters for Jordan Lee (Student ID 2) for Multi-Student Isolation Testing
-- ----------------------------------------------------------------------------
INSERT INTO `semesters` (`id`, `student_id`, `semester_number`, `academic_year`, `status`, `sgpa`) VALUES
(6, 2, 1, '2023-2024 Fall', 'Completed', 8.40),
(7, 2, 2, '2023-2024 Spring', 'Active', NULL);

-- ----------------------------------------------------------------------------
-- Subjects for Semesters
-- ----------------------------------------------------------------------------
-- Semester 4 (Completed)
INSERT INTO `subjects` (`id`, `semester_id`, `student_id`, `subject_code`, `subject_name`, `credits`, `instructor`, `grade`, `final_marks`, `grade_point`) VALUES
(1, 4, 1, 'CS401', 'Database Management Systems', 4, 'Dr. Sarah Connor', 'A+', 92.00, 9.00),
(2, 4, 1, 'CS402', 'Design and Analysis of Algorithms', 4, 'Prof. Alan Turing', 'O', 96.50, 10.00),
(3, 4, 1, 'CS403', 'Operating Systems', 3, 'Dr. Linus Torvalds', 'A+', 89.00, 9.00),
(4, 4, 1, 'CS404', 'Computer Networks', 3, 'Prof. Vint Cerf', 'A', 84.50, 8.00),
(5, 4, 1, 'CS408', 'DBMS & OS Laboratory', 2, 'Dr. Sarah Connor', 'O', 98.00, 10.00);

-- Semester 5 (Active)
INSERT INTO `subjects` (`id`, `semester_id`, `student_id`, `subject_code`, `subject_name`, `credits`, `instructor`, `grade`, `final_marks`, `grade_point`) VALUES
(6, 5, 1, 'CS501', 'Web Technologies & Cloud Computing', 4, 'Dr. Tim Berners-Lee', NULL, NULL, NULL),
(7, 5, 1, 'CS502', 'Machine Learning & Pattern Recognition', 4, 'Dr. Andrew Ng', NULL, NULL, NULL),
(8, 5, 1, 'CS503', 'Software Engineering & Agile Methodologies', 3, 'Prof. Martin Fowler', NULL, NULL, NULL),
(9, 5, 1, 'CS504', 'Information Security & Cryptography', 3, 'Dr. Bruce Schneier', NULL, NULL, NULL),
(10, 5, 1, 'CS508', 'Web Technology Lab', 2, 'Dr. Tim Berners-Lee', NULL, NULL, NULL);

-- ----------------------------------------------------------------------------
-- Assessments for Active Semester (Semester 5)
-- ----------------------------------------------------------------------------
INSERT INTO `assessments` (`id`, `subject_id`, `student_id`, `assessment_type`, `title`, `max_marks`, `obtained_marks`, `weightage`, `assessment_date`, `remarks`) VALUES
(1, 6, 1, 'internal', 'Internal Assessment 1 (HTML/CSS/Servlets)', 50.00, 46.50, 20.00, '2024-09-15', 'Excellent architecture explanation'),
(2, 6, 1, 'quiz', 'RESTful API & JDBC Connectivity Quiz', 20.00, 19.00, 10.00, '2024-09-28', 'Perfect on HTTP status codes'),
(3, 6, 1, 'lab', 'Lab Evaluation 1: Servlet Session Management', 30.00, 29.00, 15.00, '2024-10-05', 'Flawless execution'),
(4, 7, 1, 'internal', 'Midterm Test: Supervised Learning', 50.00, 44.00, 20.00, '2024-09-18', 'Strong mathematical formulation'),
(5, 7, 1, 'assignment', 'Gradient Descent & Cost Optimization in Python', 25.00, 24.50, 10.00, '2024-10-02', 'Submitted with clean notebook'),
(6, 8, 1, 'internal', 'Cycle Test: UML Modeling & Agile Sprints', 50.00, 45.00, 20.00, '2024-09-22', 'Clean class diagrams');

-- ----------------------------------------------------------------------------
-- Assignments
-- ----------------------------------------------------------------------------
INSERT INTO `assignments` (`id`, `student_id`, `semester_id`, `subject_id`, `title`, `description`, `due_date`, `priority`, `status`, `attachment_url`) VALUES
(1, 1, 5, 6, 'CampusTrack Web App Final Lab Project', 'Develop a complete Servlets + JDBC + Vercel frontend application with full CRUD.', DATE_ADD(NOW(), INTERVAL 4 DAY), 'URGENT', 'In Progress', 'https://github.com/alexmorgan/campustrack-specs.pdf'),
(2, 1, 5, 7, 'Convolutional Neural Networks Implementation', 'Build and train a ResNet model from scratch on CIFAR-10 with data augmentation.', DATE_ADD(NOW(), INTERVAL 8 DAY), 'HIGH', 'Pending', NULL),
(3, 1, 5, 8, 'Software Requirements Specification (SRS) Document', 'Document complete IEEE 830 compliant SRS for University ERP portal.', DATE_ADD(NOW(), INTERVAL 14 DAY), 'MEDIUM', 'Pending', NULL),
(4, 1, 5, 9, 'RSA Algorithm & Digital Signatures Lab Exercise', 'Implement RSA key generation, encryption, decryption, and hash signature verification in Java.', DATE_SUB(NOW(), INTERVAL 3 DAY), 'HIGH', 'Completed', 'https://github.com/alexmorgan/crypto-rsa-lab.zip'),
(5, 1, 5, 10, 'Servlet Database Connection Pooling Benchmark', 'Benchmark HikariCP connection throughput versus basic DriverManager.', DATE_ADD(NOW(), INTERVAL 1 DAY), 'URGENT', 'In Progress', NULL);

-- ----------------------------------------------------------------------------
-- Semester Official Results
-- ----------------------------------------------------------------------------
INSERT INTO `semester_results` (`id`, `student_id`, `semester_id`, `total_credits`, `earned_credits`, `sgpa`, `cgpa`, `result_status`, `remarks`, `published_date`) VALUES
(1, 1, 1, 24, 24, 8.65, 8.65, 'FIRST CLASS WITH DISTINCTION', 'Outstanding performance in fundamentals', '2023-01-20'),
(2, 1, 2, 24, 24, 8.90, 8.78, 'FIRST CLASS WITH DISTINCTION', 'Strong improvement in programming labs', '2023-06-25'),
(3, 1, 3, 23, 23, 9.15, 8.90, 'FIRST CLASS WITH DISTINCTION', 'Top 5% in department ranking', '2024-01-22'),
(4, 1, 4, 22, 22, 9.30, 9.00, 'FIRST CLASS WITH DISTINCTION', 'Dean’s Honor List awardee', '2024-06-30');

-- ----------------------------------------------------------------------------
-- Certificates & Documents
-- ----------------------------------------------------------------------------
INSERT INTO `records` (`id`, `student_id`, `semester_id`, `category`, `title`, `issuer`, `issue_date`, `credential_id`, `file_url`, `file_name`, `file_size_kb`, `file_type`, `description`) VALUES
(1, 1, 3, 'certificate', 'AWS Certified Solutions Architect – Associate', 'Amazon Web Services', '2023-11-14', 'AWS-ARCH-98231', 'https://images.unsplash.com/photo-1523289333742-be1143f6b766?auto=format&fit=crop&w=800&q=80', 'aws-solutions-architect.pdf', 1240, 'application/pdf', 'Official credential validating scalable cloud architecture on AWS'),
(2, 1, 4, 'certificate', 'Meta Full-Stack Developer Professional Certificate', 'Coursera / Meta', '2024-03-20', 'COURSERA-MTA-7749', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80', 'meta-fullstack-cert.pdf', 850, 'application/pdf', 'Full-stack web application development certificate covering APIs and databases'),
(3, 1, 4, 'result_sheet', 'Semester 4 Official Grade Sheet', 'University Examination Branch', '2024-07-05', 'GRD-SEM4-2024-019', 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40?auto=format&fit=crop&w=800&q=80', 'grade-sheet-sem4.pdf', 2100, 'application/pdf', 'Official stamped result transcript with SGPA 9.30 and CGPA 9.00'),
(4, 1, 2, 'lab_record', 'Data Structures & Algorithms Verified Lab Record', 'Department of CSE', '2023-05-18', 'LAB-DS-2023-42', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80', 'dsa-lab-record-verified.pdf', 3400, 'application/pdf', 'Completed 30 lab experiments with professor signatures and benchmarks');

-- ----------------------------------------------------------------------------
-- Projects, Internships & Activities
-- ----------------------------------------------------------------------------
INSERT INTO `activities` (`id`, `student_id`, `semester_id`, `category`, `title`, `role_or_position`, `organization`, `start_date`, `end_date`, `github_url`, `demo_url`, `certificate_url`, `description`, `skills`) VALUES
(1, 1, 4, 'project', 'CampusTrack — College Journey Tracker', 'Lead Developer', 'Apex Tech Lab', '2024-04-01', '2024-06-15', 'https://github.com/alexmorgan/campustrack', 'https://campustrack.vercel.app', NULL, 'Full-stack student academic tracking portal with Servlets REST API, MySQL, and Vercel frontend.', 'Java, Servlets, JDBC, MySQL, HTML5, Bootstrap 5, Chart.js, Vercel'),
(2, 1, 4, 'internship', 'Summer Software Engineering Intern', 'Backend Intern', 'CloudScale Technologies', '2024-06-01', '2024-08-15', NULL, 'https://cloudscale.io', 'https://cloudscale.io/certs/alex-morgan.pdf', 'Designed microservices, optimized SQL queries reducing latency by 35%, and wrote integration tests.', 'Java, Spring Boot, MySQL, Docker, Redis, REST APIs'),
(3, 1, 3, 'hackathon', 'Smart Campus Hackathon 2023', 'Team Lead & Backend Dev', 'National Technical Conclave', '2023-10-21', '2023-10-22', 'https://github.com/alexmorgan/smart-dorm-iot', NULL, 'https://conclave.org/awards/first-place-2023', 'Built IoT-enabled smart energy management system for student dormitories. Won 1st Prize.', 'Node.js, WebSockets, Python, MQTT, Embedded C'),
(4, 1, 5, 'workshop', 'Hands-on Cloud-Native DevOps Workshop', 'Participant', 'Google Developer Student Clubs', '2024-09-07', '2024-09-08', NULL, NULL, NULL, 'Two-day intensive workshop on CI/CD pipelines, Docker containers, and Kubernetes orchestration.', 'Docker, Kubernetes, GitHub Actions, Linux');

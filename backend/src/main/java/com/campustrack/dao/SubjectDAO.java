package com.campustrack.dao;

import com.campustrack.model.AcademicSummary;
import com.campustrack.model.Subject;
import com.campustrack.model.SubjectResult;
import com.campustrack.util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    private final SubjectResultDAO subjectResultDAO = new SubjectResultDAO();

    public List<Subject> findBySemesterId(int semesterId, int studentId) throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT s.*, " +
                     "(SELECT COUNT(*) FROM assessments a WHERE a.subject_id = s.id) AS assessment_count, " +
                     "(SELECT SUM(obtained_marks) FROM assessments a WHERE a.subject_id = s.id) AS total_obtained, " +
                     "(SELECT SUM(max_marks) FROM assessments a WHERE a.subject_id = s.id) AS total_max " +
                     "FROM subjects s WHERE s.semester_id = ? AND s.student_id = ? ORDER BY s.subject_code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, semesterId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subject sub = mapRow(rs);
                    // Load associated subject result
                    sub.setResult(subjectResultDAO.findBySubjectId(sub.getId(), studentId));
                    list.add(sub);
                }
            }
        }
        return list;
    }

    public List<Subject> findAllByStudentId(int studentId) throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT s.*, " +
                     "(SELECT COUNT(*) FROM assessments a WHERE a.subject_id = s.id) AS assessment_count, " +
                     "(SELECT SUM(obtained_marks) FROM assessments a WHERE a.subject_id = s.id) AS total_obtained, " +
                     "(SELECT SUM(max_marks) FROM assessments a WHERE a.subject_id = s.id) AS total_max " +
                     "FROM subjects s WHERE s.student_id = ? ORDER BY s.semester_id DESC, s.subject_code ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subject sub = mapRow(rs);
                    sub.setResult(subjectResultDAO.findBySubjectId(sub.getId(), studentId));
                    list.add(sub);
                }
            }
        }
        return list;
    }

    public Subject findByIdAndStudentId(int id, int studentId) throws SQLException {
        String sql = "SELECT s.*, " +
                     "(SELECT COUNT(*) FROM assessments a WHERE a.subject_id = s.id) AS assessment_count, " +
                     "(SELECT SUM(obtained_marks) FROM assessments a WHERE a.subject_id = s.id) AS total_obtained, " +
                     "(SELECT SUM(max_marks) FROM assessments a WHERE a.subject_id = s.id) AS total_max " +
                     "FROM subjects s WHERE s.id = ? AND s.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Subject sub = mapRow(rs);
                    sub.setResult(subjectResultDAO.findBySubjectId(sub.getId(), studentId));
                    return sub;
                }
            }
        }
        return null;
    }

    public Subject findByCodeAndSemester(String code, int semesterId, int studentId) throws SQLException {
        String sql = "SELECT * FROM subjects WHERE UPPER(TRIM(subject_code)) = UPPER(TRIM(?)) AND semester_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, semesterId);
            ps.setInt(3, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int create(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (semester_id, student_id, subject_code, subject_name, credits, subject_type, faculty_name, description, instructor) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, subject.getSemesterId());
            ps.setInt(2, subject.getStudentId());
            ps.setString(3, subject.getSubjectCode().trim().toUpperCase());
            ps.setString(4, subject.getSubjectName().trim());
            ps.setInt(5, subject.getCredits());
            ps.setString(6, subject.getSubjectType() != null ? subject.getSubjectType() : "Theory");
            String faculty = subject.getFacultyName() != null ? subject.getFacultyName().trim() : null;
            ps.setString(7, faculty);
            ps.setString(8, subject.getDescription());
            ps.setString(9, faculty);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    subject.setId(newId);
                    subject.setSubjectId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    public boolean update(Subject subject) throws SQLException {
        String sql = "UPDATE subjects SET subject_code = ?, subject_name = ?, credits = ?, subject_type = ?, faculty_name = ?, instructor = ?, description = ? " +
                     "WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, subject.getSubjectCode().trim().toUpperCase());
            ps.setString(2, subject.getSubjectName().trim());
            ps.setInt(3, subject.getCredits());
            ps.setString(4, subject.getSubjectType() != null ? subject.getSubjectType() : "Theory");
            String faculty = subject.getFacultyName() != null ? subject.getFacultyName().trim() : null;
            ps.setString(5, faculty);
            ps.setString(6, faculty);
            ps.setString(7, subject.getDescription());
            ps.setInt(8, subject.getId());
            ps.setInt(9, subject.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int studentId) throws SQLException {
        String sql = "DELETE FROM subjects WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    public AcademicSummary getAcademicSummary(int semesterId, int studentId) throws SQLException {
        AcademicSummary summary = new AcademicSummary();
        summary.setSemesterId(semesterId);

        // Get semester details
        String semSql = "SELECT semester_number, academic_year FROM semesters WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(semSql)) {
            ps.setInt(1, semesterId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    summary.setSemesterNumber(rs.getInt("semester_number"));
                    summary.setAcademicYear(rs.getString("academic_year"));
                } else {
                    return null; // Semester not found or access denied
                }
            }
        }

        List<Subject> subjects = findBySemesterId(semesterId, studentId);
        summary.setSubjects(subjects);
        summary.setSubjectCount(subjects.size());

        int totalCredits = 0;
        int totalEarnedCredits = 0;
        int totalAssessments = 0;
        int gradedSubjectsCount = 0;
        BigDecimal sumWeightedPoints = BigDecimal.ZERO;
        int sumGradedCredits = 0;

        for (Subject sub : subjects) {
            totalCredits += sub.getCredits();
            totalAssessments += sub.getAssessmentCount();

            SubjectResult res = sub.getResult();
            BigDecimal gp = null;
            String status = null;

            if (res != null) {
                gp = res.getGradePoint();
                status = res.getResultStatus();
            } else if (sub.getGradePoint() != null) {
                gp = sub.getGradePoint();
                status = "Pass";
            }

            if (gp != null) {
                gradedSubjectsCount++;
                sumGradedCredits += sub.getCredits();
                BigDecimal subPoints = gp.multiply(BigDecimal.valueOf(sub.getCredits()));
                sumWeightedPoints = sumWeightedPoints.add(subPoints);

                if ("Pass".equalsIgnoreCase(status) || gp.compareTo(BigDecimal.ZERO) > 0) {
                    totalEarnedCredits += sub.getCredits();
                }
            }
        }

        summary.setTotalCredits(totalCredits);
        summary.setEarnedCredits(totalEarnedCredits);
        summary.setEvaluatedCredits(sumGradedCredits);
        summary.setAssessmentCount(totalAssessments);

        if (subjects.isEmpty()) {
            summary.setResultStatus("Awaiting Results");
            summary.setProvisional(true);
            summary.setSgpa(null);
        } else if (gradedSubjectsCount == 0) {
            summary.setResultStatus("Awaiting Results");
            summary.setProvisional(true);
            summary.setSgpa(null);
        } else if (gradedSubjectsCount < subjects.size()) {
            summary.setResultStatus("Partial");
            summary.setProvisional(true);
            if (sumGradedCredits > 0) {
                BigDecimal sgpa = sumWeightedPoints.divide(BigDecimal.valueOf(sumGradedCredits), 2, RoundingMode.HALF_UP);
                summary.setSgpa(sgpa);
            }
        } else {
            summary.setResultStatus("Complete");
            summary.setProvisional(false);
            if (sumGradedCredits > 0) {
                BigDecimal sgpa = sumWeightedPoints.divide(BigDecimal.valueOf(sumGradedCredits), 2, RoundingMode.HALF_UP);
                summary.setSgpa(sgpa);
            }
        }

        return summary;
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        int id = rs.getInt("id");
        s.setId(id);
        s.setSubjectId(id);
        s.setSemesterId(rs.getInt("semester_id"));
        s.setStudentId(rs.getInt("student_id"));
        s.setSubjectCode(rs.getString("subject_code"));
        s.setSubjectName(rs.getString("subject_name"));
        s.setCredits(rs.getInt("credits"));

        try {
            s.setSubjectType(rs.getString("subject_type"));
        } catch (SQLException ignored) {
            s.setSubjectType("Theory");
        }

        try {
            String fac = rs.getString("faculty_name");
            if (fac == null) fac = rs.getString("instructor");
            s.setFacultyName(fac);
            s.setInstructor(fac);
        } catch (SQLException ignored) {
            s.setFacultyName(rs.getString("instructor"));
        }

        try {
            s.setDescription(rs.getString("description"));
        } catch (SQLException ignored) {}

        s.setGrade(rs.getString("grade"));
        s.setFinalMarks(rs.getBigDecimal("final_marks"));
        s.setGradePoint(rs.getBigDecimal("grade_point"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));

        try {
            s.setAssessmentCount(rs.getInt("assessment_count"));
            s.setTotalObtainedMarks(rs.getBigDecimal("total_obtained"));
            s.setTotalMaxMarks(rs.getBigDecimal("total_max"));

            if (s.getTotalObtainedMarks() != null && s.getTotalMaxMarks() != null && s.getTotalMaxMarks().compareTo(BigDecimal.ZERO) > 0) {
                s.setMarksPercentage(s.getTotalObtainedMarks().multiply(new BigDecimal(100))
                        .divide(s.getTotalMaxMarks(), 2, RoundingMode.HALF_UP));
            }
        } catch (SQLException ignored) {}

        return s;
    }
}

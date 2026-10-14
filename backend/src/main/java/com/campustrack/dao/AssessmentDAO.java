package com.campustrack.dao;

import com.campustrack.model.Assessment;
import com.campustrack.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssessmentDAO {

    public List<Assessment> findBySubjectId(int subjectId, int studentId) throws SQLException {
        List<Assessment> list = new ArrayList<>();
        // Verify subject belongs to student
        String sql = "SELECT a.* FROM assessments a " +
                     "JOIN subjects s ON a.subject_id = s.id " +
                     "WHERE a.subject_id = ? AND s.student_id = ? " +
                     "ORDER BY a.assessment_date DESC, a.id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Assessment findByIdAndStudentId(int id, int studentId) throws SQLException {
        String sql = "SELECT a.* FROM assessments a " +
                     "JOIN subjects s ON a.subject_id = s.id " +
                     "WHERE a.id = ? AND s.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int create(Assessment assessment) throws SQLException {
        // Verify subject belongs to student
        String verifySql = "SELECT id FROM subjects WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(verifySql)) {
            ps.setInt(1, assessment.getSubjectId());
            ps.setInt(2, assessment.getStudentId());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return -1; // Subject does not belong to student
                }
            }
        }

        String sql = "INSERT INTO assessments " +
                "(subject_id, student_id, assessment_name, title, assessment_category, assessment_type, " +
                "max_marks, obtained_marks, weightage_percentage, weightage, assessment_date, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, assessment.getSubjectId());
            ps.setInt(2, assessment.getStudentId());
            String name = assessment.getAssessmentName() != null ? assessment.getAssessmentName().trim() : "Assessment";
            ps.setString(3, name);
            ps.setString(4, name);
            String category = assessment.getAssessmentCategory() != null ? assessment.getAssessmentCategory().trim() : "Assignment";
            ps.setString(5, category);
            ps.setString(6, category);

            BigDecimal maxMarks = assessment.getMaxMarks() != null ? assessment.getMaxMarks() : new BigDecimal("100.00");
            ps.setBigDecimal(7, maxMarks);
            ps.setBigDecimal(8, assessment.getObtainedMarks());

            if (assessment.getWeightagePercentage() != null) {
                ps.setBigDecimal(9, assessment.getWeightagePercentage());
                ps.setBigDecimal(10, assessment.getWeightagePercentage());
            } else {
                ps.setNull(9, Types.DECIMAL);
                ps.setBigDecimal(10, BigDecimal.ZERO);
            }

            ps.setDate(11, assessment.getAssessmentDate());
            ps.setString(12, assessment.getRemarks());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    assessment.setId(newId);
                    assessment.setAssessmentId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    public boolean update(Assessment assessment) throws SQLException {
        String sql = "UPDATE assessments a " +
                     "JOIN subjects s ON a.subject_id = s.id " +
                     "SET a.assessment_name = ?, a.title = ?, a.assessment_category = ?, a.assessment_type = ?, " +
                     "a.max_marks = ?, a.obtained_marks = ?, a.weightage_percentage = ?, a.weightage = ?, " +
                     "a.assessment_date = ?, a.remarks = ? " +
                     "WHERE a.id = ? AND s.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String name = assessment.getAssessmentName() != null ? assessment.getAssessmentName().trim() : "Assessment";
            ps.setString(1, name);
            ps.setString(2, name);
            String category = assessment.getAssessmentCategory() != null ? assessment.getAssessmentCategory().trim() : "Assignment";
            ps.setString(3, category);
            ps.setString(4, category);

            BigDecimal maxMarks = assessment.getMaxMarks() != null ? assessment.getMaxMarks() : new BigDecimal("100.00");
            ps.setBigDecimal(5, maxMarks);
            ps.setBigDecimal(6, assessment.getObtainedMarks());

            if (assessment.getWeightagePercentage() != null) {
                ps.setBigDecimal(7, assessment.getWeightagePercentage());
                ps.setBigDecimal(8, assessment.getWeightagePercentage());
            } else {
                ps.setNull(7, Types.DECIMAL);
                ps.setBigDecimal(8, BigDecimal.ZERO);
            }

            ps.setDate(9, assessment.getAssessmentDate());
            ps.setString(10, assessment.getRemarks());
            ps.setInt(11, assessment.getId());
            ps.setInt(12, assessment.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int studentId) throws SQLException {
        String sql = "DELETE a FROM assessments a " +
                     "JOIN subjects s ON a.subject_id = s.id " +
                     "WHERE a.id = ? AND s.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Assessment mapRow(ResultSet rs) throws SQLException {
        Assessment a = new Assessment();
        int id = rs.getInt("id");
        a.setId(id);
        a.setAssessmentId(id);
        a.setSubjectId(rs.getInt("subject_id"));
        a.setStudentId(rs.getInt("student_id"));

        String name = null;
        try {
            name = rs.getString("assessment_name");
        } catch (SQLException ignored) {}
        if (name == null) {
            name = rs.getString("title");
        }
        a.setAssessmentName(name);
        a.setTitle(name);

        String category = null;
        try {
            category = rs.getString("assessment_category");
        } catch (SQLException ignored) {}
        if (category == null) {
            category = rs.getString("assessment_type");
        }
        a.setAssessmentCategory(category);
        a.setAssessmentType(category);

        BigDecimal maxMarks = rs.getBigDecimal("max_marks");
        a.setMaxMarks(maxMarks);
        a.setMaximumMarks(maxMarks);
        a.setObtainedMarks(rs.getBigDecimal("obtained_marks"));

        BigDecimal weight = null;
        try {
            weight = rs.getBigDecimal("weightage_percentage");
        } catch (SQLException ignored) {}
        if (weight == null) {
            weight = rs.getBigDecimal("weightage");
        }
        a.setWeightagePercentage(weight);
        a.setWeightage(weight != null ? weight : BigDecimal.ZERO);

        a.setAssessmentDate(rs.getDate("assessment_date"));
        a.setRemarks(rs.getString("remarks"));
        a.setCreatedAt(rs.getTimestamp("created_at"));

        try {
            a.setUpdatedAt(rs.getTimestamp("updated_at"));
        } catch (SQLException ignored) {}

        return a;
    }
}

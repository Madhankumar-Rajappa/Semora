package com.campustrack.dao;

import com.campustrack.model.SubjectResult;
import com.campustrack.util.DBConnection;

import java.sql.*;

public class SubjectResultDAO {

    public SubjectResult findBySubjectId(int subjectId, int studentId) throws SQLException {
        String sql = "SELECT sr.* FROM subject_results sr " +
                     "JOIN subjects s ON sr.subject_id = s.id " +
                     "WHERE sr.subject_id = ? AND s.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean saveOrUpdate(SubjectResult result, int studentId) throws SQLException {
        // First verify that subject belongs to student
        String verifySql = "SELECT id FROM subjects WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(verifySql)) {
            ps.setInt(1, result.getSubjectId());
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false; // Not owned by student
                }
            }
        }

        String upsertSql = "INSERT INTO subject_results " +
                "(subject_id, end_semester_marks, examination_maximum_marks, final_marks, final_grade, grade_point, result_status, result_source, remarks) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "end_semester_marks = VALUES(end_semester_marks), " +
                "examination_maximum_marks = VALUES(examination_maximum_marks), " +
                "final_marks = VALUES(final_marks), " +
                "final_grade = VALUES(final_grade), " +
                "grade_point = VALUES(grade_point), " +
                "result_status = VALUES(result_status), " +
                "result_source = VALUES(result_source), " +
                "remarks = VALUES(remarks)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsertSql)) {
            ps.setInt(1, result.getSubjectId());
            if (result.getEndSemesterMarks() != null) {
                ps.setBigDecimal(2, result.getEndSemesterMarks());
            } else {
                ps.setNull(2, Types.DECIMAL);
            }
            ps.setBigDecimal(3, result.getExaminationMaximumMarks() != null ? result.getExaminationMaximumMarks() : new java.math.BigDecimal("100.00"));
            if (result.getFinalMarks() != null) {
                ps.setBigDecimal(4, result.getFinalMarks());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.setString(5, result.getFinalGrade());
            if (result.getGradePoint() != null) {
                ps.setBigDecimal(6, result.getGradePoint());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setString(7, result.getResultStatus() != null ? result.getResultStatus() : "Awaiting Result");
            ps.setString(8, result.getResultSource() != null ? result.getResultSource() : "MANUAL");
            ps.setString(9, result.getRemarks());

            int rows = ps.executeUpdate();

            // Also keep subject cache columns synchronized
            syncSubjectGradeCache(result, studentId);

            return rows > 0;
        }
    }

    private void syncSubjectGradeCache(SubjectResult result, int studentId) {
        String updateSubjectSql = "UPDATE subjects SET grade = ?, final_marks = ?, grade_point = ? WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSubjectSql)) {
            ps.setString(1, result.getFinalGrade());
            if (result.getFinalMarks() != null) {
                ps.setBigDecimal(2, result.getFinalMarks());
            } else {
                ps.setNull(2, Types.DECIMAL);
            }
            if (result.getGradePoint() != null) {
                ps.setBigDecimal(3, result.getGradePoint());
            } else {
                ps.setNull(3, Types.DECIMAL);
            }
            ps.setInt(4, result.getSubjectId());
            ps.setInt(5, studentId);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private SubjectResult mapRow(ResultSet rs) throws SQLException {
        SubjectResult r = new SubjectResult();
        r.setResultId(rs.getInt("result_id"));
        r.setId(rs.getInt("result_id"));
        r.setSubjectId(rs.getInt("subject_id"));
        r.setEndSemesterMarks(rs.getBigDecimal("end_semester_marks"));
        r.setExaminationMaximumMarks(rs.getBigDecimal("examination_maximum_marks"));
        r.setFinalMarks(rs.getBigDecimal("final_marks"));
        r.setFinalGrade(rs.getString("final_grade"));
        r.setGradePoint(rs.getBigDecimal("grade_point"));
        r.setResultStatus(rs.getString("result_status"));
        r.setResultSource(rs.getString("result_source"));
        r.setRemarks(rs.getString("remarks"));
        r.setUpdatedAt(rs.getTimestamp("updated_at"));
        return r;
    }
}

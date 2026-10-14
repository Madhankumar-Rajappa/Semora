package com.campustrack.dao;

import com.campustrack.model.Semester;
import com.campustrack.util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SemesterDAO {

    public List<Semester> findAllByStudentId(int studentId) throws SQLException {
        List<Semester> list = new ArrayList<>();
        String sql = "SELECT id, student_id, semester_number, academic_year, status, sgpa, created_at, updated_at " +
                     "FROM semesters WHERE student_id = ? ORDER BY semester_number ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Semester findByIdAndStudentId(int id, int studentId) throws SQLException {
        String sql = "SELECT id, student_id, semester_number, academic_year, status, sgpa, created_at, updated_at " +
                     "FROM semesters WHERE id = ? AND student_id = ?";
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

    public Semester findBySemesterNumberAndStudentId(int semesterNumber, int studentId) throws SQLException {
        String sql = "SELECT id, student_id, semester_number, academic_year, status, sgpa, created_at, updated_at " +
                     "FROM semesters WHERE semester_number = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, semesterNumber);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int create(Semester semester) throws SQLException {
        String sql = "INSERT INTO semesters (student_id, semester_number, academic_year, status, sgpa) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, semester.getStudentId());
            ps.setInt(2, semester.getSemesterNumber());
            ps.setString(3, semester.getAcademicYear());

            String status = semester.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "Active";
            } else if ("active".equalsIgnoreCase(status)) {
                status = "Active";
            } else if ("completed".equalsIgnoreCase(status)) {
                status = "Completed";
            }
            ps.setString(4, status);

            if (semester.getSgpa() != null) {
                ps.setBigDecimal(5, semester.getSgpa());
            } else {
                ps.setNull(5, Types.DECIMAL);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean update(Semester semester) throws SQLException {
        String sql = "UPDATE semesters SET semester_number = ?, academic_year = ?, status = ?, sgpa = ? WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, semester.getSemesterNumber());
            ps.setString(2, semester.getAcademicYear());

            String status = semester.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "Active";
            } else if ("active".equalsIgnoreCase(status)) {
                status = "Active";
            } else if ("completed".equalsIgnoreCase(status)) {
                status = "Completed";
            }
            ps.setString(3, status);

            if (semester.getSgpa() != null) {
                ps.setBigDecimal(4, semester.getSgpa());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }
            ps.setInt(5, semester.getId());
            ps.setInt(6, semester.getStudentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, int studentId, String status) throws SQLException {
        String sql = "UPDATE semesters SET status = ? WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String normalizedStatus = "Active";
            if ("completed".equalsIgnoreCase(status)) {
                normalizedStatus = "Completed";
            } else if ("active".equalsIgnoreCase(status)) {
                normalizedStatus = "Active";
            } else if (status != null && !status.trim().isEmpty()) {
                normalizedStatus = status.trim();
            }
            ps.setString(1, normalizedStatus);
            ps.setInt(2, id);
            ps.setInt(3, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int studentId) throws SQLException {
        String sql = "DELETE FROM semesters WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    public void recalculateSgpa(int semesterId, int studentId) throws SQLException {
        String sql = "SELECT SUM(credits * grade_point) as total_points, SUM(credits) as total_credits " +
                     "FROM subjects WHERE semester_id = ? AND student_id = ? AND grade_point IS NOT NULL";
        BigDecimal calculatedSgpa = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, semesterId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal totalPoints = rs.getBigDecimal("total_points");
                    int totalCredits = rs.getInt("total_credits");
                    if (totalPoints != null && totalCredits > 0) {
                        calculatedSgpa = totalPoints.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
                    }
                }
            }
        }

        if (calculatedSgpa != null) {
            String updateSql = "UPDATE semesters SET sgpa = ? WHERE id = ? AND student_id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, calculatedSgpa);
                ps.setInt(2, semesterId);
                ps.setInt(3, studentId);
                ps.executeUpdate();
            }
        }
    }

    private Semester mapRow(ResultSet rs) throws SQLException {
        Semester s = new Semester();
        int id = rs.getInt("id");
        s.setId(id);
        s.setSemesterId(id);
        s.setStudentId(rs.getInt("student_id"));
        int semNum = rs.getInt("semester_number");
        s.setSemesterNumber(semNum);
        s.setSemesterName("Semester " + semNum);
        s.setAcademicYear(rs.getString("academic_year"));
        s.setStatus(rs.getString("status"));
        s.setSgpa(rs.getBigDecimal("sgpa"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}

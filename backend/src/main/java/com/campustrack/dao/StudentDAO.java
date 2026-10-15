package com.campustrack.dao;

import com.campustrack.model.Student;
import com.campustrack.util.DBConnection;

import java.sql.*;

public class StudentDAO {

    public Student findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM students WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, true);
                }
            }
        }
        return null;
    }

    public Student findByRegistrationNumber(String regNo) throws SQLException {
        if (regNo == null || regNo.trim().isEmpty()) return null;
        String sql = "SELECT * FROM students WHERE registration_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, false);
                }
            }
        }
        return null;
    }

    public Student findById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs, false);
                }
            }
        }
        return null;
    }

    public int create(Student student) throws SQLException {
        String sql = "INSERT INTO students (name, email, password_hash, department, college, admission_year, grading_scale, registration_number, current_academic_year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, student.getName() != null ? student.getName().trim() : "");
            ps.setString(2, student.getEmail() != null ? student.getEmail().trim().toLowerCase() : "");
            ps.setString(3, student.getPasswordHash());
            ps.setString(4, student.getDepartment() != null ? student.getDepartment().trim() : "");
            ps.setString(5, student.getCollege() != null ? student.getCollege().trim() : "");
            ps.setInt(6, student.getAdmissionYear());
            ps.setString(7, student.getGradingScale() != null ? student.getGradingScale() : "10-POINT");
            ps.setString(8, student.getRegistrationNumber() != null ? student.getRegistrationNumber().trim() : null);
            ps.setString(9, student.getCurrentAcademicYear() != null ? student.getCurrentAcademicYear().trim() : "2024-2025");

            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean updateProfile(Student student) throws SQLException {
        String sql = "UPDATE students SET name = ?, department = ?, college = ?, admission_year = ?, current_academic_year = ? " +
                     "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName().trim());
            ps.setString(2, student.getDepartment() != null ? student.getDepartment().trim() : "");
            ps.setString(3, student.getCollege() != null ? student.getCollege().trim() : "");
            ps.setInt(4, student.getAdmissionYear());
            ps.setString(5, student.getCurrentAcademicYear() != null ? student.getCurrentAcademicYear().trim() : "2024-2025");
            ps.setInt(6, student.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int studentId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE students SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Student mapRow(ResultSet rs, boolean includePasswordHash) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setRegistrationNumber(rs.getString("registration_number"));
        s.setName(rs.getString("name"));
        s.setEmail(rs.getString("email"));
        if (includePasswordHash) {
            s.setPasswordHash(rs.getString("password_hash"));
        }
        s.setDepartment(rs.getString("department"));
        s.setCollege(rs.getString("college"));
        s.setAdmissionYear(rs.getInt("admission_year"));
        s.setCurrentAcademicYear(rs.getString("current_academic_year"));
        s.setGradingScale(rs.getString("grading_scale"));
        s.setAvatarUrl(rs.getString("avatar_url"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}

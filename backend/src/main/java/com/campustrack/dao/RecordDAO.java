package com.campustrack.dao;

import com.campustrack.model.DocumentRecord;
import com.campustrack.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {

    public List<DocumentRecord> findAll(int studentId, Integer semesterId, String category, String search) throws SQLException {
        List<DocumentRecord> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT r.*, s.semester_number " +
            "FROM records r " +
            "LEFT JOIN semesters s ON r.semester_id = s.id " +
            "WHERE r.student_id = ? "
        );

        if (semesterId != null && semesterId > 0) {
            sql.append("AND r.semester_id = ? ");
        }
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND r.category = ? ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (r.title LIKE ? OR r.issuer LIKE ? OR r.credential_id LIKE ?) ");
        }

        sql.append("ORDER BY r.issue_date DESC, r.id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, studentId);

            if (semesterId != null && semesterId > 0) {
                ps.setInt(idx++, semesterId);
            }
            if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
                ps.setString(idx++, category.trim());
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim() + "%";
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
                ps.setString(idx++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public DocumentRecord findByIdAndStudentId(int id, int studentId) throws SQLException {
        String sql = "SELECT r.*, s.semester_number " +
                     "FROM records r " +
                     "LEFT JOIN semesters s ON r.semester_id = s.id " +
                     "WHERE r.id = ? AND r.student_id = ?";
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

    public int create(DocumentRecord r) throws SQLException {
        String sql = "INSERT INTO records (student_id, semester_id, category, title, issuer, issue_date, credential_id, file_url, file_name, file_size_kb, file_type, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getStudentId());
            if (r.getSemesterId() != null && r.getSemesterId() > 0) {
                ps.setInt(2, r.getSemesterId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, r.getCategory());
            ps.setString(4, r.getTitle());
            ps.setString(5, r.getIssuer());
            ps.setDate(6, r.getIssueDate());
            ps.setString(7, r.getCredentialId());
            ps.setString(8, r.getFileUrl());
            ps.setString(9, r.getFileName());
            ps.setInt(10, r.getFileSizeKb());
            ps.setString(11, r.getFileType());
            ps.setString(12, r.getDescription());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean update(DocumentRecord r) throws SQLException {
        String sql = "UPDATE records SET semester_id = ?, category = ?, title = ?, issuer = ?, issue_date = ?, " +
                     "credential_id = ?, file_url = ?, file_name = ?, file_size_kb = ?, file_type = ?, description = ? " +
                     "WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (r.getSemesterId() != null && r.getSemesterId() > 0) {
                ps.setInt(1, r.getSemesterId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, r.getCategory());
            ps.setString(3, r.getTitle());
            ps.setString(4, r.getIssuer());
            ps.setDate(5, r.getIssueDate());
            ps.setString(6, r.getCredentialId());
            ps.setString(7, r.getFileUrl());
            ps.setString(8, r.getFileName());
            ps.setInt(9, r.getFileSizeKb());
            ps.setString(10, r.getFileType());
            ps.setString(11, r.getDescription());
            ps.setInt(12, r.getId());
            ps.setInt(13, r.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int studentId) throws SQLException {
        String sql = "DELETE FROM records WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private DocumentRecord mapRow(ResultSet rs) throws SQLException {
        DocumentRecord r = new DocumentRecord();
        r.setId(rs.getInt("id"));
        r.setStudentId(rs.getInt("student_id"));
        int semId = rs.getInt("semester_id");
        if (!rs.wasNull()) {
            r.setSemesterId(semId);
        }
        int semNum = rs.getInt("semester_number");
        if (!rs.wasNull()) {
            r.setSemesterNumber(semNum);
        }
        r.setCategory(rs.getString("category"));
        r.setTitle(rs.getString("title"));
        r.setIssuer(rs.getString("issuer"));
        r.setIssueDate(rs.getDate("issue_date"));
        r.setCredentialId(rs.getString("credential_id"));
        r.setFileUrl(rs.getString("file_url"));
        r.setFileName(rs.getString("file_name"));
        r.setFileSizeKb(rs.getInt("file_size_kb"));
        r.setFileType(rs.getString("file_type"));
        r.setDescription(rs.getString("description"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }
}

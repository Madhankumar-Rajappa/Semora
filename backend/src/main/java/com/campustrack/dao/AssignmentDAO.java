package com.campustrack.dao;

import com.campustrack.model.Assignment;
import com.campustrack.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    public List<Assignment> findAll(int studentId, Integer semesterId, String status, String priority) throws SQLException {
        List<Assignment> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, s.subject_code, s.subject_name " +
            "FROM assignments a " +
            "LEFT JOIN subjects s ON a.subject_id = s.id " +
            "WHERE a.student_id = ? "
        );

        if (semesterId != null && semesterId > 0) {
            sql.append("AND a.semester_id = ? ");
        }
        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            sql.append("AND a.status = ? ");
        }
        if (priority != null && !priority.trim().isEmpty() && !priority.equalsIgnoreCase("ALL")) {
            sql.append("AND a.priority = ? ");
        }

        sql.append("ORDER BY a.due_date ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            ps.setInt(paramIndex++, studentId);

            if (semesterId != null && semesterId > 0) {
                ps.setInt(paramIndex++, semesterId);
            }
            if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
                ps.setString(paramIndex++, status.trim());
            }
            if (priority != null && !priority.trim().isEmpty() && !priority.equalsIgnoreCase("ALL")) {
                ps.setString(paramIndex++, priority.trim().toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Assignment> findUpcoming(int studentId, int limit) throws SQLException {
        List<Assignment> list = new ArrayList<>();
        String sql = "SELECT a.*, s.subject_code, s.subject_name " +
                     "FROM assignments a " +
                     "LEFT JOIN subjects s ON a.subject_id = s.id " +
                     "WHERE a.student_id = ? AND a.status != 'Completed' " +
                     "ORDER BY a.due_date ASC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Assignment findByIdAndStudentId(int id, int studentId) throws SQLException {
        String sql = "SELECT a.*, s.subject_code, s.subject_name " +
                     "FROM assignments a " +
                     "LEFT JOIN subjects s ON a.subject_id = s.id " +
                     "WHERE a.id = ? AND a.student_id = ?";
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

    public int create(Assignment a) throws SQLException {
        String sql = "INSERT INTO assignments (student_id, semester_id, subject_id, title, description, due_date, priority, status, attachment_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getSemesterId());
            if (a.getSubjectId() != null && a.getSubjectId() > 0) {
                ps.setInt(3, a.getSubjectId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, a.getTitle());
            ps.setString(5, a.getDescription());
            ps.setTimestamp(6, a.getDueDate());
            ps.setString(7, a.getPriority() != null ? a.getPriority().toUpperCase() : "MEDIUM");
            ps.setString(8, a.getStatus() != null ? a.getStatus() : "Pending");
            ps.setString(9, a.getAttachmentUrl());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean update(Assignment a) throws SQLException {
        String sql = "UPDATE assignments SET semester_id = ?, subject_id = ?, title = ?, description = ?, " +
                     "due_date = ?, priority = ?, status = ?, attachment_url = ?, submitted_at = ? " +
                     "WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getSemesterId());
            if (a.getSubjectId() != null && a.getSubjectId() > 0) {
                ps.setInt(2, a.getSubjectId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, a.getTitle());
            ps.setString(4, a.getDescription());
            ps.setTimestamp(5, a.getDueDate());
            ps.setString(6, a.getPriority() != null ? a.getPriority().toUpperCase() : "MEDIUM");
            ps.setString(7, a.getStatus());
            ps.setString(8, a.getAttachmentUrl());

            if (a.getStatus() != null && (a.getStatus().equalsIgnoreCase("Submitted") || a.getStatus().equalsIgnoreCase("Completed"))) {
                ps.setTimestamp(9, a.getSubmittedAt() != null ? a.getSubmittedAt() : new Timestamp(System.currentTimeMillis()));
            } else {
                ps.setNull(9, Types.TIMESTAMP);
            }

            ps.setInt(10, a.getId());
            ps.setInt(11, a.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int studentId) throws SQLException {
        String sql = "DELETE FROM assignments WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Assignment mapRow(ResultSet rs) throws SQLException {
        Assignment a = new Assignment();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setSemesterId(rs.getInt("semester_id"));

        int subId = rs.getInt("subject_id");
        if (!rs.wasNull()) {
            a.setSubjectId(subId);
        }
        a.setSubjectCode(rs.getString("subject_code"));
        a.setSubjectName(rs.getString("subject_name"));

        a.setTitle(rs.getString("title"));
        a.setDescription(rs.getString("description"));
        Timestamp due = rs.getTimestamp("due_date");
        a.setDueDate(due);
        a.setPriority(rs.getString("priority"));
        a.setStatus(rs.getString("status"));
        a.setAttachmentUrl(rs.getString("attachment_url"));
        a.setSubmittedAt(rs.getTimestamp("submitted_at"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        a.setUpdatedAt(rs.getTimestamp("updated_at"));

        if (due != null) {
            long now = System.currentTimeMillis();
            boolean isCompleted = "Completed".equalsIgnoreCase(a.getStatus()) || "Submitted".equalsIgnoreCase(a.getStatus());
            a.setOverdue(!isCompleted && due.getTime() < now);
        }
        return a;
    }
}

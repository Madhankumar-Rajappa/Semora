package com.campustrack.dao;

import com.campustrack.model.Activity;
import com.campustrack.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityDAO {

    public List<Activity> findAll(int studentId, Integer semesterId, String category, String search) throws SQLException {
        List<Activity> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT a.*, s.semester_number " +
            "FROM activities a " +
            "LEFT JOIN semesters s ON a.semester_id = s.id " +
            "WHERE a.student_id = ? "
        );

        if (semesterId != null && semesterId > 0) {
            sql.append("AND a.semester_id = ? ");
        }
        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("ALL")) {
            sql.append("AND a.category = ? ");
        }
        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND (a.title LIKE ? OR a.organization LIKE ? OR a.skills LIKE ?) ");
        }

        sql.append("ORDER BY a.start_date DESC, a.id DESC");

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

    public Activity findByIdAndStudentId(int id, int studentId) throws SQLException {
        String sql = "SELECT a.*, s.semester_number " +
                     "FROM activities a " +
                     "LEFT JOIN semesters s ON a.semester_id = s.id " +
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

    public int create(Activity a) throws SQLException {
        String sql = "INSERT INTO activities (student_id, semester_id, category, title, role_or_position, organization, " +
                     "start_date, end_date, github_url, demo_url, certificate_url, description, skills) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getStudentId());
            if (a.getSemesterId() != null && a.getSemesterId() > 0) {
                ps.setInt(2, a.getSemesterId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, a.getCategory());
            ps.setString(4, a.getTitle());
            ps.setString(5, a.getRoleOrPosition());
            ps.setString(6, a.getOrganization());
            ps.setDate(7, a.getStartDate());
            ps.setDate(8, a.getEndDate());
            ps.setString(9, a.getGithubUrl());
            ps.setString(10, a.getDemoUrl());
            ps.setString(11, a.getCertificateUrl());
            ps.setString(12, a.getDescription());
            ps.setString(13, a.getSkills());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean update(Activity a) throws SQLException {
        String sql = "UPDATE activities SET semester_id = ?, category = ?, title = ?, role_or_position = ?, " +
                     "organization = ?, start_date = ?, end_date = ?, github_url = ?, demo_url = ?, " +
                     "certificate_url = ?, description = ?, skills = ? WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (a.getSemesterId() != null && a.getSemesterId() > 0) {
                ps.setInt(1, a.getSemesterId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setString(2, a.getCategory());
            ps.setString(3, a.getTitle());
            ps.setString(4, a.getRoleOrPosition());
            ps.setString(5, a.getOrganization());
            ps.setDate(6, a.getStartDate());
            ps.setDate(7, a.getEndDate());
            ps.setString(8, a.getGithubUrl());
            ps.setString(9, a.getDemoUrl());
            ps.setString(10, a.getCertificateUrl());
            ps.setString(11, a.getDescription());
            ps.setString(12, a.getSkills());
            ps.setInt(13, a.getId());
            ps.setInt(14, a.getStudentId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int studentId) throws SQLException {
        String sql = "DELETE FROM activities WHERE id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Activity mapRow(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setId(rs.getInt("id"));
        a.setStudentId(rs.getInt("student_id"));
        int semId = rs.getInt("semester_id");
        if (!rs.wasNull()) {
            a.setSemesterId(semId);
        }
        int semNum = rs.getInt("semester_number");
        if (!rs.wasNull()) {
            a.setSemesterNumber(semNum);
        }
        a.setCategory(rs.getString("category"));
        a.setTitle(rs.getString("title"));
        a.setRoleOrPosition(rs.getString("role_or_position"));
        a.setOrganization(rs.getString("organization"));
        a.setStartDate(rs.getDate("start_date"));
        a.setEndDate(rs.getDate("end_date"));
        a.setGithubUrl(rs.getString("github_url"));
        a.setDemoUrl(rs.getString("demo_url"));
        a.setCertificateUrl(rs.getString("certificate_url"));
        a.setDescription(rs.getString("description"));
        a.setSkills(rs.getString("skills"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}

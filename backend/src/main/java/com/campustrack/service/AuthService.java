package com.campustrack.service;

import com.campustrack.dao.StudentDAO;
import com.campustrack.model.Student;
import com.campustrack.util.JwtUtil;
import com.campustrack.util.PasswordUtil;
import com.google.gson.JsonObject;

import java.sql.SQLException;

public class AuthService {

    private final StudentDAO studentDAO = new StudentDAO();

    public JsonObject login(String email, String password) throws SQLException, IllegalArgumentException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        Student student = studentDAO.findByEmail(email.trim().toLowerCase());
        if (student == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!PasswordUtil.verifyPassword(password, student.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = JwtUtil.generateToken(student.getId(), student.getEmail(), student.getName());

        JsonObject result = new JsonObject();
        result.addProperty("token", token);
        result.addProperty("studentId", student.getId());
        result.addProperty("name", student.getName());
        result.addProperty("email", student.getEmail());
        result.addProperty("department", student.getDepartment());
        result.addProperty("college", student.getCollege());
        result.addProperty("admissionYear", student.getAdmissionYear());

        return result;
    }

    public JsonObject register(Student student) throws SQLException, IllegalArgumentException {
        if (student.getEmail() == null || !student.getEmail().contains("@")) {
            throw new IllegalArgumentException("Valid email address is required.");
        }
        if (student.getPassword() == null || student.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required.");
        }

        Student existing = studentDAO.findByEmail(student.getEmail());
        if (existing != null) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashed = PasswordUtil.hashPassword(student.getPassword());
        student.setPasswordHash(hashed);

        int newId = studentDAO.create(student);
        if (newId <= 0) {
            throw new SQLException("Failed to create student account.");
        }

        student.setId(newId);
        String token = JwtUtil.generateToken(newId, student.getEmail(), student.getName());

        JsonObject result = new JsonObject();
        result.addProperty("token", token);
        result.addProperty("studentId", newId);
        result.addProperty("name", student.getName());
        result.addProperty("email", student.getEmail());
        result.addProperty("department", student.getDepartment());
        result.addProperty("college", student.getCollege());
        result.addProperty("admissionYear", student.getAdmissionYear());

        return result;
    }

    public Student getProfile(int studentId) throws SQLException {
        return studentDAO.findById(studentId);
    }

    public boolean updateProfile(Student student) throws SQLException {
        return studentDAO.updateProfile(student);
    }

    public boolean changePassword(int studentId, String oldPassword, String newPassword) throws SQLException, IllegalArgumentException {
        Student student = studentDAO.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found.");
        }

        Student full = studentDAO.findByEmail(student.getEmail());
        if (!PasswordUtil.verifyPassword(oldPassword, full.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        return studentDAO.updatePassword(studentId, newHash);
    }
}

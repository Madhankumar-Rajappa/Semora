package com.campustrack.service;

import com.campustrack.dao.SemesterDAO;
import com.campustrack.dao.StudentDAO;
import com.campustrack.model.Semester;
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
        result.addProperty("id", student.getId());
        result.addProperty("registrationNumber", student.getRegistrationNumber());
        result.addProperty("name", student.getName());
        result.addProperty("fullName", student.getName());
        result.addProperty("email", student.getEmail());
        result.addProperty("department", student.getDepartment());
        result.addProperty("college", student.getCollege());
        result.addProperty("collegeName", student.getCollege());
        result.addProperty("admissionYear", student.getAdmissionYear());
        result.addProperty("currentAcademicYear", student.getCurrentAcademicYear());

        return result;
    }

    public JsonObject register(Student student) throws SQLException, IllegalArgumentException {
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (student.getRegistrationNumber() == null || student.getRegistrationNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID or registration number is required.");
        }
        if (student.getEmail() == null || !student.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new IllegalArgumentException("A valid email address is required (e.g., student@college.edu).");
        }
        if (student.getPassword() == null || student.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (student.getCollege() == null || student.getCollege().trim().isEmpty()) {
            throw new IllegalArgumentException("College or institution name is required.");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department or branch is required.");
        }
        if (student.getAdmissionYear() < 1990 || student.getAdmissionYear() > 2050) {
            throw new IllegalArgumentException("Valid admission year is required.");
        }

        // Check uniqueness of email
        Student existingEmail = studentDAO.findByEmail(student.getEmail());
        if (existingEmail != null) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        // Check uniqueness of registration number
        Student existingReg = studentDAO.findByRegistrationNumber(student.getRegistrationNumber());
        if (existingReg != null) {
            throw new IllegalArgumentException("A student with this registration number already exists.");
        }

        String hashed = PasswordUtil.hashPassword(student.getPassword());
        student.setPasswordHash(hashed);

        int newId = studentDAO.create(student);
        if (newId <= 0) {
            throw new SQLException("Failed to create student account.");
        }

        student.setId(newId);

        // Automatically initialize all 8 semesters for the registered student
        SemesterDAO semesterDAO = new SemesterDAO();
        int baseYear = student.getAdmissionYear() > 0 ? student.getAdmissionYear() : 2024;
        for (int semNum = 1; semNum <= 8; semNum++) {
            int yearOffset = (semNum - 1) / 2;
            String acadYear = (baseYear + yearOffset) + "-" + (baseYear + yearOffset + 1);
            Semester sem = new Semester();
            sem.setStudentId(newId);
            sem.setSemesterNumber(semNum);
            sem.setAcademicYear(acadYear);
            sem.setStatus("Active");
            semesterDAO.create(sem);
        }

        JsonObject result = new JsonObject();
        result.addProperty("studentId", newId);
        result.addProperty("registrationNumber", student.getRegistrationNumber());
        result.addProperty("name", student.getName());
        result.addProperty("fullName", student.getName());
        result.addProperty("email", student.getEmail());
        result.addProperty("department", student.getDepartment());
        result.addProperty("college", student.getCollege());
        result.addProperty("admissionYear", student.getAdmissionYear());
        result.addProperty("currentAcademicYear", student.getCurrentAcademicYear());

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

package com.campustrack.model;

import java.sql.Timestamp;

public class Student {
    private int id;
    private String name;
    private String email;
    private transient String password; // Not serialized to JSON
    private transient String passwordHash;
    private String department;
    private String college;
    private int admissionYear;
    private String gradingScale;
    private String avatarUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Student() {}

    public Student(int id, String name, String email, String department, String college, int admissionYear) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.college = college;
        this.admissionYear = admissionYear;
        this.gradingScale = "10-POINT";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getCollege() { return college; }
    public void setCollege(String college) { this.college = college; }

    public int getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(int admissionYear) { this.admissionYear = admissionYear; }

    public String getGradingScale() { return gradingScale; }
    public void setGradingScale(String gradingScale) { this.gradingScale = gradingScale; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

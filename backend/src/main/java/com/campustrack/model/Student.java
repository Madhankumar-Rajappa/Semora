package com.campustrack.model;

import java.sql.Timestamp;

public class Student {
    private int id;
    private int studentId; // alias
    private String registrationNumber;
    private String name;
    private String fullName; // alias
    private String email;
    private String password;
    private transient String passwordHash;
    private String department;
    private String college;
    private String collegeName; // alias
    private int admissionYear;
    private String currentAcademicYear = "2024-2025";
    private String gradingScale = "10-POINT";
    private String avatarUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Student() {}

    public Student(int id, String name, String email, String department, String college, int admissionYear) {
        this.id = id;
        this.studentId = id;
        this.name = name;
        this.fullName = name;
        this.email = email;
        this.department = department;
        this.college = college;
        this.collegeName = college;
        this.admissionYear = admissionYear;
        this.gradingScale = "10-POINT";
    }

    public int getId() { 
        return id != 0 ? id : studentId; 
    }
    public void setId(int id) { 
        this.id = id; 
        this.studentId = id; 
    }

    public int getStudentId() { 
        return studentId != 0 ? studentId : id; 
    }
    public void setStudentId(int studentId) { 
        this.studentId = studentId; 
        this.id = studentId; 
    }

    public String getRegistrationNumber() { 
        return registrationNumber; 
    }
    public void setRegistrationNumber(String registrationNumber) { 
        this.registrationNumber = registrationNumber; 
    }

    public String getName() { 
        return (name != null && !name.trim().isEmpty()) ? name : fullName; 
    }
    public void setName(String name) { 
        this.name = name; 
        this.fullName = name; 
    }

    public String getFullName() { 
        return (fullName != null && !fullName.trim().isEmpty()) ? fullName : name; 
    }
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
        this.name = fullName; 
    }

    public String getEmail() { 
        return email; 
    }
    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getPassword() { 
        return password; 
    }
    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getPasswordHash() { 
        return passwordHash; 
    }
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
    }

    public String getDepartment() { 
        return department; 
    }
    public void setDepartment(String department) { 
        this.department = department; 
    }

    public String getCollege() { 
        return (college != null && !college.trim().isEmpty()) ? college : collegeName; 
    }
    public void setCollege(String college) { 
        this.college = college; 
        this.collegeName = college; 
    }

    public String getCollegeName() { 
        return (collegeName != null && !collegeName.trim().isEmpty()) ? collegeName : college; 
    }
    public void setCollegeName(String collegeName) { 
        this.collegeName = collegeName; 
        this.college = collegeName; 
    }

    public int getAdmissionYear() { 
        return admissionYear; 
    }
    public void setAdmissionYear(int admissionYear) { 
        this.admissionYear = admissionYear; 
    }

    public String getCurrentAcademicYear() { 
        return currentAcademicYear; 
    }
    public void setCurrentAcademicYear(String currentAcademicYear) { 
        this.currentAcademicYear = currentAcademicYear; 
    }

    public String getGradingScale() { 
        return gradingScale; 
    }
    public void setGradingScale(String gradingScale) { 
        this.gradingScale = gradingScale; 
    }

    public String getAvatarUrl() { 
        return avatarUrl; 
    }
    public void setAvatarUrl(String avatarUrl) { 
        this.avatarUrl = avatarUrl; 
    }

    public Timestamp getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Timestamp createdAt) { 
        this.createdAt = createdAt; 
    }

    public Timestamp getUpdatedAt() { 
        return updatedAt; 
    }
    public void setUpdatedAt(Timestamp updatedAt) { 
        this.updatedAt = updatedAt; 
    }
}

package com.campustrack.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class Semester {
    private int id;
    private int semesterId;
    private int studentId;
    private int semesterNumber;
    private String semesterName;
    private String academicYear;
    private String status; // 'Active', 'Completed'
    private BigDecimal sgpa;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Optional relation collections for detailed queries
    private List<Subject> subjects;
    private SemesterResult result;

    public Semester() {}

    public Semester(int studentId, int semesterNumber, String academicYear, String status) {
        this.studentId = studentId;
        this.semesterNumber = semesterNumber;
        this.semesterName = "Semester " + semesterNumber;
        this.academicYear = academicYear;
        this.status = status;
    }

    public int getId() { 
        return id != 0 ? id : semesterId; 
    }
    public void setId(int id) { 
        this.id = id; 
        this.semesterId = id; 
    }

    public int getSemesterId() { 
        return semesterId != 0 ? semesterId : id; 
    }
    public void setSemesterId(int semesterId) { 
        this.semesterId = semesterId; 
        this.id = semesterId; 
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(int semesterNumber) { 
        this.semesterNumber = semesterNumber; 
        if (this.semesterName == null || this.semesterName.isEmpty()) {
            this.semesterName = "Semester " + semesterNumber;
        }
    }

    public String getSemesterName() { 
        return (semesterName != null && !semesterName.isEmpty()) ? semesterName : ("Semester " + semesterNumber); 
    }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getSgpa() { return sgpa; }
    public void setSgpa(BigDecimal sgpa) { this.sgpa = sgpa; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public List<Subject> getSubjects() { return subjects; }
    public void setSubjects(List<Subject> subjects) { this.subjects = subjects; }

    public SemesterResult getResult() { return result; }
    public void setResult(SemesterResult result) { this.result = result; }
}

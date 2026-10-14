package com.campustrack.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class Subject {
    private int id;
    private int subjectId; // alias
    private int semesterId;
    private int studentId;
    private String subjectCode;
    private String subjectName;
    private int credits = 3;
    private String subjectType = "Theory"; // Theory, Laboratory, Project, Other
    private String facultyName;
    private String instructor; // alias for facultyName
    private String description;

    // Direct result cache fields (legacy / quick summary)
    private String grade;
    private BigDecimal finalMarks;
    private BigDecimal gradePoint;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Granular assessments under this subject
    private List<Assessment> assessments;
    private SubjectResult result;

    // Summary statistics for UI
    private int assessmentCount = 0;
    private BigDecimal totalObtainedMarks;
    private BigDecimal totalMaxMarks;
    private BigDecimal marksPercentage;

    public Subject() {}

    public int getId() {
        return id != 0 ? id : subjectId;
    }

    public void setId(int id) {
        this.id = id;
        this.subjectId = id;
    }

    public int getSubjectId() {
        return subjectId != 0 ? subjectId : id;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
        this.id = subjectId;
    }

    public int getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getSubjectType() {
        return subjectType != null ? subjectType : "Theory";
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getFacultyName() {
        return (facultyName != null && !facultyName.trim().isEmpty()) ? facultyName : instructor;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
        this.instructor = facultyName;
    }

    public String getInstructor() {
        return (instructor != null && !instructor.trim().isEmpty()) ? instructor : facultyName;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
        if (this.facultyName == null) {
            this.facultyName = instructor;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public BigDecimal getFinalMarks() {
        return finalMarks;
    }

    public void setFinalMarks(BigDecimal finalMarks) {
        this.finalMarks = finalMarks;
    }

    public BigDecimal getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(BigDecimal gradePoint) {
        this.gradePoint = gradePoint;
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

    public List<Assessment> getAssessments() {
        return assessments;
    }

    public void setAssessments(List<Assessment> assessments) {
        this.assessments = assessments;
    }

    public SubjectResult getResult() {
        return result;
    }

    public void setResult(SubjectResult result) {
        this.result = result;
    }

    public int getAssessmentCount() {
        return assessmentCount;
    }

    public void setAssessmentCount(int assessmentCount) {
        this.assessmentCount = assessmentCount;
    }

    public BigDecimal getTotalObtainedMarks() {
        return totalObtainedMarks;
    }

    public void setTotalObtainedMarks(BigDecimal totalObtainedMarks) {
        this.totalObtainedMarks = totalObtainedMarks;
    }

    public BigDecimal getTotalMaxMarks() {
        return totalMaxMarks;
    }

    public void setTotalMaxMarks(BigDecimal totalMaxMarks) {
        this.totalMaxMarks = totalMaxMarks;
    }

    public BigDecimal getMarksPercentage() {
        return marksPercentage;
    }

    public void setMarksPercentage(BigDecimal marksPercentage) {
        this.marksPercentage = marksPercentage;
    }
}

package com.campustrack.model;

import java.math.BigDecimal;
import java.util.List;

public class AcademicSummary {
    private int semesterId;
    private int semesterNumber;
    private String academicYear;
    private int subjectCount = 0;
    private int totalCredits = 0;
    private int earnedCredits = 0;
    private int evaluatedCredits = 0;
    private int assessmentCount = 0;
    private String resultStatus = "Awaiting Results"; // Complete, Partial, Awaiting Results
    private BigDecimal sgpa;
    private boolean isProvisional = true;
    private List<Subject> subjects;

    public AcademicSummary() {}

    public int getSemesterId() {
        return semesterId;
    }

    public void setSemesterId(int semesterId) {
        this.semesterId = semesterId;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }

    public void setSemesterNumber(int semesterNumber) {
        this.semesterNumber = semesterNumber;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public int getSubjectCount() {
        return subjectCount;
    }

    public void setSubjectCount(int subjectCount) {
        this.subjectCount = subjectCount;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }

    public int getEarnedCredits() {
        return earnedCredits;
    }

    public void setEarnedCredits(int earnedCredits) {
        this.earnedCredits = earnedCredits;
    }

    public int getEvaluatedCredits() {
        return evaluatedCredits;
    }

    public void setEvaluatedCredits(int evaluatedCredits) {
        this.evaluatedCredits = evaluatedCredits;
    }

    public int getAssessmentCount() {
        return assessmentCount;
    }

    public void setAssessmentCount(int assessmentCount) {
        this.assessmentCount = assessmentCount;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public BigDecimal getSgpa() {
        return sgpa;
    }

    public void setSgpa(BigDecimal sgpa) {
        this.sgpa = sgpa;
    }

    public boolean isProvisional() {
        return isProvisional;
    }

    public void setProvisional(boolean provisional) {
        isProvisional = provisional;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }
}

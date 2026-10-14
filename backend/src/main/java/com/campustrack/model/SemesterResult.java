package com.campustrack.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class SemesterResult {
    private int id;
    private int studentId;
    private int semesterId;
    private int totalCredits;
    private int earnedCredits;
    private BigDecimal sgpa;
    private BigDecimal cgpa;
    private String resultStatus;
    private String remarks;
    private Date publishedDate;
    private Timestamp updatedAt;

    public SemesterResult() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSemesterId() { return semesterId; }
    public void setSemesterId(int semesterId) { this.semesterId = semesterId; }

    public int getTotalCredits() { return totalCredits; }
    public void setTotalCredits(int totalCredits) { this.totalCredits = totalCredits; }

    public int getEarnedCredits() { return earnedCredits; }
    public void setEarnedCredits(int earnedCredits) { this.earnedCredits = earnedCredits; }

    public BigDecimal getSgpa() { return sgpa; }
    public void setSgpa(BigDecimal sgpa) { this.sgpa = sgpa; }

    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Date getPublishedDate() { return publishedDate; }
    public void setPublishedDate(Date publishedDate) { this.publishedDate = publishedDate; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}

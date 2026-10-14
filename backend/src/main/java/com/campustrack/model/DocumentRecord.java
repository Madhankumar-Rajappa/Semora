package com.campustrack.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Represents an uploaded academic document, certificate, or grade sheet.
 */
public class DocumentRecord {
    private int id;
    private int studentId;
    private Integer semesterId;
    private Integer semesterNumber;
    private String category; // 'certificate', 'result_sheet', 'lab_record', 'course_completion', 'id_card', 'other'
    private String title;
    private String issuer;
    private Date issueDate;
    private String credentialId;
    private String fileUrl;
    private String fileName;
    private int fileSizeKb;
    private String fileType;
    private String description;
    private Timestamp createdAt;

    public DocumentRecord() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Integer getSemesterId() { return semesterId; }
    public void setSemesterId(Integer semesterId) { this.semesterId = semesterId; }

    public Integer getSemesterNumber() { return semesterNumber; }
    public void setSemesterNumber(Integer semesterNumber) { this.semesterNumber = semesterNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public int getFileSizeKb() { return fileSizeKb; }
    public void setFileSizeKb(int fileSizeKb) { this.fileSizeKb = fileSizeKb; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

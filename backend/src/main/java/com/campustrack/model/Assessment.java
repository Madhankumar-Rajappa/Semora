package com.campustrack.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;

public class Assessment {
    private int id;
    private int assessmentId; // alias
    private int subjectId;
    private int studentId;
    private String assessmentName;
    private String title; // alias for assessmentName
    private String assessmentCategory;
    private String assessmentType; // alias for assessmentCategory
    private BigDecimal maxMarks;
    private BigDecimal maximumMarks; // alias
    private BigDecimal obtainedMarks;
    private BigDecimal weightage;
    private BigDecimal weightagePercentage; // alias
    private Date assessmentDate;
    private String remarks;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Helper calculated percentage
    private BigDecimal percentage;

    public Assessment() {}

    public int getId() {
        return id != 0 ? id : assessmentId;
    }

    public void setId(int id) {
        this.id = id;
        this.assessmentId = id;
    }

    public int getAssessmentId() {
        return assessmentId != 0 ? assessmentId : id;
    }

    public void setAssessmentId(int assessmentId) {
        this.assessmentId = assessmentId;
        this.id = assessmentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getAssessmentName() {
        return (assessmentName != null && !assessmentName.trim().isEmpty()) ? assessmentName : title;
    }

    public void setAssessmentName(String assessmentName) {
        this.assessmentName = assessmentName;
        this.title = assessmentName;
    }

    public String getTitle() {
        return (title != null && !title.trim().isEmpty()) ? title : assessmentName;
    }

    public void setTitle(String title) {
        this.title = title;
        if (this.assessmentName == null) {
            this.assessmentName = title;
        }
    }

    public String getAssessmentCategory() {
        return (assessmentCategory != null && !assessmentCategory.trim().isEmpty()) ? assessmentCategory : assessmentType;
    }

    public void setAssessmentCategory(String assessmentCategory) {
        this.assessmentCategory = assessmentCategory;
        this.assessmentType = assessmentCategory;
    }

    public String getAssessmentType() {
        return (assessmentType != null && !assessmentType.trim().isEmpty()) ? assessmentType : assessmentCategory;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
        if (this.assessmentCategory == null) {
            this.assessmentCategory = assessmentType;
        }
    }

    public BigDecimal getMaxMarks() {
        if (maximumMarks != null) return maximumMarks;
        if (maxMarks != null) return maxMarks;
        return null;
    }

    public void setMaxMarks(BigDecimal maxMarks) {
        this.maxMarks = maxMarks;
        this.maximumMarks = maxMarks;
    }

    public BigDecimal getMaximumMarks() {
        if (maximumMarks != null) return maximumMarks;
        if (maxMarks != null) return maxMarks;
        return null;
    }

    public void setMaximumMarks(BigDecimal maximumMarks) {
        this.maximumMarks = maximumMarks;
        this.maxMarks = maximumMarks;
    }

    public BigDecimal getObtainedMarks() {
        return obtainedMarks;
    }

    public void setObtainedMarks(BigDecimal obtainedMarks) {
        this.obtainedMarks = obtainedMarks;
    }

    public BigDecimal getWeightage() {
        return weightage != null ? weightage : weightagePercentage;
    }

    public void setWeightage(BigDecimal weightage) {
        this.weightage = weightage;
        this.weightagePercentage = weightage;
    }

    public BigDecimal getWeightagePercentage() {
        return weightagePercentage != null ? weightagePercentage : weightage;
    }

    public void setWeightagePercentage(BigDecimal weightagePercentage) {
        this.weightagePercentage = weightagePercentage;
        this.weightage = weightagePercentage;
    }

    public Date getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(Date assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public BigDecimal getPercentage() {
        if (percentage != null) return percentage;
        if (obtainedMarks != null && getMaxMarks() != null && getMaxMarks().compareTo(BigDecimal.ZERO) > 0) {
            return obtainedMarks.multiply(new BigDecimal(100))
                    .divide(getMaxMarks(), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }
}

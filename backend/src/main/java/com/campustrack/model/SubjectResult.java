package com.campustrack.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SubjectResult {
    private int resultId;
    private int id; // alias for resultId
    private int subjectId;
    private BigDecimal endSemesterMarks;
    private BigDecimal examinationMaximumMarks = new BigDecimal("100.00");
    private BigDecimal finalMarks;
    private String finalGrade;
    private BigDecimal gradePoint;
    private String resultStatus = "Awaiting Result"; // 'Pass', 'Fail', 'Awaiting Result'
    private String resultSource = "MANUAL"; // 'MANUAL' or 'CALCULATED'
    private String remarks;
    private Timestamp updatedAt;

    public SubjectResult() {}

    public int getResultId() {
        return resultId != 0 ? resultId : id;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
        this.id = resultId;
    }

    public int getId() {
        return id != 0 ? id : resultId;
    }

    public void setId(int id) {
        this.id = id;
        this.resultId = id;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public BigDecimal getEndSemesterMarks() {
        return endSemesterMarks;
    }

    public void setEndSemesterMarks(BigDecimal endSemesterMarks) {
        this.endSemesterMarks = endSemesterMarks;
    }

    public BigDecimal getExaminationMaximumMarks() {
        return examinationMaximumMarks;
    }

    public void setExaminationMaximumMarks(BigDecimal examinationMaximumMarks) {
        this.examinationMaximumMarks = examinationMaximumMarks;
    }

    public BigDecimal getFinalMarks() {
        return finalMarks;
    }

    public void setFinalMarks(BigDecimal finalMarks) {
        this.finalMarks = finalMarks;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public BigDecimal getGradePoint() {
        return gradePoint;
    }

    public void setGradePoint(BigDecimal gradePoint) {
        this.gradePoint = gradePoint;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultSource() {
        return resultSource;
    }

    public void setResultSource(String resultSource) {
        this.resultSource = resultSource;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}

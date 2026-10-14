package com.campustrack.service;

import com.campustrack.dao.AssessmentDAO;
import com.campustrack.model.Assessment;

import java.sql.SQLException;
import java.util.List;

public class AssessmentService {

    private final AssessmentDAO assessmentDAO = new AssessmentDAO();

    public List<Assessment> getAssessmentsBySubject(int subjectId, int studentId) throws SQLException {
        return assessmentDAO.findBySubjectId(subjectId, studentId);
    }

    public Assessment getAssessment(int id, int studentId) throws SQLException {
        return assessmentDAO.findByIdAndStudentId(id, studentId);
    }

    public Assessment createAssessment(Assessment assessment) throws SQLException {
        int id = assessmentDAO.create(assessment);
        assessment.setId(id);
        return assessment;
    }

    public boolean updateAssessment(Assessment assessment) throws SQLException {
        return assessmentDAO.update(assessment);
    }

    public boolean deleteAssessment(int id, int studentId) throws SQLException {
        return assessmentDAO.delete(id, studentId);
    }
}

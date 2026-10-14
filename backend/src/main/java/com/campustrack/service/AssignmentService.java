package com.campustrack.service;

import com.campustrack.dao.AssignmentDAO;
import com.campustrack.model.Assignment;

import java.sql.SQLException;
import java.util.List;

public class AssignmentService {

    private final AssignmentDAO assignmentDAO = new AssignmentDAO();

    public List<Assignment> getAssignments(int studentId, Integer semesterId, String status, String priority) throws SQLException {
        return assignmentDAO.findAll(studentId, semesterId, status, priority);
    }

    public List<Assignment> getUpcoming(int studentId, int limit) throws SQLException {
        return assignmentDAO.findUpcoming(studentId, limit);
    }

    public Assignment getAssignment(int id, int studentId) throws SQLException {
        return assignmentDAO.findByIdAndStudentId(id, studentId);
    }

    public Assignment createAssignment(Assignment assignment) throws SQLException {
        int id = assignmentDAO.create(assignment);
        assignment.setId(id);
        return assignment;
    }

    public boolean updateAssignment(Assignment assignment) throws SQLException {
        return assignmentDAO.update(assignment);
    }

    public boolean deleteAssignment(int id, int studentId) throws SQLException {
        return assignmentDAO.delete(id, studentId);
    }
}

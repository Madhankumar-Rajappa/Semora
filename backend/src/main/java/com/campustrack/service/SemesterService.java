package com.campustrack.service;

import com.campustrack.dao.AssessmentDAO;
import com.campustrack.dao.SemesterDAO;
import com.campustrack.dao.SubjectDAO;
import com.campustrack.model.Semester;
import com.campustrack.model.Subject;

import java.sql.SQLException;
import java.util.List;

public class SemesterService {

    private final SemesterDAO semesterDAO = new SemesterDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();

    public List<Semester> getSemesters(int studentId) throws SQLException {
        return semesterDAO.findAllByStudentId(studentId);
    }

    public Semester getSemesterDetails(int semesterId, int studentId) throws SQLException {
        Semester sem = semesterDAO.findByIdAndStudentId(semesterId, studentId);
        if (sem == null) {
            return null;
        }

        try {
            List<Subject> subjects = subjectDAO.findBySemesterId(semesterId, studentId);
            for (Subject sub : subjects) {
                sub.setAssessments(assessmentDAO.findBySubjectId(sub.getId(), studentId));
            }
            sem.setSubjects(subjects);
        } catch (Exception ignored) {
            // In Phase 1 subjects may be empty or not yet populated
        }
        return sem;
    }

    public Semester getSemesterByNumber(int semesterNumber, int studentId) throws SQLException {
        return semesterDAO.findBySemesterNumberAndStudentId(semesterNumber, studentId);
    }

    public Semester createSemester(Semester semester) throws SQLException {
        int id = semesterDAO.create(semester);
        semester.setId(id);
        semester.setSemesterId(id);
        return semester;
    }

    public boolean updateSemester(Semester semester) throws SQLException {
        return semesterDAO.update(semester);
    }

    public boolean updateSemesterStatus(int id, int studentId, String status) throws SQLException {
        return semesterDAO.updateStatus(id, studentId, status);
    }

    public boolean deleteSemester(int id, int studentId) throws SQLException {
        return semesterDAO.delete(id, studentId);
    }

    public void recalculateSgpa(int semesterId, int studentId) throws SQLException {
        semesterDAO.recalculateSgpa(semesterId, studentId);
    }
}

package com.campustrack.service;

import com.campustrack.dao.AssessmentDAO;
import com.campustrack.dao.SemesterDAO;
import com.campustrack.dao.SubjectDAO;
import com.campustrack.dao.SubjectResultDAO;
import com.campustrack.model.AcademicSummary;
import com.campustrack.model.Subject;
import com.campustrack.model.SubjectResult;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SubjectService {

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final SemesterDAO semesterDAO = new SemesterDAO();
    private final AssessmentDAO assessmentDAO = new AssessmentDAO();
    private final SubjectResultDAO subjectResultDAO = new SubjectResultDAO();

    public List<Subject> getSubjectsBySemester(int semesterId, int studentId) throws SQLException {
        List<Subject> list = subjectDAO.findBySemesterId(semesterId, studentId);
        for (Subject s : list) {
            s.setAssessments(assessmentDAO.findBySubjectId(s.getId(), studentId));
            if (s.getResult() == null) {
                s.setResult(subjectResultDAO.findBySubjectId(s.getId(), studentId));
            }
        }
        return list;
    }

    public List<Subject> getAllSubjects(int studentId) throws SQLException {
        return subjectDAO.findAllByStudentId(studentId);
    }

    public Subject getSubject(int subjectId, int studentId) throws SQLException {
        Subject s = subjectDAO.findByIdAndStudentId(subjectId, studentId);
        if (s != null) {
            s.setAssessments(assessmentDAO.findBySubjectId(subjectId, studentId));
            s.setResult(subjectResultDAO.findBySubjectId(subjectId, studentId));
        }
        return s;
    }

    public Subject getSubjectByCode(String code, int semesterId, int studentId) throws SQLException {
        return subjectDAO.findByCodeAndSemester(code, semesterId, studentId);
    }

    public Subject createSubject(Subject subject) throws SQLException {
        int id = subjectDAO.create(subject);
        subject.setId(id);
        subject.setSubjectId(id);
        semesterDAO.recalculateSgpa(subject.getSemesterId(), subject.getStudentId());
        return subject;
    }

    public boolean updateSubject(Subject subject) throws SQLException {
        boolean updated = subjectDAO.update(subject);
        if (updated) {
            semesterDAO.recalculateSgpa(subject.getSemesterId(), subject.getStudentId());
        }
        return updated;
    }

    public boolean deleteSubject(int id, int studentId) throws SQLException {
        Subject s = subjectDAO.findByIdAndStudentId(id, studentId);
        boolean deleted = subjectDAO.delete(id, studentId);
        if (deleted && s != null) {
            semesterDAO.recalculateSgpa(s.getSemesterId(), studentId);
        }
        return deleted;
    }

    public AcademicSummary getAcademicSummary(int semesterId, int studentId) throws SQLException {
        return subjectDAO.getAcademicSummary(semesterId, studentId);
    }

    public SubjectResult getSubjectResult(int subjectId, int studentId) throws SQLException {
        return subjectResultDAO.findBySubjectId(subjectId, studentId);
    }

    public boolean saveSubjectResult(SubjectResult result, int studentId) throws SQLException {
        boolean saved = subjectResultDAO.saveOrUpdate(result, studentId);
        if (saved) {
            Subject sub = subjectDAO.findByIdAndStudentId(result.getSubjectId(), studentId);
            if (sub != null) {
                semesterDAO.recalculateSgpa(sub.getSemesterId(), studentId);
            }
        }
        return saved;
    }
}

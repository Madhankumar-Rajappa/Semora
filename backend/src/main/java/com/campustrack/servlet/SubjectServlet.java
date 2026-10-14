package com.campustrack.servlet;

import com.campustrack.model.Assessment;
import com.campustrack.model.Subject;
import com.campustrack.model.SubjectResult;
import com.campustrack.service.AssessmentService;
import com.campustrack.service.SubjectService;
import com.campustrack.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@WebServlet("/api/subjects/*")
public class SubjectServlet extends HttpServlet {

    private final SubjectService subjectService = new SubjectService();
    private final AssessmentService assessmentService = new AssessmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/".equals(pathInfo) || "".equals(pathInfo)) {
                List<Subject> all = subjectService.getAllSubjects(studentId);
                JsonUtil.sendSuccess(resp, all, "All subjects retrieved.");
                return;
            }

            String[] parts = pathInfo.split("/");
            if (parts.length < 2) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid subject endpoint.");
                return;
            }

            int subId = Integer.parseInt(parts[1]);

            // GET /api/subjects/{subjectId}/assessments
            if (parts.length >= 3 && "assessments".equalsIgnoreCase(parts[2])) {
                Subject s = subjectService.getSubject(subId, studentId);
                if (s == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or access denied.");
                    return;
                }
                List<Assessment> assessments = assessmentService.getAssessmentsBySubject(subId, studentId);
                JsonUtil.sendSuccess(resp, assessments, "Assessments for subject retrieved.");
                return;
            }

            // GET /api/subjects/{subjectId}/result
            if (parts.length >= 3 && "result".equalsIgnoreCase(parts[2])) {
                Subject s = subjectService.getSubject(subId, studentId);
                if (s == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or access denied.");
                    return;
                }
                SubjectResult res = subjectService.getSubjectResult(subId, studentId);
                if (res == null) {
                    // Return empty template with default Awaiting Result
                    res = new SubjectResult();
                    res.setSubjectId(subId);
                    res.setResultStatus("Awaiting Result");
                    res.setResultSource("MANUAL");
                }
                JsonUtil.sendSuccess(resp, res, "Subject result retrieved.");
                return;
            }

            // GET /api/subjects/{subjectId}
            Subject s = subjectService.getSubject(subId, studentId);
            if (s == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or access denied.");
                return;
            }
            JsonUtil.sendSuccess(resp, s, "Subject details retrieved.");

        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid subject ID format.");
        } catch (Exception e) {
            System.err.println("[SubjectServlet] Error in doGet: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving subject: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        String pathInfo = req.getPathInfo();
        try {
            String[] parts = (pathInfo != null) ? pathInfo.split("/") : new String[0];

            // POST /api/subjects/{subjectId}/assessments
            if (parts.length >= 3 && "assessments".equalsIgnoreCase(parts[2])) {
                int subId = Integer.parseInt(parts[1]);
                Subject s = subjectService.getSubject(subId, studentId);
                if (s == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or access denied.");
                    return;
                }

                Assessment assessment = JsonUtil.readBody(req, Assessment.class);
                if (assessment == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid payload.");
                    return;
                }

                String name = assessment.getAssessmentName();
                if (name == null || name.trim().isEmpty()) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Assessment name is required.");
                    return;
                }

                BigDecimal maxMarks = assessment.getMaxMarks();
                BigDecimal obtained = assessment.getObtainedMarks();

                if (maxMarks == null || maxMarks.compareTo(BigDecimal.ZERO) <= 0) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Maximum marks must be greater than zero.");
                    return;
                }

                if (obtained == null || obtained.compareTo(BigDecimal.ZERO) < 0) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Obtained marks cannot be negative.");
                    return;
                }

                if (obtained.compareTo(maxMarks) > 0) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Obtained marks cannot exceed maximum marks.");
                    return;
                }

                BigDecimal weight = assessment.getWeightagePercentage();
                if (weight != null && (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(new BigDecimal("100.00")) > 0)) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Weightage percentage must be between 0 and 100.");
                    return;
                }

                assessment.setSubjectId(subId);
                assessment.setStudentId(studentId);
                Assessment created = assessmentService.createAssessment(assessment);
                JsonUtil.sendCreated(resp, created, "Assessment recorded successfully.");
                return;
            }

            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");

        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid ID format.");
        } catch (Exception e) {
            System.err.println("[SubjectServlet] Error in doPost: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating assessment.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Subject ID required.");
            return;
        }

        try {
            String[] parts = pathInfo.substring(1).split("/");
            int subId = Integer.parseInt(parts[0]);

            // PUT /api/subjects/{subjectId}/result
            if (parts.length >= 2 && "result".equalsIgnoreCase(parts[1])) {
                Subject s = subjectService.getSubject(subId, studentId);
                if (s == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or access denied.");
                    return;
                }

                SubjectResult result = JsonUtil.readBody(req, SubjectResult.class);
                if (result == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Payload required.");
                    return;
                }

                // Validation
                if (result.getEndSemesterMarks() != null && result.getEndSemesterMarks().compareTo(BigDecimal.ZERO) < 0) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "End-semester marks cannot be negative.");
                    return;
                }
                if (result.getFinalMarks() != null && result.getFinalMarks().compareTo(BigDecimal.ZERO) < 0) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Final marks cannot be negative.");
                    return;
                }
                if (result.getGradePoint() != null && (result.getGradePoint().compareTo(BigDecimal.ZERO) < 0 || result.getGradePoint().compareTo(new BigDecimal("10.00")) > 0)) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Grade point must be between 0.0 and 10.0.");
                    return;
                }

                String status = result.getResultStatus();
                if (status == null || status.trim().isEmpty()) {
                    result.setResultStatus("Awaiting Result");
                }

                result.setSubjectId(subId);
                boolean saved = subjectService.saveSubjectResult(result, studentId);
                if (saved) {
                    SubjectResult updated = subjectService.getSubjectResult(subId, studentId);
                    JsonUtil.sendSuccess(resp, updated != null ? updated : result, "Subject result saved successfully.");
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Failed to update subject result.");
                }
                return;
            }

            // PUT /api/subjects/{subjectId}
            Subject existing = subjectService.getSubject(subId, studentId);
            if (existing == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or access denied.");
                return;
            }

            Subject sub = JsonUtil.readBody(req, Subject.class);
            if (sub == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Payload required.");
                return;
            }

            if (sub.getSubjectName() == null || sub.getSubjectName().trim().isEmpty()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Subject name is required.");
                return;
            }
            if (sub.getSubjectCode() == null || sub.getSubjectCode().trim().isEmpty()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Subject code is required.");
                return;
            }
            if (sub.getCredits() <= 0) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Credits must be a positive number.");
                return;
            }

            // Check duplicate code within the same semester (excluding current subject)
            Subject codeCheck = subjectService.getSubjectByCode(sub.getSubjectCode(), existing.getSemesterId(), studentId);
            if (codeCheck != null && codeCheck.getId() != subId) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, 
                        "Subject code '" + sub.getSubjectCode().trim().toUpperCase() + "' already exists in this semester.");
                return;
            }

            sub.setId(subId);
            sub.setSemesterId(existing.getSemesterId());
            sub.setStudentId(studentId);

            boolean updated = subjectService.updateSubject(sub);
            if (updated) {
                Subject reloaded = subjectService.getSubject(subId, studentId);
                JsonUtil.sendSuccess(resp, reloaded != null ? reloaded : sub, "Subject updated successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject update failed.");
            }

        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid subject ID format.");
        } catch (SQLIntegrityConstraintViolationException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, "Subject code already exists in this semester.");
        } catch (Exception e) {
            System.err.println("[SubjectServlet] Error in doPut: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating subject: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Subject ID required.");
            return;
        }

        try {
            int subId = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            Subject existing = subjectService.getSubject(subId, studentId);
            if (existing == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject not found or access denied.");
                return;
            }

            boolean deleted = subjectService.deleteSubject(subId, studentId);
            if (deleted) {
                JsonUtil.sendSuccess(resp, null, "Subject and associated records deleted successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Subject could not be deleted.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid subject ID format.");
        } catch (Exception e) {
            System.err.println("[SubjectServlet] Error in doDelete: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting subject: " + e.getMessage());
        }
    }
}

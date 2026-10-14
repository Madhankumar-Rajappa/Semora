package com.campustrack.servlet;

import com.campustrack.model.Assessment;
import com.campustrack.service.AssessmentService;
import com.campustrack.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/api/assessments/*")
public class AssessmentServlet extends HttpServlet {

    private final AssessmentService assessmentService = new AssessmentService();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Assessment ID required in path.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            Assessment existing = assessmentService.getAssessment(id, studentId);
            if (existing == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assessment not found or access denied.");
                return;
            }

            Assessment a = JsonUtil.readBody(req, Assessment.class);
            if (a == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Payload required.");
                return;
            }

            BigDecimal maxMarks = a.getMaxMarks();
            BigDecimal obtained = a.getObtainedMarks();

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

            BigDecimal weight = a.getWeightagePercentage();
            if (weight != null && (weight.compareTo(BigDecimal.ZERO) < 0 || weight.compareTo(new BigDecimal("100.00")) > 0)) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Weightage percentage must be between 0 and 100.");
                return;
            }

            a.setId(id);
            a.setStudentId(studentId);
            a.setSubjectId(existing.getSubjectId());

            boolean ok = assessmentService.updateAssessment(a);
            if (ok) {
                Assessment updated = assessmentService.getAssessment(id, studentId);
                JsonUtil.sendSuccess(resp, updated != null ? updated : a, "Assessment updated successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assessment update failed.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid assessment ID format.");
        } catch (Exception e) {
            System.err.println("[AssessmentServlet] Error in doPut: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating assessment: " + e.getMessage());
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Assessment ID required in path.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            Assessment existing = assessmentService.getAssessment(id, studentId);
            if (existing == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assessment not found or access denied.");
                return;
            }

            boolean ok = assessmentService.deleteAssessment(id, studentId);
            if (ok) {
                JsonUtil.sendSuccess(resp, null, "Assessment deleted successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assessment not found or could not be deleted.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid assessment ID format.");
        } catch (Exception e) {
            System.err.println("[AssessmentServlet] Error in doDelete: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting assessment: " + e.getMessage());
        }
    }
}

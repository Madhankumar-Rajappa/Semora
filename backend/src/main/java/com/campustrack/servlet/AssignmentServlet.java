package com.campustrack.servlet;

import com.campustrack.model.Assignment;
import com.campustrack.service.AssignmentService;
import com.campustrack.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/assignments/*")
public class AssignmentServlet extends HttpServlet {

    private final AssignmentService assignmentService = new AssignmentService();

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
                String semParam = req.getParameter("semesterId");
                Integer semesterId = (semParam != null && !semParam.isEmpty()) ? Integer.parseInt(semParam) : null;
                String status = req.getParameter("status");
                String priority = req.getParameter("priority");

                List<Assignment> list = assignmentService.getAssignments(studentId, semesterId, status, priority);
                JsonUtil.sendSuccess(resp, list, "Assignments retrieved.");
            } else if ("/upcoming".equalsIgnoreCase(pathInfo)) {
                List<Assignment> upcoming = assignmentService.getUpcoming(studentId, 5);
                JsonUtil.sendSuccess(resp, upcoming, "Upcoming assignments retrieved.");
            } else {
                int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
                Assignment a = assignmentService.getAssignment(id, studentId);
                if (a == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assignment not found.");
                    return;
                }
                JsonUtil.sendSuccess(resp, a, "Assignment details retrieved.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid number parameter.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        try {
            Assignment a = JsonUtil.readBody(req, Assignment.class);
            if (a == null || a.getTitle() == null || a.getDueDate() == null || a.getSemesterId() <= 0) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Title, semester, and due date are required.");
                return;
            }
            a.setStudentId(studentId);
            Assignment created = assignmentService.createAssignment(a);
            JsonUtil.sendCreated(resp, created, "Assignment created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating assignment: " + e.getMessage());
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Assignment ID required.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            Assignment a = JsonUtil.readBody(req, Assignment.class);
            if (a == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Payload required.");
                return;
            }
            a.setId(id);
            a.setStudentId(studentId);
            boolean ok = assignmentService.updateAssignment(a);
            if (ok) {
                JsonUtil.sendSuccess(resp, a, "Assignment updated successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assignment not found.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid assignment ID.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Assignment ID required.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            boolean ok = assignmentService.deleteAssignment(id, studentId);
            if (ok) {
                JsonUtil.sendSuccess(resp, null, "Assignment deleted successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Assignment not found.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid assignment ID.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}

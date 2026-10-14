package com.campustrack.servlet;

import com.campustrack.model.Activity;
import com.campustrack.service.ActivityService;
import com.campustrack.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/activities/*")
public class ActivityServlet extends HttpServlet {

    private final ActivityService activityService = new ActivityService();

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
                String category = req.getParameter("category");
                String search = req.getParameter("search");

                List<Activity> list = activityService.getActivities(studentId, semesterId, category, search);
                JsonUtil.sendSuccess(resp, list, "Activities retrieved.");
            } else {
                int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
                Activity a = activityService.getActivity(id, studentId);
                if (a == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Activity not found.");
                    return;
                }
                JsonUtil.sendSuccess(resp, a, "Activity details retrieved.");
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
            Activity a = JsonUtil.readBody(req, Activity.class);
            if (a == null || a.getTitle() == null || a.getCategory() == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Title and category are required.");
                return;
            }
            a.setStudentId(studentId);
            Activity created = activityService.createActivity(a);
            JsonUtil.sendCreated(resp, created, "Activity created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating activity: " + e.getMessage());
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Activity ID required.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            Activity a = JsonUtil.readBody(req, Activity.class);
            if (a == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Payload required.");
                return;
            }
            a.setId(id);
            a.setStudentId(studentId);
            boolean ok = activityService.updateActivity(a);
            if (ok) {
                JsonUtil.sendSuccess(resp, a, "Activity updated successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Activity not found.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid activity ID.");
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Activity ID required.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            boolean ok = activityService.deleteActivity(id, studentId);
            if (ok) {
                JsonUtil.sendSuccess(resp, null, "Activity deleted successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Activity not found.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid activity ID.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}

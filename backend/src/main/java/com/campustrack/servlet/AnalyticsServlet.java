package com.campustrack.servlet;

import com.campustrack.service.AnalyticsService;
import com.campustrack.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/analytics/*")
public class AnalyticsServlet extends HttpServlet {

    private final AnalyticsService analyticsService = new AnalyticsService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || "/dashboard".equalsIgnoreCase(pathInfo) || "/".equals(pathInfo) || "".equals(pathInfo)) {
                JsonObject dashboard = analyticsService.getDashboardOverview(studentId);
                JsonUtil.sendSuccess(resp, dashboard, "Analytics dashboard data retrieved successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Analytics endpoint not found: " + pathInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading analytics: " + e.getMessage());
        }
    }
}

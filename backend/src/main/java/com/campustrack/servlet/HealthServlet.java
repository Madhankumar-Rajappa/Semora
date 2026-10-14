package com.campustrack.servlet;

import com.campustrack.util.DBConnection;
import com.campustrack.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        boolean dbHealthy = DBConnection.checkConnection();
        JsonObject status = new JsonObject();
        status.addProperty("status", dbHealthy ? "UP" : "DEGRADED");
        status.addProperty("database", dbHealthy ? "CONNECTED" : "DISCONNECTED");
        status.addProperty("service", "CampusTrack Backend REST API");
        status.addProperty("javaVersion", System.getProperty("java.version"));
        status.addProperty("serverTime", System.currentTimeMillis());

        if (dbHealthy) {
            JsonUtil.sendSuccess(resp, status, "CampusTrack API service is healthy and operational.");
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "CampusTrack database is unreachable. Check credentials or connection.");
        }
    }
}

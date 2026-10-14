package com.campustrack.servlet;

import com.campustrack.model.Student;
import com.campustrack.service.AuthService;
import com.campustrack.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) pathInfo = "";

        try {
            switch (pathInfo) {
                case "/register": {
                    Student student = JsonUtil.readBody(req, Student.class);
                    if (student == null) {
                        JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid registration payload.");
                        return;
                    }
                    JsonObject result = authService.register(student);
                    JsonUtil.sendCreated(resp, result, "Student registration successful!");
                    break;
                }
                case "/login": {
                    JsonObject creds = JsonUtil.readBody(req, JsonObject.class);
                    if (creds == null || !creds.has("email") || !creds.has("password")) {
                        JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
                        return;
                    }
                    String email = creds.get("email").getAsString();
                    String password = creds.get("password").getAsString();
                    JsonObject result = authService.login(email, password);
                    JsonUtil.sendSuccess(resp, result, "Login successful!");
                    break;
                }
                case "/logout": {
                    // Stateless token client-side clearance, send acknowledgement
                    JsonObject res = new JsonObject();
                    res.addProperty("loggedOut", true);
                    JsonUtil.sendSuccess(resp, res, "Logged out successfully.");
                    break;
                }
                default:
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Auth endpoint not found: " + pathInfo);
                    break;
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if ("/me".equals(pathInfo) || "".equals(pathInfo) || pathInfo == null) {
            Integer studentId = (Integer) req.getAttribute("studentId");
            if (studentId == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
                return;
            }

            try {
                Student profile = authService.getProfile(studentId);
                if (profile == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Student profile not found.");
                    return;
                }
                JsonUtil.sendSuccess(resp, profile, "Profile retrieved successfully.");
            } catch (Exception e) {
                e.printStackTrace();
                JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
            }
        } else {
            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
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
        try {
            if ("/profile".equals(pathInfo)) {
                Student updateData = JsonUtil.readBody(req, Student.class);
                if (updateData == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Payload required.");
                    return;
                }
                updateData.setId(studentId);
                boolean ok = authService.updateProfile(updateData);
                if (ok) {
                    Student fresh = authService.getProfile(studentId);
                    JsonUtil.sendSuccess(resp, fresh, "Profile updated successfully.");
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update profile.");
                }
            } else if ("/password".equals(pathInfo)) {
                JsonObject pwdData = JsonUtil.readBody(req, JsonObject.class);
                if (pwdData == null || !pwdData.has("oldPassword") || !pwdData.has("newPassword")) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Old password and new password are required.");
                    return;
                }
                String oldPass = pwdData.get("oldPassword").getAsString();
                String newPass = pwdData.get("newPassword").getAsString();
                boolean changed = authService.changePassword(studentId, oldPass, newPass);
                if (changed) {
                    JsonUtil.sendSuccess(resp, null, "Password changed successfully.");
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed to change password.");
                }
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");
            }
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update error: " + e.getMessage());
        }
    }
}

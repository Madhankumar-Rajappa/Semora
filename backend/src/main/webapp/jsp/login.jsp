<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.campustrack.dao.StudentDAO, com.campustrack.model.Student, com.campustrack.util.PasswordUtil" %>
<%
    String error = null;
    if ("POST".equalsIgnoreCase(request.getMethod())) {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        if (email != null && password != null) {
            try {
                StudentDAO dao = new StudentDAO();
                Student s = dao.findByEmail(email);
                if (s != null && PasswordUtil.verifyPassword(password, s.getPasswordHash())) {
                    session.setAttribute("jspStudent", s);
                    response.sendRedirect("dashboard.jsp");
                    return;
                } else {
                    error = "Invalid credentials. You can use demo email alex.morgan@university.edu / Password123!";
                }
            } catch (Exception e) {
                error = "Database error: " + e.getMessage();
            }
        }
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>JSP Lab Demo — Student Login</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { background: #0f172a; color: #f8fafc; min-height: 100vh; display: flex; align-items: center; }
        .card { background: #1e293b; border: 1px solid #334155; border-radius: 1rem; }
    </style>
</head>
<body>
    <div class="container">
        <div class="row justify-content-center">
            <div class="col-md-5">
                <div class="card p-4 shadow">
                    <h3 class="fw-bold text-center text-white mb-3">JSP Demo Login</h3>
                    <p class="text-secondary text-center small mb-4">Laboratory JSP + Servlet Session Verification</p>

                    <% if (error != null) { %>
                        <div class="alert alert-danger py-2 small"><%= error %></div>
                    <% } %>

                    <form method="POST">
                        <div class="mb-3">
                            <label class="form-label text-light small">Student Email</label>
                            <input type="email" name="email" class="form-control bg-dark text-white border-secondary" 
                                   value="alex.morgan@university.edu" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label text-light small">Password</label>
                            <input type="password" name="password" class="form-control bg-dark text-white border-secondary" 
                                   value="Password123!" required>
                        </div>
                        <button type="submit" class="btn btn-primary w-100 py-2">
                            <i class="bi bi-box-arrow-in-right me-1"></i> Sign In to JSP View
                        </button>
                    </form>
                    <div class="mt-4 text-center">
                        <a href="index.jsp" class="text-decoration-none text-secondary small">← Back to Lab Home</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

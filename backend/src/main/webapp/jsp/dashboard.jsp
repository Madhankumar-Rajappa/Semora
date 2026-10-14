<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.campustrack.model.Student, com.campustrack.dao.SemesterDAO, com.campustrack.model.Semester, java.util.List" %>
<%
    Student s = (Student) session.getAttribute("jspStudent");
    if (s == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    SemesterDAO semDao = new SemesterDAO();
    List<Semester> semesters = null;
    try {
        semesters = semDao.findAllByStudentId(s.getId());
    } catch (Exception e) {
        // Handled in view
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>JSP Lab Dashboard — <%= s.getName() %></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { background: #0b0f19; color: #f8fafc; font-family: system-ui, sans-serif; }
        .card { background: #151d30; border: 1px solid #1e293b; border-radius: 0.75rem; }
    </style>
</head>
<body class="p-4">
    <div class="container">
        <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom border-secondary">
            <div>
                <h2 class="fw-bold text-white mb-1"><i class="bi bi-mortarboard me-2 text-primary"></i>CampusTrack Lab Dashboard</h2>
                <p class="text-secondary mb-0">Student Profile: <span class="text-info fw-semibold"><%= s.getName() %></span> (<%= s.getEmail() %>)</p>
            </div>
            <div>
                <a href="semesters.jsp" class="btn btn-outline-primary me-2"><i class="bi bi-calendar3 me-1"></i> Semesters</a>
                <a href="index.jsp" class="btn btn-outline-danger"><i class="bi bi-power me-1"></i> Exit</a>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <div class="col-md-4">
                <div class="card p-3 shadow-sm">
                    <span class="text-secondary small">DEPARTMENT</span>
                    <h5 class="fw-bold text-white mt-1"><%= s.getDepartment() %></h5>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card p-3 shadow-sm">
                    <span class="text-secondary small">COLLEGE / INSTITUTE</span>
                    <h5 class="fw-bold text-white mt-1"><%= s.getCollege() %></h5>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card p-3 shadow-sm">
                    <span class="text-secondary small">TOTAL RECORDED SEMESTERS</span>
                    <h5 class="fw-bold text-success mt-1"><%= (semesters != null ? semesters.size() : 0) %> Semesters</h5>
                </div>
            </div>
        </div>

        <div class="card p-4">
            <h5 class="text-white fw-bold mb-3"><i class="bi bi-journal-text me-2"></i>Academic Semesters (JDBC Query)</h5>
            <div class="table-responsive">
                <table class="table table-dark table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Semester</th>
                            <th>Academic Year</th>
                            <th>Status</th>
                            <th>SGPA</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (semesters != null && !semesters.isEmpty()) { 
                            for (Semester sem : semesters) { %>
                                <tr>
                                    <td>Semester <%= sem.getSemesterNumber() %></td>
                                    <td><%= sem.getAcademicYear() %></td>
                                    <td>
                                        <span class="badge <%= "completed".equalsIgnoreCase(sem.getStatus()) ? "bg-success" : "bg-primary" %>">
                                            <%= sem.getStatus() %>
                                        </span>
                                    </td>
                                    <td class="fw-bold text-info"><%= sem.getSgpa() != null ? sem.getSgpa() : "In Progress" %></td>
                                </tr>
                        <%  } 
                        } else { %>
                            <tr><td colspan="4" class="text-secondary text-center py-3">No semesters recorded yet.</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.campustrack.model.Student, com.campustrack.dao.SemesterDAO, com.campustrack.dao.SubjectDAO, com.campustrack.model.Semester, com.campustrack.model.Subject, java.util.List" %>
<%
    Student s = (Student) session.getAttribute("jspStudent");
    if (s == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    SemesterDAO semDao = new SemesterDAO();
    SubjectDAO subDao = new SubjectDAO();
    List<Semester> semesters = null;
    List<Subject> subjects = null;
    int selectedSemId = -1;

    try {
        semesters = semDao.findAllByStudentId(s.getId());
        String semParam = request.getParameter("semId");
        if (semParam != null && !semParam.isEmpty()) {
            selectedSemId = Integer.parseInt(semParam);
        } else if (semesters != null && !semesters.isEmpty()) {
            selectedSemId = semesters.get(semesters.size() - 1).getId();
        }

        if (selectedSemId > 0) {
            subjects = subDao.findBySemesterId(selectedSemId, s.getId());
        }
    } catch (Exception e) {
        // Handled
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>JSP Lab Demo — Semester Academic Records</title>
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
                <h2 class="fw-bold text-white mb-1"><i class="bi bi-journal-bookmark me-2 text-primary"></i>Semesters & Subjects (JSP View)</h2>
                <p class="text-secondary mb-0">Lab Demonstration: Direct JDBC Query & Session Handling</p>
            </div>
            <div>
                <a href="dashboard.jsp" class="btn btn-outline-light me-2"><i class="bi bi-grid me-1"></i> Dashboard</a>
                <a href="index.jsp" class="btn btn-outline-danger"><i class="bi bi-power me-1"></i> Exit</a>
            </div>
        </div>

        <!-- Semester Selection Filter -->
        <div class="card p-3 mb-4">
            <form method="GET" class="d-flex align-items-center gap-3">
                <label class="text-secondary small fw-bold mb-0">SELECT SEMESTER:</label>
                <select name="semId" class="form-select bg-dark text-white border-secondary w-auto" onchange="this.form.submit()">
                    <% if (semesters != null) {
                        for (Semester sem : semesters) { %>
                            <option value="<%= sem.getId() %>" <%= (sem.getId() == selectedSemId) ? "selected" : "" %>>
                                Semester <%= sem.getSemesterNumber() %> (<%= sem.getAcademicYear() %>) — <%= sem.getStatus().toUpperCase() %>
                            </option>
                    <%  }
                    } %>
                </select>
            </form>
        </div>

        <!-- Subjects Table -->
        <div class="card p-4">
            <h5 class="text-white fw-bold mb-3"><i class="bi bi-book me-2 text-info"></i>Enrolled Coursework</h5>
            <div class="table-responsive">
                <table class="table table-dark table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Subject Code</th>
                            <th>Subject Name</th>
                            <th>Credits</th>
                            <th>Instructor</th>
                            <th>Grade</th>
                            <th>Marks / 100</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (subjects != null && !subjects.isEmpty()) {
                            for (Subject sub : subjects) { %>
                                <tr>
                                    <td class="fw-bold text-white"><%= sub.getSubjectCode() %></td>
                                    <td><%= sub.getSubjectName() %></td>
                                    <td><span class="badge bg-secondary"><%= sub.getCredits() %> Credits</span></td>
                                    <td class="text-secondary"><%= sub.getInstructor() != null ? sub.getInstructor() : "N/A" %></td>
                                    <td><span class="badge bg-primary"><%= sub.getGrade() != null ? sub.getGrade() : "—" %></span></td>
                                    <td class="fw-bold text-info"><%= sub.getFinalMarks() != null ? sub.getFinalMarks() : "—" %></td>
                                </tr>
                        <%  }
                        } else { %>
                            <tr><td colspan="6" class="text-secondary text-center py-4">No subjects found for this semester.</td></tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</body>
</html>

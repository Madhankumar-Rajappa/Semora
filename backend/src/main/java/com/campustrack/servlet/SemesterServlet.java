package com.campustrack.servlet;

import com.campustrack.model.AcademicSummary;
import com.campustrack.model.Semester;
import com.campustrack.model.Subject;
import com.campustrack.service.SemesterService;
import com.campustrack.service.SubjectService;
import com.campustrack.util.JsonUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@WebServlet("/api/semesters/*")
public class SemesterServlet extends HttpServlet {

    private final SemesterService semesterService = new SemesterService();
    private final SubjectService subjectService = new SubjectService();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

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
                // List all semesters scoped to authenticated student
                List<Semester> list = semesterService.getSemesters(studentId);
                JsonUtil.sendSuccess(resp, list, "Semesters retrieved successfully.");
            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length == 2) {
                    // /api/semesters/{id}
                    int semId = Integer.parseInt(parts[1]);
                    Semester sem = semesterService.getSemesterDetails(semId, studentId);
                    if (sem == null) {
                        JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Semester not found or access denied.");
                        return;
                    }
                    JsonUtil.sendSuccess(resp, sem, "Semester details retrieved.");
                } else if (parts.length >= 3 && "subjects".equalsIgnoreCase(parts[2])) {
                    // GET /api/semesters/{semesterId}/subjects
                    int semId = Integer.parseInt(parts[1]);
                    Semester sem = semesterService.getSemesterDetails(semId, studentId);
                    if (sem == null) {
                        JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Semester not found or access denied.");
                        return;
                    }
                    List<Subject> subjects = subjectService.getSubjectsBySemester(semId, studentId);
                    JsonUtil.sendSuccess(resp, subjects, "Semester subjects retrieved.");
                } else if (parts.length >= 3 && "academic-summary".equalsIgnoreCase(parts[2])) {
                    // GET /api/semesters/{semesterId}/academic-summary
                    int semId = Integer.parseInt(parts[1]);
                    AcademicSummary summary = subjectService.getAcademicSummary(semId, studentId);
                    if (summary == null) {
                        JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Semester not found or access denied.");
                        return;
                    }
                    JsonUtil.sendSuccess(resp, summary, "Semester academic summary retrieved.");
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request path.");
                }
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid semester ID format.");
        } catch (Exception e) {
            System.err.println("[SemesterServlet] Error in doGet: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error occurred while retrieving semester data.");
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

            // POST /api/semesters/{semesterId}/subjects
            if (parts.length >= 3 && "subjects".equalsIgnoreCase(parts[2])) {
                int semId = Integer.parseInt(parts[1]);
                Semester sem = semesterService.getSemesterDetails(semId, studentId);
                if (sem == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Semester not found or access denied.");
                    return;
                }

                Subject sub = JsonUtil.readBody(req, Subject.class);
                if (sub == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
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

                // Check duplicate subject code within the same semester
                Subject existing = subjectService.getSubjectByCode(sub.getSubjectCode(), semId, studentId);
                if (existing != null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, 
                            "Subject code '" + sub.getSubjectCode().trim().toUpperCase() + "' already exists in this semester.");
                    return;
                }

                sub.setSemesterId(semId);
                sub.setStudentId(studentId);
                Subject created = subjectService.createSubject(sub);
                JsonUtil.sendCreated(resp, created, "Subject added to semester successfully.");
                return;
            }

            // POST /api/semesters (Create semester)
            if (pathInfo == null || "/".equals(pathInfo) || "".equals(pathInfo)) {
                Semester sem = JsonUtil.readBody(req, Semester.class);
                if (sem == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON payload.");
                    return;
                }

                // Validation: Semester number 1 through 8
                if (sem.getSemesterNumber() < 1 || sem.getSemesterNumber() > 8) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Semester number must be an integer between 1 and 8.");
                    return;
                }

                // Validation: Academic Year required
                if (sem.getAcademicYear() == null || sem.getAcademicYear().trim().isEmpty()) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Academic year is required (e.g. '2024-2025').");
                    return;
                }
                sem.setAcademicYear(sem.getAcademicYear().trim());

                // Status validation and normalization (default Active)
                String status = sem.getStatus();
                if (status == null || status.trim().isEmpty() || "active".equalsIgnoreCase(status)) {
                    sem.setStatus("Active");
                } else if ("completed".equalsIgnoreCase(status)) {
                    sem.setStatus("Completed");
                } else {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Status must be either 'Active' or 'Completed'.");
                    return;
                }

                // Check duplicate semester number for this student
                Semester existing = semesterService.getSemesterByNumber(sem.getSemesterNumber(), studentId);
                if (existing != null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, 
                            "Semester " + sem.getSemesterNumber() + " already exists for this student.");
                    return;
                }

                sem.setStudentId(studentId);
                Semester created = semesterService.createSemester(sem);
                JsonUtil.sendCreated(resp, created, "Semester " + created.getSemesterNumber() + " created successfully.");
                return;
            }

            JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found.");

        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid semester ID format.");
        } catch (SQLIntegrityConstraintViolationException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, "A unique constraint conflict occurred: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[SemesterServlet] Error in doPost: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating semester or subject.");
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Semester ID required in path.");
            return;
        }

        try {
            int semId = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            Semester sem = JsonUtil.readBody(req, Semester.class);
            if (sem == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Request payload is required.");
                return;
            }

            if (sem.getSemesterNumber() < 1 || sem.getSemesterNumber() > 8) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Semester number must be an integer between 1 and 8.");
                return;
            }

            if (sem.getAcademicYear() == null || sem.getAcademicYear().trim().isEmpty()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Academic year is required.");
                return;
            }
            sem.setAcademicYear(sem.getAcademicYear().trim());

            String status = sem.getStatus();
            if (status == null || status.trim().isEmpty() || "active".equalsIgnoreCase(status)) {
                sem.setStatus("Active");
            } else if ("completed".equalsIgnoreCase(status)) {
                sem.setStatus("Completed");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Status must be either 'Active' or 'Completed'.");
                return;
            }

            Semester existing = semesterService.getSemesterByNumber(sem.getSemesterNumber(), studentId);
            if (existing != null && existing.getId() != semId) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, 
                        "Semester " + sem.getSemesterNumber() + " already exists for this student.");
                return;
            }

            sem.setId(semId);
            sem.setStudentId(studentId);
            boolean updated = semesterService.updateSemester(sem);
            if (updated) {
                Semester updatedSem = semesterService.getSemesterDetails(semId, studentId);
                JsonUtil.sendSuccess(resp, updatedSem != null ? updatedSem : sem, "Semester updated successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Semester not found or access denied.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid semester ID format.");
        } catch (SQLIntegrityConstraintViolationException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_CONFLICT, "Semester number conflict.");
        } catch (Exception e) {
            System.err.println("[SemesterServlet] Error in doPut: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating semester.");
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer studentId = (Integer) req.getAttribute("studentId");
        if (studentId == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access.");
            return;
        }

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Semester ID required in path.");
            return;
        }

        try {
            String[] parts = pathInfo.substring(1).split("/");
            int semId = Integer.parseInt(parts[0]);

            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String targetStatus = null;
            if (sb.length() > 0) {
                try {
                    JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
                    if (json.has("status") && !json.get("status").isJsonNull()) {
                        targetStatus = json.get("status").getAsString().trim();
                    }
                } catch (Exception ignored) {}
            }

            if (targetStatus == null || targetStatus.isEmpty()) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Field 'status' is required.");
                return;
            }

            String normalizedStatus = "Active";
            if ("completed".equalsIgnoreCase(targetStatus)) {
                normalizedStatus = "Completed";
            } else if ("active".equalsIgnoreCase(targetStatus)) {
                normalizedStatus = "Active";
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Status must be 'Active' or 'Completed'.");
                return;
            }

            boolean updated = semesterService.updateSemesterStatus(semId, studentId, normalizedStatus);
            if (updated) {
                Semester updatedSem = semesterService.getSemesterDetails(semId, studentId);
                JsonUtil.sendSuccess(resp, updatedSem, "Semester status marked as " + normalizedStatus + ".");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Semester not found or access denied.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid semester ID format.");
        } catch (Exception e) {
            System.err.println("[SemesterServlet] Error in doPatch: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error updating status.");
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Semester ID required.");
            return;
        }

        try {
            int semId = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            boolean deleted = semesterService.deleteSemester(semId, studentId);
            if (deleted) {
                JsonUtil.sendSuccess(resp, null, "Semester deleted successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Semester not found or access denied.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid semester ID format.");
        } catch (Exception e) {
            System.err.println("[SemesterServlet] Error in doDelete: " + e.getMessage());
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error deleting semester.");
        }
    }
}

package com.campustrack.servlet;

import com.campustrack.model.DocumentRecord;
import com.campustrack.service.RecordService;
import com.campustrack.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/records/*")
public class RecordServlet extends HttpServlet {

    private final RecordService recordService = new RecordService();

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

                List<DocumentRecord> list = recordService.getRecords(studentId, semesterId, category, search);
                JsonUtil.sendSuccess(resp, list, "Document records retrieved.");
            } else {
                int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
                DocumentRecord r = recordService.getRecord(id, studentId);
                if (r == null) {
                    JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Document record not found.");
                    return;
                }
                JsonUtil.sendSuccess(resp, r, "Document record details retrieved.");
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
            DocumentRecord r = JsonUtil.readBody(req, DocumentRecord.class);
            if (r == null || r.getTitle() == null || r.getCategory() == null || r.getFileUrl() == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Title, category, and file URL are required.");
                return;
            }
            r.setStudentId(studentId);
            DocumentRecord created = recordService.createRecord(r);
            JsonUtil.sendCreated(resp, created, "Document record created successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error creating document record: " + e.getMessage());
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Record ID required.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            DocumentRecord r = JsonUtil.readBody(req, DocumentRecord.class);
            if (r == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Payload required.");
                return;
            }
            r.setId(id);
            r.setStudentId(studentId);
            boolean ok = recordService.updateRecord(r);
            if (ok) {
                JsonUtil.sendSuccess(resp, r, "Document record updated successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Document record not found.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid record ID.");
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
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Record ID required.");
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1).split("/")[0]);
            boolean ok = recordService.deleteRecord(id, studentId);
            if (ok) {
                JsonUtil.sendSuccess(resp, null, "Document record deleted successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Document record not found.");
            }
        } catch (NumberFormatException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid record ID.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error: " + e.getMessage());
        }
    }
}

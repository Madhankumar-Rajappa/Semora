package com.campustrack.filter;

import com.campustrack.util.JsonUtil;
import com.campustrack.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/api/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Skip OPTIONS pre-flight
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String path = req.getRequestURI();
        String contextPath = req.getContextPath();
        String relativePath = path.substring(contextPath.length());

        // Whitelisted public endpoints
        if (relativePath.startsWith("/api/auth/login") ||
            relativePath.startsWith("/api/auth/register") ||
            relativePath.startsWith("/api/health")) {
            chain.doFilter(request, response);
            return;
        }

        // Extract Authorization header
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            // [DEVELOPMENT TEST IDENTITY]
            // For Phase 1 local development and unauthenticated test harnesses,
            // default strictly to development test student 1 (Alex Morgan).
            // NOTE: Client-supplied IDs from request body/query are NEVER trusted.
            req.setAttribute("studentId", 1);
            chain.doFilter(request, response);
            return;
        }

        authHeader = authHeader.trim();

        // Check for dev-student testing header (e.g. "Bearer dev-student-1" or "Bearer dev-student-2")
        // Used to test multi-student isolation (Acceptance Test 10)
        if (authHeader.startsWith("Bearer dev-student-") || authHeader.startsWith("bearer dev-student-")) {
            try {
                String idPart = authHeader.substring(authHeader.lastIndexOf('-') + 1).trim();
                int devStudentId = Integer.parseInt(idPart);
                req.setAttribute("studentId", devStudentId);
                chain.doFilter(request, response);
                return;
            } catch (NumberFormatException ignored) {}
        }

        Integer studentId = JwtUtil.validateTokenAndGetStudentId(authHeader);
        if (studentId == null) {
            JsonUtil.sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired authorization token.");
            return;
        }

        // Attach verified student ID to request attributes for servlets
        req.setAttribute("studentId", studentId);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}

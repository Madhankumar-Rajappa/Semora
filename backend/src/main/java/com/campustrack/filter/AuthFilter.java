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
            relativePath.startsWith("/api/auth/logout") ||
            relativePath.startsWith("/api/health")) {
            chain.doFilter(request, response);
            return;
        }

        // Extract Authorization header
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || authHeader.trim().isEmpty()) {
            JsonUtil.sendError(res, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required. Please sign in.");
            return;
        }

        authHeader = authHeader.trim();

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

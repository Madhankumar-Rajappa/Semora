<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>CampusTrack — Web Tech Lab Demo (JSP)</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body {
            background: linear-gradient(135deg, #0f172a 0%, #1e1b4b 100%);
            color: #f8fafc;
            min-height: 100vh;
            display: flex;
            align-items: center;
        }
        .glass-card {
            background: rgba(30, 41, 59, 0.7);
            backdrop-filter: blur(12px);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 1rem;
        }
    </style>
</head>
<body>
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="glass-card p-5 shadow-lg text-center">
                    <div class="mb-4">
                        <span class="badge bg-primary px-3 py-2 rounded-pill fs-6">
                            <i class="bi bi-mortarboard-fill me-1"></i> Web Technology Laboratory
                        </span>
                    </div>
                    <h1 class="display-5 fw-bold text-white mb-3">CampusTrack</h1>
                    <p class="lead text-light mb-4">
                        Personal College Life Tracker — Java Servlets, JDBC & MySQL Backend Engine
                    </p>
                    <div class="alert alert-info border-0 bg-info-subtle text-dark text-start mb-4">
                        <strong><i class="bi bi-info-circle-fill me-2"></i>Lab Evaluator Note:</strong>
                        This server hosts the complete Java Servlets REST API backend running on Apache Tomcat.
                        The primary modern production frontend is hosted on <strong>Vercel</strong>, consuming these endpoints via cross-origin HTTPS requests.
                        Below you can also test basic JSP views for traditional laboratory demonstration.
                    </div>
                    <div class="d-flex flex-wrap justify-content-center gap-3">
                        <a href="login.jsp" class="btn btn-primary btn-lg px-4">
                            <i class="bi bi-box-arrow-in-right me-2"></i> JSP Lab Login Demo
                        </a>
                        <a href="../api/health" target="_blank" class="btn btn-outline-light btn-lg px-4">
                            <i class="bi bi-heart-pulse-fill me-2 text-danger"></i> View REST Health API
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

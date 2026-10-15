package com.campustrack;

import com.campustrack.filter.AuthFilter;
import com.campustrack.filter.CorsFilter;
import com.campustrack.servlet.*;
import com.campustrack.util.DBConnection;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.File;

/**
 * Embedded Tomcat 10 Application Server
 * Allows running CampusTrack backend directly via Java/Maven on port 8080.
 */
public class ApplicationServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.trim().isEmpty()) {
            try {
                port = Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {}
        }

        System.out.println("==========================================================");
        System.out.println("  Starting CampusTrack Backend Server (Tomcat 10)");
        System.out.println("  Port: " + port);
        System.out.println("==========================================================");

        // Pre-check database connection
        boolean dbOk = DBConnection.checkConnection();
        System.out.println("[Database Status] Connected to MySQL: " + (dbOk ? "YES (OK)" : "FAILED (Check credentials)"));

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // Trigger connector creation

        // Webapp root directory
        File docBase = new File("src/main/webapp");
        if (!docBase.exists()) {
            docBase = new File("backend/src/main/webapp");
        }
        if (!docBase.exists()) {
            docBase = new File(".");
        }

        // Base temp directory for Tomcat
        File baseDir = new File("target/tomcat-embed");
        baseDir.mkdirs();
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        // Use addContext for direct programmatic servlet and filter binding
        Context ctx = tomcat.addContext("", docBase.getAbsolutePath());
        ctx.setParentClassLoader(ApplicationServer.class.getClassLoader());

        // Register Filters
        // 1. CORS Filter
        FilterDef corsDef = new FilterDef();
        corsDef.setFilterName("CorsFilter");
        corsDef.setFilterClass(CorsFilter.class.getName());
        corsDef.setFilter(new CorsFilter());
        ctx.addFilterDef(corsDef);

        FilterMap corsMap = new FilterMap();
        corsMap.setFilterName("CorsFilter");
        corsMap.addURLPattern("/api/*");
        ctx.addFilterMap(corsMap);

        // 2. Auth Filter
        FilterDef authDef = new FilterDef();
        authDef.setFilterName("AuthFilter");
        authDef.setFilterClass(AuthFilter.class.getName());
        authDef.setFilter(new AuthFilter());
        ctx.addFilterDef(authDef);

        FilterMap authMap = new FilterMap();
        authMap.setFilterName("AuthFilter");
        authMap.addURLPattern("/api/*");
        ctx.addFilterMap(authMap);

        // Register Servlets
        Tomcat.addServlet(ctx, "HealthServlet", new HealthServlet()).addMapping("/api/health");
        Tomcat.addServlet(ctx, "AuthServlet", new AuthServlet()).addMapping("/api/auth/*");
        Tomcat.addServlet(ctx, "SemesterServlet", new SemesterServlet()).addMapping("/api/semesters/*");
        Tomcat.addServlet(ctx, "SubjectServlet", new SubjectServlet()).addMapping("/api/subjects/*");
        Tomcat.addServlet(ctx, "AssessmentServlet", new AssessmentServlet()).addMapping("/api/assessments/*");

        // Start server
        tomcat.start();
        System.out.println("\n>>> CampusTrack Server is live at: http://localhost:" + port + "/");
        System.out.println(">>> REST API Health check: http://localhost:" + port + "/api/health");
        System.out.println(">>> Lab JSP Views:         http://localhost:" + port + "/jsp/index.jsp\n");

        tomcat.getServer().await();
    }
}

package com.campustrack.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[AppContextListener] CampusTrack API context initialized.");
        // Test connectivity
        boolean dbOk = DBConnection.checkConnection();
        System.out.println("[AppContextListener] Database connected: " + dbOk);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[AppContextListener] CampusTrack API context shutting down...");
        DBConnection.closePool();
    }
}

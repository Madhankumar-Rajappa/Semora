package com.campustrack.service;

import com.campustrack.dao.AnalyticsDAO;
import com.google.gson.JsonObject;

import java.sql.SQLException;

public class AnalyticsService {

    private final AnalyticsDAO analyticsDAO = new AnalyticsDAO();

    public JsonObject getDashboardOverview(int studentId) throws SQLException {
        return analyticsDAO.getDashboardOverview(studentId);
    }
}

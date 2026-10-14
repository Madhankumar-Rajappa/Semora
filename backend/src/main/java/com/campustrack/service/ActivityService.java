package com.campustrack.service;

import com.campustrack.dao.ActivityDAO;
import com.campustrack.model.Activity;

import java.sql.SQLException;
import java.util.List;

public class ActivityService {

    private final ActivityDAO activityDAO = new ActivityDAO();

    public List<Activity> getActivities(int studentId, Integer semesterId, String category, String search) throws SQLException {
        return activityDAO.findAll(studentId, semesterId, category, search);
    }

    public Activity getActivity(int id, int studentId) throws SQLException {
        return activityDAO.findByIdAndStudentId(id, studentId);
    }

    public Activity createActivity(Activity activity) throws SQLException {
        int id = activityDAO.create(activity);
        activity.setId(id);
        return activity;
    }

    public boolean updateActivity(Activity activity) throws SQLException {
        return activityDAO.update(activity);
    }

    public boolean deleteActivity(int id, int studentId) throws SQLException {
        return activityDAO.delete(id, studentId);
    }
}

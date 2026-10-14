package com.campustrack.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.campustrack.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Utility for parsing JSON requests and emitting standardized JSON API responses.
 */
public class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    public static Gson getGson() {
        return GSON;
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T readBody(HttpServletRequest request, Class<T> classOfT) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return GSON.fromJson(sb.toString(), classOfT);
    }

    public static void sendResponse(HttpServletResponse response, int statusCode, ApiResponse<?> apiResponse) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String json = GSON.toJson(apiResponse);
        response.getWriter().write(json);
        response.getWriter().flush();
    }

    public static void sendSuccess(HttpServletResponse response, Object data, String message) throws IOException {
        sendResponse(response, HttpServletResponse.SC_OK, ApiResponse.success(data, message));
    }

    public static void sendCreated(HttpServletResponse response, Object data, String message) throws IOException {
        sendResponse(response, HttpServletResponse.SC_CREATED, ApiResponse.success(data, message));
    }

    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        sendResponse(response, statusCode, ApiResponse.error(message));
    }
}

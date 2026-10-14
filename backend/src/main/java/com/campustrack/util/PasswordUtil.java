package com.campustrack.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for hashing and verifying passwords securely with SHA-256 and salt.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltBase64 = Base64.getEncoder().encodeToString(salt);

        String hash = computeHash(plainPassword, salt);
        return saltBase64 + ":" + hash;
    }

    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }

        // Support both formatted "salt:hash" and raw SHA-256 hashes (for seed data)
        if (storedHash.contains(":")) {
            String[] parts = storedHash.split(":", 2);
            if (parts.length != 2) return false;
            try {
                byte[] salt = Base64.getDecoder().decode(parts[0]);
                String computed = computeHash(plainPassword, salt);
                return constantTimeEquals(parts[1], computed);
            } catch (Exception e) {
                return false;
            }
        } else {
            // Direct SHA-256 comparison for demo seed accounts
            String rawHex = computeRawSha256(plainPassword);
            return constantTimeEquals(storedHash, rawHex);
        }
    }

    private static String computeHash(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public static String computeRawSha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

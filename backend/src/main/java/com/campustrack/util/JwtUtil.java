package com.campustrack.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Lightweight and robust HMAC-SHA256 JWT Token generator and validator.
 */
public class JwtUtil {

    private static final String DEFAULT_SECRET = "CampusTrackSecureSecretKey2026!9876543210#@";
    private static final String HMAC_ALGO = "HmacSHA256";
    private static final long EXPIRATION_MILLIS = 7L * 24 * 60 * 60 * 1000; // 7 days

    private static String getSecret() {
        String envSecret = System.getenv("JWT_SECRET");
        return (envSecret != null && !envSecret.trim().isEmpty()) ? envSecret.trim() : DEFAULT_SECRET;
    }

    public static String generateToken(int studentId, String email, String name) {
        try {
            long now = System.currentTimeMillis();
            long exp = now + EXPIRATION_MILLIS;

            // Header
            JsonObject header = new JsonObject();
            header.addProperty("alg", "HS256");
            header.addProperty("typ", "JWT");
            String encodedHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(header.toString().getBytes(StandardCharsets.UTF_8));

            // Claims Payload
            JsonObject payload = new JsonObject();
            payload.addProperty("sub", String.valueOf(studentId));
            payload.addProperty("email", email);
            payload.addProperty("name", name);
            payload.addProperty("iat", now / 1000);
            payload.addProperty("exp", exp / 1000);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));

            // Signature
            String signatureInput = encodedHeader + "." + encodedPayload;
            String signature = sign(signatureInput, getSecret());

            return encodedHeader + "." + encodedPayload + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Error generating auth token", e);
        }
    }

    public static Integer validateTokenAndGetStudentId(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        token = token.trim();
        if (token.startsWith("Bearer ") || token.startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return null;
        }

        try {
            String encodedHeader = parts[0];
            String encodedPayload = parts[1];
            String signature = parts[2];

            // Verify signature
            String expectedSig = sign(encodedHeader + "." + encodedPayload, getSecret());
            if (!expectedSig.equals(signature)) {
                return null;
            }

            // Decode payload
            byte[] payloadBytes = Base64.getUrlDecoder().decode(encodedPayload);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            JsonObject payload = JsonParser.parseString(payloadJson).getAsJsonObject();

            // Check expiration
            long exp = payload.get("exp").getAsLong() * 1000;
            if (System.currentTimeMillis() > exp) {
                return null; // Expired
            }

            return payload.get("sub").getAsInt();
        } catch (Exception e) {
            return null;
        }
    }

    private static String sign(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
    }
}

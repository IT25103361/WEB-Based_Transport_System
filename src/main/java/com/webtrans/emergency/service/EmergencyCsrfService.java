package com.webtrans.emergency.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EmergencyCsrfService {

    public static final String CSRF_SESSION_ATTR = "EMERGENCY_CSRF_TOKEN";
    private final SecureRandom secureRandom = new SecureRandom();

    public String getOrCreateToken(HttpSession session) {
        if (session == null) {
            return generateRandomToken();
        }
        Object existing = session.getAttribute(CSRF_SESSION_ATTR);
        if (existing instanceof String && !((String) existing).isBlank()) {
            return (String) existing;
        }
        String newToken = generateRandomToken();
        session.setAttribute(CSRF_SESSION_ATTR, newToken);
        return newToken;
    }

    public boolean validateToken(HttpSession session, String tokenFromClient) {
        if (session == null || tokenFromClient == null || tokenFromClient.isBlank()) {
            return false;
        }
        Object sessionToken = session.getAttribute(CSRF_SESSION_ATTR);
        if (!(sessionToken instanceof String)) {
            return false;
        }
        return constantTimeEquals((String) sessionToken, tokenFromClient.trim());
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}

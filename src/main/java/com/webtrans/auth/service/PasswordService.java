package com.webtrans.auth.service;

import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    public String encode(String rawPassword) {
        if (rawPassword == null) return null;
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Verifies password with legacy plaintext compatibility.
     * Supports both BCrypt hashes ($2a$, $2b$, $2y$) and legacy plaintext passwords.
     */
    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBCrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        // Legacy plaintext fallback to prevent lockout of existing accounts
        return rawPassword.equals(storedPassword);
    }

    public boolean isBCrypt(String storedPassword) {
        return storedPassword != null &&
                (storedPassword.startsWith("$2a$") ||
                 storedPassword.startsWith("$2b$") ||
                 storedPassword.startsWith("$2y$"));
    }

    /**
     * Seamlessly upgrades legacy plaintext passwords to BCrypt upon successful authentication.
     */
    public void upgradeIfLegacy(User user, String rawPassword) {
        if (user != null && !isBCrypt(user.getPassword())) {
            user.setPassword(encode(rawPassword));
            userRepository.save(user);
        }
    }
}

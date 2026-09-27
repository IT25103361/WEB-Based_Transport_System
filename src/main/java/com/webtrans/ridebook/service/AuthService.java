package com.seproject.courier.service;

import com.seproject.courier.dto.auth.AuthResponse;
import com.seproject.courier.dto.auth.LoginRequest;
import com.seproject.courier.dto.auth.RegisterRequest;
import com.seproject.courier.entity.User;
import com.seproject.courier.repository.UserRepository;
import com.seproject.courier.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ==================== REGISTER ====================
    public AuthResponse register(RegisterRequest request) {

        // 1. Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        // 2. Build User entity
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        // 3. Save
        User saved = userRepository.save(user);

        // 4. Generate JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", saved.getId());
        claims.put("role", saved.getRole().name());
        String token = jwtUtil.generateToken(saved.getEmail(), claims);

        // 5. Return
        return AuthResponse.builder()
                .token(token)
                .userId(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    // ==================== LOGIN ====================
    public AuthResponse login(LoginRequest request) {

        // 1. Let Spring Security authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // If we get here without exception → credentials are valid
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Generate fresh JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().name());
        String token = jwtUtil.generateToken(user.getEmail(), claims);

        // 3. Return
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
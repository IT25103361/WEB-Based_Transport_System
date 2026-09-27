package com.seproject.courier.config;

import com.seproject.courier.security.CustomUserDetailsService;
import com.seproject.courier.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF (we use JWT, not session cookies)
                .csrf(AbstractHttpConfigurer::disable)

                // Enable CORS with our config
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Use stateless sessions (no HttpSession)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // ===== Public endpoints — no auth required =====
                        .requestMatchers(
                                // JSP pages + static assets
                                "/",
                                "/login",
                                "/register",
                                "/register-partner",
                                "/sender",
                                "/partner",
                                "/track",
                                "/*.html",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/webjars/**",
                                "/favicon.ico",

                                // WebSocket handshake + SockJS endpoints
                                "/ws/**",

                                // REST API — public
                                "/api/courier/auth/**",     // register, login
                                "/api/courier/track/**",    // public tracking

                                // Error handler
                                "/error"
                        ).permitAll()

                        // ===== Role-specific endpoints =====
                        .requestMatchers("/api/courier/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/courier/partner/**").hasRole("PARTNER")
                        .requestMatchers("/api/courier/parcels/**").hasAnyRole("SENDER", "ADMIN")

                        // Everything else needs auth
                        .anyRequest().authenticated()
                )

                // Register our authentication provider
                .authenticationProvider(authenticationProvider())

                // Add JWT filter before the username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
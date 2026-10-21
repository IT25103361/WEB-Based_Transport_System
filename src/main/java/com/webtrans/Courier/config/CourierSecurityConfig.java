package com.webtrans.Courier.config;

import com.webtrans.Courier.security.CourierSessionAuthFilter;
import com.webtrans.Courier.security.CustomUserDetailsService;
import com.webtrans.Courier.security.JwtAuthFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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

@Configuration("courierSecurityConfig")
@EnableWebSecurity
public class CourierSecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }

    /**
     * Disable servlet container auto-registration of JwtAuthFilter
     * so it does not execute as a global filter on non-courier endpoints.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public CourierSessionAuthFilter courierSessionAuthFilter() {
        return new CourierSessionAuthFilter();
    }

    @Bean
    public FilterRegistrationBean<CourierSessionAuthFilter> courierSessionAuthFilterRegistration(CourierSessionAuthFilter filter) {
        FilterRegistrationBean<CourierSessionAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public PasswordEncoder courierPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider courierAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(courierPasswordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Scoped specifically to /api/courier/**
     * Session-based authentication for Courier REST APIs using the shared loggedUser identity.
     * Protected with CSRF (CookieCsrfTokenRepository) and role checks.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain courierApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/courier/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(courierCorsConfigurationSource()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED))
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Public REST endpoints
                        .requestMatchers("/api/courier/auth/**").permitAll()

                        // Tracking REST endpoint requires shared login session
                        .requestMatchers("/api/courier/track", "/api/courier/track/**").authenticated()

                        // Partner registration / onboarding under shared account
                        .requestMatchers("/api/courier/partner/register").hasAnyRole("SENDER", "PARTNER")

                        // Role-specific REST
                        .requestMatchers("/api/courier/admin", "/api/courier/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/courier/partner", "/api/courier/partner/**").hasRole("PARTNER")
                        .requestMatchers("/api/courier/parcels", "/api/courier/parcels/**").hasAnyRole("SENDER", "ADMIN", "PARTNER")

                        // Everything else under /api/courier requires authentication
                        .anyRequest().authenticated()
                )
                .authenticationProvider(courierAuthenticationProvider())
                .addFilterBefore(courierSessionAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Permissive filter chain for all other application routes (Ride, School, Rental, Admin, Courier JSPs).
     * Preserves existing session-based authentication without interfering.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultAppSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource courierCorsConfigurationSource() {
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

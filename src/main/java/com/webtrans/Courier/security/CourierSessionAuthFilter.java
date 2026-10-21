package com.webtrans.Courier.security;

import com.webtrans.Courier.model.CourierUser;
import com.webtrans.Courier.service.CourierProfileService;
import com.webtrans.auth.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class CourierSessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private CourierProfileService profileService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = (contextPath != null && !contextPath.isEmpty() && uri != null && uri.startsWith(contextPath))
                ? uri.substring(contextPath.length())
                : (request.getServletPath() != null && !request.getServletPath().isEmpty() ? request.getServletPath() : uri);

        if (path == null || (!path.startsWith("/api/courier") && !path.startsWith("/courier"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authenticate strictly from the server-side shared loggedUser session
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedUser") instanceof User) {
            User sharedUser = (User) session.getAttribute("loggedUser");
            CourierProfileService.ProfileResolutionResult res = profileService.resolveProfile(sharedUser);
            if (res.isSuccess()) {
                CourierUser courierUser = res.getProfile();
                CustomUserDetails userDetails = new CustomUserDetails(courierUser);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                SecurityContextHolder.clearContext();
            }
        } else {
            // Unauthenticated shared session: clear security context completely.
            // Old cached Courier tokens must NOT override the shared user or keep private access alive.
            SecurityContextHolder.clearContext();
        }

        // Ensure CSRF token is loaded so CookieCsrfTokenRepository writes the XSRF-TOKEN cookie
        org.springframework.security.web.csrf.CsrfToken csrfToken = 
                (org.springframework.security.web.csrf.CsrfToken) request.getAttribute(org.springframework.security.web.csrf.CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        filterChain.doFilter(request, response);
    }
}

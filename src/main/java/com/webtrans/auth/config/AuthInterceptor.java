package com.webtrans.auth.config;

import com.webtrans.auth.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final UrlPathHelper pathHelper = new UrlPathHelper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String contextPath = request.getContextPath();
        String uri = pathHelper.getPathWithinApplication(request);

        // Preserve the existing public asset paths.
        if (uri.startsWith("/school/assets") || uri.startsWith("/school/css")
                || uri.startsWith("/school/js") || uri.startsWith("/static")
                || uri.startsWith("/rental/images") || uri.startsWith("/admin/css")
                || uri.startsWith("/admin/js") || uri.startsWith("/admin/images")
                || uri.startsWith("/admin/assets") || uri.startsWith("/courier/css")
                || uri.startsWith("/courier/js") || uri.startsWith("/emergency/css")
                || uri.startsWith("/emergency/js") || uri.startsWith("/emergency/images")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/foodgrocery/")
                || uri.startsWith("/food/uploads/")) {
            return true;
        }

        // Courier routes and APIs use isolated Courier authentication
        if (uri.startsWith("/courier") || uri.startsWith("/api/courier") || uri.startsWith("/ws")) {
            return true;
        }

        // Food & Grocery module routes and APIs use isolated Food authentication
        if (uri.startsWith("/food") || uri.startsWith("/api/food")) {
            return true;
        }

        // Emergency module public routes and APIs (creation, tracking, csrf)
        if (uri.equals("/emergency") || uri.equals("/emergency/")
                || uri.startsWith("/emergency/track")
                || uri.startsWith("/api/emergency/request")
                || uri.startsWith("/api/emergency/track")
                || uri.startsWith("/api/emergency/csrf")) {
            return true;
        }

        // Pages must reflect the current identities after login/logout/account switching.
        response.setHeader("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        if (uri.equals("/") || uri.equals("/login") || uri.equals("/register")
                || uri.equals("/logout") || uri.equals("/ride/home")
                || uri.equals("/ride/") || uri.equals("/ride/ride-hub")
                || uri.equals("/ride-hub")
                || uri.equals("/ride/logout") || uri.equals("/ride/passenger/logout")
                || uri.equals("/ride/driver/logout")
                || uri.equals("/ride/passenger/login") || uri.equals("/ride/passenger/register")
                || uri.equals("/ride/driver/login") || uri.equals("/ride/driver/register")
                || uri.equals("/admin/login") || uri.equals("/admin/logout")
                || uri.equals("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        boolean sharedLogin = session != null && session.getAttribute("loggedUser") instanceof User;
        boolean driverLogin = session != null && session.getAttribute("driverUser") instanceof User;
        boolean adminLogin = session != null && session.getAttribute("adminUser") instanceof User &&
                "ADMIN".equals(session.getAttribute("adminRole"));

        // Emergency staff/admin console and management APIs
        if (uri.startsWith("/emergency/admin") || uri.startsWith("/api/emergency/admin")) {
            if (!adminLogin) {
                if (uri.startsWith("/api/emergency/admin/")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Administrator authorization required.\"}");
                    return false;
                }
                response.sendRedirect(contextPath + "/admin/login?error=auth_required");
                return false;
            }
            return true;
        }

        // Strictly protect all admin dashboard and data endpoints on the server
        if (uri.startsWith("/admin")) {
            if (!adminLogin) {
                if (uri.startsWith("/admin/api/")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Administrator authorization required.\"}");
                    return false;
                }
                response.sendRedirect(contextPath + "/admin/login?error=auth_required");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/ride/api/")) {
            boolean permitted;
            if (uri.startsWith("/ride/api/tracking/")) {
                // The controller checks whether either authenticated identity owns this ride.
                permitted = sharedLogin || driverLogin;
            } else if (uri.equals("/ride/api/location/update")
                    || (uri.startsWith("/ride/api/booking/") && uri.endsWith("/status"))) {
                permitted = driverLogin;
            } else {
                // Do not grant driver-only sessions access to unrelated/shared APIs.
                permitted = sharedLogin;
            }
            if (!permitted) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":401,\"message\":\"Please sign in to the required portal.\"}");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/ride/driver") || uri.startsWith("/ride/accept/")
                || uri.startsWith("/ride/complete/") || uri.startsWith("/ride/delete/")) {
            if (!driverLogin) {
                response.sendRedirect(contextPath + "/ride/driver/login");
                return false;
            }
            return true;
        }

        if (uri.startsWith("/ride/passenger") || uri.equals("/ride/book")
                || uri.startsWith("/ride/cancel/") || uri.startsWith("/ride/payment/")
                || uri.startsWith("/ride/pay/") || uri.startsWith("/ride/review/")) {
            if (!sharedLogin) {
                response.sendRedirect(contextPath + "/ride/passenger/login");
                return false;
            }
            return true;
        }

        // Home/Passenger login retains existing access checks for the other modules.
        if (!sharedLogin) {
            response.sendRedirect(contextPath + "/");
            return false;
        }
        return true;
    }
}

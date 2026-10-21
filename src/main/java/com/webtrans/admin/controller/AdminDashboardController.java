package com.webtrans.admin.controller;

import com.webtrans.admin.dto.ActivityItemDto;
import com.webtrans.admin.dto.AdminOverviewDto;
import com.webtrans.admin.dto.ModuleDetailsDto;
import com.webtrans.admin.service.AdminDashboardService;
import com.webtrans.auth.model.User;
import com.webtrans.auth.repository.UserRepository;
import com.webtrans.auth.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordService passwordService;

    /**
     * Redirects /admin root to /admin/dashboard.
     */
    @GetMapping("")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    /**
     * Admin login view.
     */
    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (isAdminAuthenticated(session)) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    /**
     * Processes admin credentials.
     * Prevents normal passenger or driver accounts from logging into admin dashboard.
     */
    @PostMapping("/login")
    public String processAdminLogin(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String normalizedEmail = (email != null) ? email.trim().toLowerCase() : "";
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty() || !passwordService.matches(password, userOpt.get().getPassword())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid administrator email or password.");
            return "redirect:/admin/login";
        }

        User user = userOpt.get();
        if (!user.isAdmin()) {
            redirectAttributes.addFlashAttribute("errorMsg",
                    "Access Denied: Account \"" + user.getEmail() + "\" does not have administrative privileges.");
            return "redirect:/admin/login";
        }

        // Rotate session ID to protect against session fixation
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        // Establish strictly isolated admin session
        session.setAttribute("adminUser", user);
        session.setAttribute("adminRole", "ADMIN");

        return "redirect:/admin/dashboard";
    }

    /**
     * Admin logout handler.
     */
    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("adminUser");
        session.removeAttribute("adminRole");
        return "redirect:/admin/login?logout=success";
    }

    /**
     * Main Operations Overview Dashboard view.
     */
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(name = "range", defaultValue = "7days") String range,
            @RequestParam(name = "start", required = false) String start,
            @RequestParam(name = "end", required = false) String end,
            HttpSession session,
            Model model) {

        if (!isAdminAuthenticated(session)) {
            return "redirect:/admin/login?error=auth_required";
        }

        User admin = (User) session.getAttribute("adminUser");
        AdminOverviewDto overview = adminDashboardService.getOverview(range, start, end);
        List<ActivityItemDto> activities = adminDashboardService.getRecentActivities(15);

        model.addAttribute("adminUser", admin);
        model.addAttribute("overview", overview);
        model.addAttribute("activities", activities);

        return "admin/dashboard";
    }

    /**
     * REST Endpoint: Real-time overview metrics and chart data.
     */
    @GetMapping("/api/overview")
    @ResponseBody
    public ResponseEntity<?> getOverviewApi(
            @RequestParam(name = "range", defaultValue = "7days") String range,
            @RequestParam(name = "start", required = false) String start,
            @RequestParam(name = "end", required = false) String end,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Admin session required."));
        }

        AdminOverviewDto overview = adminDashboardService.getOverview(range, start, end);
        return ResponseEntity.ok(overview);
    }

    /**
     * REST Endpoint: Granular details for a selected module.
     */
    @GetMapping("/api/modules/{moduleKey}")
    @ResponseBody
    public ResponseEntity<?> getModuleDetailsApi(
            @PathVariable("moduleKey") String moduleKey,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Admin session required."));
        }

        ModuleDetailsDto details = adminDashboardService.getModuleDetails(moduleKey);
        return ResponseEntity.ok(details);
    }

    /**
     * REST Endpoint: Recent operational activity list.
     */
    @GetMapping("/api/activity")
    @ResponseBody
    public ResponseEntity<?> getActivityApi(
            @RequestParam(name = "limit", defaultValue = "25") int limit,
            HttpSession session) {

        if (!isAdminAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Admin session required."));
        }

        List<ActivityItemDto> activity = adminDashboardService.getRecentActivities(Math.min(limit, 100));
        return ResponseEntity.ok(activity);
    }

    private boolean isAdminAuthenticated(HttpSession session) {
        if (session == null) return false;
        Object userObj = session.getAttribute("adminUser");
        Object roleObj = session.getAttribute("adminRole");
        return (userObj instanceof User) && "ADMIN".equals(roleObj);
    }
}

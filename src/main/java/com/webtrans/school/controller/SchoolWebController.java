package com.webtrans.school.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/school")
public class SchoolWebController {

    // Prefix /WEB-INF/jsp/ + view name + suffix .jsp resolves each JSP.
    @GetMapping({"", "/", "/index"})
    public String schoolIndex() {
        return "school/index";
    }

    @GetMapping("/login")
    public String login() {
        return "school/login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "school/dashboard";
    }

    @GetMapping("/student")
    public String student() {
        return "school/student";
    }

    @GetMapping("/tracking")
    public String tracking() {
        return "school/tracking";
    }

    @GetMapping("/schedule")
    public String schedule() {
        return "school/schedule";
    }

    @GetMapping("/notifications")
    public String notifications() {
        return "school/notifications";
    }

    @GetMapping("/profile")
    public String profile() {
        return "school/profile";
    }
}


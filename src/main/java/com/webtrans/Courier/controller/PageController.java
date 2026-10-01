package com.webtrans.Courier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/login", "/index.html"})
    public String login() {
        return "login";
    }

    @GetMapping({"/register", "/register.html"})
    public String register() {
        return "register";
    }

    @GetMapping({"/register-partner", "/register-partner.html"})
    public String registerPartner() {
        return "register-partner";
    }

    @GetMapping({"/sender", "/sender.html"})
    public String sender() {
        return "sender";
    }

    @GetMapping({"/partner", "/partner.html"})
    public String partner() {
        return "partner";
    }

    @GetMapping({"/track", "/track.html"})
    public String track() {
        return "track";
    }
}
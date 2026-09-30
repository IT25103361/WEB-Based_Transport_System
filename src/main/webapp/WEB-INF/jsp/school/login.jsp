<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="app-context" content="${pageContext.request.contextPath}">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Sign in | NEXTGO Parent Portal</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/animations.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/responsive.css">
</head>
<body class="login-page">
<main class="login-shell">
    <aside class="login-art">
        <a class="brand" href="${pageContext.request.contextPath}/school/index">
        <span class="brand-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
            <polyline points="2 17 12 22 22 17"></polyline>
            <polyline points="2 12 12 17 22 12"></polyline>
          </svg>
        </span>
            NEXTGO
        </a>

        <div>
            <span class="eyebrow">SCHOOL STUDENT TRANSPORT</span>
            <h1>A calmer way to follow the school run.</h1>
        </div>

        <img src="${pageContext.request.contextPath}/school/assets/images/hero-school-bus.svg" alt="School bus illustration">
    </aside>

    <section class="login-form entrance">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px;">
            <a class="back-link" href="${pageContext.request.contextPath}/school/index" style="margin-bottom: 0;">&larr; Back to home</a>
            <button class="theme-toggle-btn" id="loginThemeToggleBtn" type="button" onclick="toggleLoginTheme()" title="Toggle Theme" style="padding: 6px 14px; font-size: 12px;">
                <span id="loginThemeIcon">🌙</span> <span id="loginThemeText">Dark</span>
            </button>
        </div>

        <div>
            <span class="eyebrow">PARENT PORTAL</span>
            <h2>Welcome back</h2>
            <p>Sign in to monitor your child's school journey in real time.</p>
        </div>

        <form id="loginForm" novalidate>
            <label>
                Email Address
                <input id="email" type="email" autocomplete="email" placeholder="you@example.com" required>
            </label>
            <label>
                Password
                <input id="password" type="password" autocomplete="current-password" placeholder="Enter your password" required>
            </label>
            <p class="form-error" id="loginError" role="alert"></p>
            <button class="button full" type="submit">Sign In <span>&rarr;</span></button>
        </form>

        <!-- Demo Password Information -->
        <div class="demo-logins-card">
            <div class="demo-password-row" style="justify-content: center;">
                <span>Password for all accounts:</span>
                <code>NextGo@123</code>
            </div>
        </div>

        <p class="muted">Use the parent credentials supplied by your school.</p>
    </section>
</main>

<script>
    function fillLogin(email, password) {
        const emailInput = document.getElementById("email");
        const passInput = document.getElementById("password");
        const err = document.getElementById("loginError");
        if (emailInput && passInput) {
            emailInput.value = email;
            passInput.value = password;
            if (err) err.textContent = "";
            passInput.focus();
        }
    }
    function updateThemeBtn(t) {
        const isDark = t === "dark";
        const icon = document.getElementById("loginThemeIcon");
        const text = document.getElementById("loginThemeText");
        if (icon) icon.textContent = isDark ? "☀️" : "🌙";
        if (text) text.textContent = isDark ? "Light" : "Dark";
    }
    function toggleLoginTheme() {
        const current = localStorage.getItem("nextgo_theme") || "light";
        const next = current === "dark" ? "light" : "dark";
        localStorage.setItem("nextgo_theme", next);
        document.documentElement.setAttribute("data-theme", next);
        document.body.setAttribute("data-theme", next);
        updateThemeBtn(next);
    }
    (function() {
        const savedTheme = localStorage.getItem("nextgo_theme") || "light";
        document.documentElement.setAttribute("data-theme", savedTheme);
        document.body.setAttribute("data-theme", savedTheme);
        document.addEventListener("DOMContentLoaded", () => updateThemeBtn(savedTheme));
    })();
</script>
<script src="${pageContext.request.contextPath}/school/js/api.js"></script>
<script src="${pageContext.request.contextPath}/school/js/auth.js"></script>
</body>
</html>
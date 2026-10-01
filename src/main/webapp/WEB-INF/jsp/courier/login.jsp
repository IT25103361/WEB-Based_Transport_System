<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="ctx" content="${pageContext.request.contextPath}" />
  <title>Courier | Log in</title>
  <script>
    (function(){
      try {
        document.documentElement.setAttribute(
          "data-theme",
          localStorage.getItem("courier.theme") || "light"
        );
      } catch (e) {
        document.documentElement.setAttribute("data-theme", "light");
      }
    })();
  </script>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css" />
  <script src="${pageContext.request.contextPath}/js/api.js"></script>
</head>
<body>

<button class="theme-float" data-theme-btn title="Toggle theme">🌙</button>

<div class="auth-wrap">
  <div class="auth-card">
    <div class="logo">📦 courier<span class="dot">.</span></div>
    <h2>Welcome back</h2>
    <p class="tag">Sign in to send or deliver a parcel</p>

    <div id="msgBox"></div>

    <form id="loginForm" autocomplete="on">
      <div class="input-group">
        <label for="email">Email address</label>
        <input
                type="email"
                id="email"
                placeholder="you@example.com"
                required
                autocomplete="email"
        />
      </div>

      <div class="input-group">
        <label for="password">Password</label>
        <input
                type="password"
                id="password"
                placeholder="••••••••"
                required
                autocomplete="current-password"
        />
      </div>

      <button type="submit" class="auth-btn" id="submitBtn">Log in</button>
    </form>

    <div class="footer-link">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></div>
    <div class="footer-link">Receiving a parcel? <a href="${pageContext.request.contextPath}/track">Track with a code</a></div>
  </div>
</div>

<script src="${pageContext.request.contextPath}/js/login.js"></script>
</body>
</html>

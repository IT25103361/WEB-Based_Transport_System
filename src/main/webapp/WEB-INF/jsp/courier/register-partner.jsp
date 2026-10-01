<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="ctx" content="${pageContext.request.contextPath}" />
    <title>Courier | Create a partner account</title>
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
        <div class="logo">
            <span class="icon">🛵</span>
            courier<span>.</span>
        </div>
        <h2>Deliver parcels</h2>
        <p class="tag">Create a partner account to earn on your schedule</p>

        <div id="msgBox"></div>

        <form id="regForm">
            <div class="input-group">
                <label for="name">Full name</label>
                <input type="text" id="name" placeholder="Jane Silva" required>
            </div>

            <div class="grid-2">
                <div class="input-group">
                    <label for="email">Email address</label>
                    <input type="email" id="email" placeholder="you@example.com" required autocomplete="email">
                </div>
                <div class="input-group">
                    <label for="phone">Phone</label>
                    <input type="tel" id="phone" placeholder="07XXXXXXXX" pattern="[0-9]{10}" title="Enter a 10-digit number">
                </div>
            </div>

            <div class="input-group">
                <label for="password">Password</label>
                <input type="password" id="password" placeholder="At least 6 characters" minlength="6" required autocomplete="new-password">
            </div>

            <button type="submit" class="auth-btn" id="submitBtn">Create partner account</button>
        </form>

        <div class="footer-link">
            Want to send instead? <a href="${pageContext.request.contextPath}/register">Sign up as a sender</a>
        </div>
        <div class="footer-link">
            Already have an account? <a href="${pageContext.request.contextPath}/login">Log in here</a>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/register-partner.js"></script>
</body>
</html>

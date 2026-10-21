<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="ctx" content="${pageContext.request.contextPath}" />
  <title>Courier | Link Legacy Account</title>
  <!-- Shared NextGo Theme -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css" />
  <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css" />
  <script src="${pageContext.request.contextPath}/js/api.js"></script>
</head>
<body>

<button type="button" class="theme-float" data-theme-btn title="Toggle theme">🌙</button>

<div class="auth-wrap">
  <div class="auth-card">
    <div class="logo">📦 courier<span class="dot">.</span></div>
    <h2>Link Existing Account</h2>
    <p class="tag">Existing Courier records found for <strong><c:out value="${email}" /></strong></p>

    <div style="background: rgba(59, 130, 246, 0.08); border: 1px solid rgba(59, 130, 246, 0.25); border-radius: 8px; padding: 12px; margin-bottom: 18px; font-size: 0.88rem; line-height: 1.45; color: var(--text-muted, #475569);">
      To preserve your existing parcel history, delivery tracking, and partner privileges, verify your previous Courier password to link this profile to your NEXT GO identity.
    </div>

    <c:if test="${not empty error}">
      <div style="background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; padding: 10px 14px; border-radius: 8px; margin-bottom: 16px; font-size: 0.88rem;">
        <c:out value="${error}" />
      </div>
    </c:if>

    <form action="${pageContext.request.contextPath}/courier/link-account" method="post" autocomplete="on">
      <div class="input-group">
        <label for="legacyPassword">Courier Account Password</label>
        <input
                type="password"
                id="legacyPassword"
                name="legacyPassword"
                placeholder="Enter previous courier password"
                required
                autocomplete="current-password"
        />
      </div>

      <button type="submit" class="auth-btn" id="submitBtn">Verify &amp; Link Account</button>
    </form>

    <div class="footer-link" style="margin-top: 1.5rem;">
      <a href="${pageContext.request.contextPath}/">← Return to Main Hub</a>
    </div>
  </div>
</div>

</body>
</html>

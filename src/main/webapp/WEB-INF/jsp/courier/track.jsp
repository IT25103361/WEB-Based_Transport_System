<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Track parcel — Courier</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="ctx" content="${pageContext.request.contextPath}" />

  <script>
    (function(){
      try {
        document.documentElement.setAttribute(
          "data-theme",
          localStorage.getItem("courier.theme") || "light"
        );
      } catch (e) { document.documentElement.setAttribute("data-theme","light"); }
    })();
  </script>

  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css" />

  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js"></script>
  <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
  <script src="${pageContext.request.contextPath}/js/api.js"></script>
</head>
<body>

<div class="app-header">
  <div class="brand"><a href="${pageContext.request.contextPath}/login" style="color:#fff;">courier<span class="dot">.</span></a></div>
  <div class="who">
    <button class="btn-ghost-dark" data-theme-btn>🌙</button>
    <a href="${pageContext.request.contextPath}/login" style="color:#cfd2d8;">Sign in</a>
  </div>
</div>

<div class="app-main" style="max-width: 760px;">
  <div class="card">
    <div class="search-card">
      <input id="codeInput" placeholder="Enter tracking code, e.g. CR-AB12CD" />
      <button class="btn btn-brand" id="searchBtn">Track</button>
    </div>
  </div>

  <div id="result"></div>
</div>

<script src="${pageContext.request.contextPath}/js/track.js"></script>
</body>
</html>

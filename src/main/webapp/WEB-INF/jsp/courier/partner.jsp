<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Partner dashboard — Courier</title>
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
  <script src="${pageContext.request.contextPath}/js/api.js"></script>
</head>
<body>

<div class="top-header">
  <div class="logo">courier<span>.</span></div>
  <div class="header-right">
    <span class="who-name">
      <span class="status-dot" id="statusDot"></span>
      👋 <b id="whoName">Partner</b>
    </span>
    <button class="icon-round" data-theme-btn title="Toggle theme">🌙</button>
    <button class="btn-logout" id="logoutBtn">Log out</button>
  </div>
</div>

<div class="main-wrap" id="root">
  <div class="tabs" id="tabsBar">
    <div class="tab-glider" id="tabGlider"></div>
    <div class="tab active" data-tab="deliveries">Deliveries</div>
    <div class="tab" data-tab="requests">Requests</div>
    <div class="tab" data-tab="earnings">Earnings</div>
  </div>

  <div id="tab-deliveries"></div>
  <div id="tab-requests" style="display:none;"></div>
  <div id="tab-earnings" style="display:none;"></div>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script src="${pageContext.request.contextPath}/js/partner.js"></script>
</body>
</html>
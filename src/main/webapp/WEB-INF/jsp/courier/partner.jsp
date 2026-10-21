<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Partner dashboard — Courier</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="ctx" content="${pageContext.request.contextPath}" />
  <meta name="user-name" content="${courierUserName}" />
  <meta name="user-role" content="${courierUserRole}" />
  <meta name="user-email" content="${courierUserEmail}" />

  <!-- Shared NextGo Theme -->
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css" />
  <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>

  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css" />
  <script src="${pageContext.request.contextPath}/js/api.js"></script>
</head>
<body>

<div class="top-header">
  <div class="logo">
    <a href="${pageContext.request.contextPath}/courier" style="color:inherit;text-decoration:none;">courier<span>.</span></a>
    <a href="${pageContext.request.contextPath}/" style="font-size:0.85rem;margin-left:14px;color:var(--text-muted,#71717a);text-decoration:none;">← Main Hub</a>
  </div>
  <div class="header-right">
    <button type="button" class="btn-track-nav" id="navTrackBtn" title="Track a parcel">
      📦 Track a parcel
    </button>
    <a href="${pageContext.request.contextPath}/courier/sender" style="padding:6px 12px;font-size:0.85rem;border-radius:8px;background:var(--card-bg,#fff);border:1px solid var(--border,#e4e4e7);color:var(--text-main,#09090b);text-decoration:none;font-weight:600;display:inline-flex;align-items:center;gap:6px;margin-right:8px;">📦 Send Parcel</a>
    <span class="who-name">
      <span class="status-dot" id="statusDot"></span>
      👋 <b id="whoName"><c:out value="${courierUserName != null ? courierUserName : 'Partner'}" /></b>
    </span>
    <button type="button" class="icon-round" data-theme-btn title="Toggle theme">🌙</button>
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

<!-- Track parcel modal dialog -->
<div id="trackModal" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true" aria-labelledby="trackModalTitle">
  <div class="modal">
    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:14px;">
      <h3 id="trackModalTitle" style="margin:0;">Track your parcel</h3>
      <button type="button" class="btn-ghost-close" id="closeTrackModalBtn" aria-label="Close modal" style="background:none; border:none; font-size:1.4rem; color:var(--text-muted); cursor:pointer; padding:2px 8px; border-radius:6px; line-height:1;">&times;</button>
    </div>
    <form id="trackModalForm" onsubmit="return false;">
      <div class="input-group" style="margin-bottom:12px;">
        <label for="trackCodeInput" style="font-weight:700; margin-bottom:6px; display:block;">Tracking ID</label>
        <input id="trackCodeInput" type="text" autocomplete="off" spellcheck="false" placeholder="e.g. CR-9A4B2C" required />
        <div class="field-hint" style="margin-top:6px;">Enter your tracking code (e.g. CR-9A4B2C) to view live delivery status.</div>
        <div id="trackModalError" class="message error" style="display:none; margin-top:10px; padding:10px 12px; font-size:0.85rem;"></div>
      </div>
      <div class="modal-actions" style="margin-top:18px;">
        <button type="button" class="btn btn-outline" id="cancelTrackModalBtn">Cancel</button>
        <button type="submit" class="btn btn-brand" id="submitTrackModalBtn">Track parcel</button>
      </div>
    </form>
  </div>
</div>

<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<script src="${pageContext.request.contextPath}/js/partner.js"></script>
</body>
</html>
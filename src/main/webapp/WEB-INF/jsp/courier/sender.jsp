<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <title>Send a parcel — Courier</title>
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
    <c:choose>
      <c:when test="${courierUserRole == 'PARTNER'}">
        <a href="${pageContext.request.contextPath}/courier/partner" style="padding:6px 12px;font-size:0.85rem;border-radius:8px;background:var(--card-bg,#fff);border:1px solid var(--border,#e4e4e7);color:var(--text-main,#09090b);text-decoration:none;font-weight:600;display:inline-flex;align-items:center;gap:6px;margin-right:8px;">🛵 Partner Dashboard</a>
      </c:when>
      <c:otherwise>
        <a href="${pageContext.request.contextPath}/courier/register-partner" style="padding:6px 12px;font-size:0.85rem;border-radius:8px;background:rgba(34,197,94,0.1);border:1px solid rgba(34,197,94,0.25);color:#15803d;text-decoration:none;font-weight:600;display:inline-flex;align-items:center;gap:6px;margin-right:8px;">🛵 Deliver with us</a>
      </c:otherwise>
    </c:choose>
    <span class="who-name">👋 <b id="whoName"><c:out value="${courierUserName != null ? courierUserName : 'Sender'}" /></b></span>
    <button type="button" class="icon-round" data-theme-btn title="Toggle theme">🌙</button>
    <button class="btn-logout" id="logoutBtn">Log out</button>
  </div>
</div>

<div class="main-wrap">
  <div class="tabs" id="tabsBar">
    <div class="tab-glider" id="tabGlider"></div>
    <div class="tab active" data-tab="ongoing">Ongoing</div>
    <div class="tab" data-tab="new">New delivery</div>
    <div class="tab" data-tab="history">History</div>
  </div>

  <div id="tab-ongoing">
    <div class="card">
      <h2>Ongoing deliveries</h2>
      <span class="sub">Live status of everything in flight</span>
      <div id="ongoingList"><div class="skeleton" style="height:90px;"></div></div>
    </div>
  </div>

  <div id="tab-new" style="display:none;">
    <div class="card">
      <h2>Book a delivery</h2>
      <span class="sub">Drop two pins on the map, we'll handle the rest</span>

      <form id="parcelForm">
        <label>Service</label>
        <div class="tag-row">
          <div class="chip active" id="tierInstant">⚡ Instant (within city)</div>
          <div class="chip" id="tierIslandwide">🚚 Islandwide</div>
        </div>

        <div class="grid-2" style="margin-top:16px;">
          <div class="input-group">
            <label>Receiver name <span class="req">*</span></label>
            <input id="receiverName" required placeholder="Receiver's full name" />
          </div>
          <div class="input-group">
            <label>Receiver phone</label>
            <input id="receiverPhone" placeholder="07XXXXXXXX" />
          </div>
        </div>
        <div class="input-group">
          <label>Receiver email <span class="req">*</span></label>
          <input id="receiverEmail" type="email" required placeholder="receiver@example.com" />
        </div>

        <hr class="sep" />

        <label>Pickup &amp; drop-off pins <span class="req">*</span></label>
        <div class="tag-row">
          <div class="chip active" id="pinPickup">📍 Setting pickup</div>
          <div class="chip" id="pinDrop">🏁 Setting drop-off</div>
        </div>
        <div id="map" class="map-box"></div>
        <div class="pin-status">
          <span id="pickupStatus" class="missing">📍 Pickup not set</span>
          <span id="dropStatus" class="missing">🏁 Drop-off not set</span>
        </div>
        <div class="map-hint" id="mapHint">Tap the map to place the pickup pin, then switch to drop-off. Drag a pin to adjust.</div>

        <div class="grid-2" style="margin-top:14px;">
          <div class="input-group">
            <label>Pickup address <span style="font-weight:500;color:var(--text-muted);">(optional)</span></label>
            <input id="pickupAddress" placeholder="e.g. Blue Gate, near the school" />
          </div>
          <div class="input-group">
            <label>Drop-off address <span style="font-weight:500;color:var(--text-muted);">(optional)</span></label>
            <input id="dropAddress" placeholder="e.g. 2nd floor, red building" />
          </div>
        </div>
        <div class="field-hint" style="margin-top:-6px;margin-bottom:6px;">
          Your map pins are what actually matter — only add this if there's something the map can't show, like a gate name or floor number.
        </div>

        <hr class="sep" />

        <div class="grid-2">
          <div class="input-group">
            <label>Weight (kg) <span class="req">*</span></label>
            <input id="weightKg" type="number" step="0.1" min="0.1" value="1" required />
          </div>
          <div class="input-group">
            <label>Size</label>
            <select id="size">
              <option value="SMALL">Small</option>
              <option value="MEDIUM" selected>Medium</option>
              <option value="LARGE">Large</option>
            </select>
          </div>
        </div>

        <div class="input-group" id="scheduleWrap" style="display:none;">
          <label>Preferred pickup time</label>
          <input id="scheduledAt" type="datetime-local" />
        </div>

        <div class="price-box">
          <div>
            <div class="lbl">Estimated price</div>
            <div class="amt" id="priceEstimate">—</div>
          </div>
          <div class="field-hint" style="text-align:right;max-width:180px;">
            Instant = base + distance.<br>Islandwide = base + weight.
          </div>
        </div>

        <div id="formErr" class="message error" style="display:none;"></div>
        <div id="formOk" class="message success" style="display:none;"></div>
        <button class="btn btn-primary" style="margin-top:18px;" id="submitBtn" type="submit">Book this delivery</button>
      </form>
    </div>
  </div>

  <div id="tab-history" style="display:none;">
    <div class="card">
      <h2>Past deliveries</h2>
      <div id="historyList"><div class="skeleton" style="height:80px;"></div></div>
    </div>
  </div>
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
<script src="${pageContext.request.contextPath}/js/sender.js"></script>
</body>
</html>

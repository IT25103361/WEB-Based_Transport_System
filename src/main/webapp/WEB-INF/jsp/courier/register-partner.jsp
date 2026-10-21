<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="ctx" content="${pageContext.request.contextPath}" />
    <meta name="user-name" content="${courierUserName != null ? courierUserName : sharedUser.fullName}" />
    <meta name="user-email" content="${courierUserEmail != null ? courierUserEmail : sharedUser.email}" />
    <title>Courier | Deliver with NEXT GO</title>
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
        <div class="logo">
            <span class="icon">🛵</span>
            courier<span>.</span>
        </div>
        <h2>Deliver parcels</h2>
        <p class="tag">Complete your partner profile to start earning on your schedule</p>

        <div style="background: rgba(34, 197, 94, 0.08); border: 1px solid rgba(34, 197, 94, 0.25); border-radius: 8px; padding: 12px; margin-bottom: 18px; font-size: 0.88rem; color: var(--text-muted, #475569);">
            Signed in as: <strong><c:out value="${courierUserName != null ? courierUserName : sharedUser.fullName}" /></strong> (<c:out value="${courierUserEmail != null ? courierUserEmail : sharedUser.email}" />)
        </div>

        <c:if test="${not empty error}">
            <div style="background: #fef2f2; border: 1px solid #fecaca; color: #b91c1c; padding: 10px 14px; border-radius: 8px; margin-bottom: 16px; font-size: 0.88rem;">
                <c:out value="${error}" />
            </div>
        </c:if>

        <div id="msgBox"></div>

        <form id="regForm" action="${pageContext.request.contextPath}/courier/register-partner" method="post">
            <div class="input-group">
                <label for="vehicleType">Vehicle Type</label>
                <select id="vehicleType" name="vehicleType" required style="width:100%;padding:10px;border-radius:8px;border:1px solid var(--border,#d1d5db);background:var(--input-bg,#fff);color:inherit;font-size:0.95rem;">
                    <option value="BIKE" selected>Motorcycle / Scooter (Bike)</option>
                    <option value="CAR">Car</option>
                    <option value="VAN">Van</option>
                    <option value="TRUCK">Truck</option>
                </select>
            </div>

            <div class="input-group">
                <label for="phone">Phone Number</label>
                <input type="tel" id="phone" name="phone" placeholder="07XXXXXXXX" pattern="[0-9]{10}" title="Enter a 10-digit number" />
            </div>

            <button type="submit" class="auth-btn" id="submitBtn">Start Delivering</button>
        </form>

        <div class="footer-link">
            Want to send parcels instead? <a href="${pageContext.request.contextPath}/courier/sender">Send a parcel</a>
        </div>
        <div class="footer-link" style="margin-top: 0.8rem;">
            <a href="${pageContext.request.contextPath}/">← Return to Main Hub</a>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/js/register-partner.js"></script>
</body>
</html>

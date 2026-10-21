<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Operations Overview Dashboard — NEXT GO</title>
    <!-- Google Fonts: Inter -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Bootstrap 5 & FontAwesome -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <!-- Scoped Admin Stylesheet -->
    <link href="${pageContext.request.contextPath}/admin/css/admin-dashboard.css" rel="stylesheet">
    <!-- Shared NextGo Theme -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/nextgo-theme.css">
    <script src="${pageContext.request.contextPath}/js/nextgo-theme.js"></script>
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>
<body class="admin-body">

<div class="admin-dashboard-app">

    <!-- ==========================================
         SIDEBAR (Dark Navy)
         ========================================== -->
    <aside class="admin-sidebar" aria-label="Admin Navigation">
        <div class="admin-sidebar-header">
            <a href="/admin/dashboard" class="admin-brand">
                <div class="admin-brand-icon">
                    <i class="fa-solid fa-chart-line"></i>
                </div>
                <div>
                    <div class="admin-brand-text">NEXT GO</div>
                    <span class="admin-brand-badge">Operations Portal</span>
                </div>
            </a>
        </div>

        <nav class="admin-sidebar-menu">
            <div class="admin-menu-header">Command Center</div>
            <a href="/admin/dashboard" class="admin-nav-item active">
                <i class="fa-solid fa-table-columns"></i>
                <span>All Modules Overview</span>
            </a>

            <div class="admin-menu-header">Six Core Modules</div>
            <a href="javascript:void(0)" class="admin-nav-item" onclick="openModuleDetail('ride')">
                <i class="fa-solid fa-taxi text-info"></i>
                <span>1. Ride Management</span>
            </a>
            <a href="javascript:void(0)" class="admin-nav-item" onclick="openModuleDetail('courier')">
                <i class="fa-solid fa-box-open text-success"></i>
                <span>2. Courier Delivery</span>
            </a>
            <a href="javascript:void(0)" class="admin-nav-item" onclick="openModuleDetail('school')">
                <i class="fa-solid fa-school-flag text-warning"></i>
                <span>3. School Transport</span>
            </a>
            <a href="javascript:void(0)" class="admin-nav-item" onclick="openModuleDetail('food')">
                <i class="fa-solid fa-utensils text-danger"></i>
                <span>4. Food & Grocery</span>
            </a>
            <a href="javascript:void(0)" class="admin-nav-item" onclick="openModuleDetail('rental')">
                <i class="fa-solid fa-car-side" style="color: #a78bfa;"></i>
                <span>5. Vehicle Rental</span>
            </a>
            <a href="javascript:void(0)" class="admin-nav-item" onclick="openModuleDetail('emergency')">
                <i class="fa-solid fa-truck-medical text-secondary"></i>
                <span>6. Emergency Services</span>
            </a>
        </nav>

        <div class="admin-sidebar-footer">
            <div class="admin-profile-pill">
                <div class="admin-avatar">
                    <c:out value="${adminUser.fullName != null && adminUser.fullName.length() > 0 ? adminUser.fullName.substring(0,1).toUpperCase() : 'A'}"/>
                </div>
                <div class="admin-profile-info">
                    <div class="admin-profile-name"><c:out value="${adminUser.fullName != null ? adminUser.fullName : 'Administrator'}"/></div>
                    <div class="admin-profile-role"><i class="fa-solid fa-user-shield me-1"></i>ADMINISTRATOR</div>
                </div>
            </div>
            <div class="d-flex gap-2">
                <a href="/ride/home" class="btn btn-outline-light btn-sm w-50 py-1" style="font-size: 0.76rem;" title="Go to Main Hub">
                    <i class="fa-solid fa-house me-1"></i> Main Hub
                </a>
                <a href="/admin/logout" class="btn btn-outline-danger btn-sm w-50 py-1" style="font-size: 0.76rem;" title="Sign out of Admin">
                    <i class="fa-solid fa-right-from-bracket me-1"></i> Sign Out
                </a>
            </div>
        </div>
    </aside>

    <!-- ==========================================
         MAIN CONTENT AREA
         ========================================== -->
    <main class="admin-main-content">

        <!-- Top Bar -->
        <header class="admin-topbar">
            <div class="admin-topbar-left">
                <button type="button" class="admin-mobile-toggle" id="adminMobileToggle" aria-label="Toggle navigation">
                    <i class="fa-solid fa-bars"></i>
                </button>
                <div>
                    <h1 class="admin-page-title">Operations Overview</h1>
                    <div class="d-flex align-items-center gap-2 mt-1">
                        <span class="admin-timezone-badge">
                            <i class="fa-solid fa-clock text-info"></i>
                            <span>Timezone: <strong>Asia/Colombo (UTC+05:30)</strong></span>
                        </span>
                        <span class="text-muted small d-none d-md-inline">
                            Last refreshed: <strong id="lastRefreshedTime">${overview.lastRefreshed}</strong>
                        </span>
                    </div>
                </div>
            </div>

            <!-- Date Range Controls & Actions -->
            <div class="admin-topbar-actions">
                <div class="admin-filter-group" role="group" aria-label="Date range filter">
                    <button type="button" class="admin-filter-pill ${overview.filterPreset == 'today' ? 'active' : ''}" data-range="today">Today</button>
                    <button type="button" class="admin-filter-pill ${overview.filterPreset == '7days' ? 'active' : ''}" data-range="7days">Last 7 Days</button>
                    <button type="button" class="admin-filter-pill ${overview.filterPreset == '30days' ? 'active' : ''}" data-range="30days">Last 30 Days</button>
                    <button type="button" class="admin-filter-pill ${overview.filterPreset == 'custom' ? 'active' : ''}" data-range="custom">Custom</button>
                </div>

                <div id="customDatesWrapper" class="admin-custom-dates ${overview.filterPreset == 'custom' ? '' : 'd-none'}">
                    <input type="date" id="customStartDate" class="admin-date-input" value="${overview.startDate}">
                    <span class="text-muted small">to</span>
                    <input type="date" id="customEndDate" class="admin-date-input" value="${overview.endDate}">
                    <button type="button" id="applyCustomRangeBtn" class="btn btn-sm btn-primary py-1 px-2" style="font-size: 0.78rem;">Apply</button>
                </div>

                <button type="button" class="nextgo-theme-toggle" data-nextgo-theme-toggle title="Toggle light/dark theme" aria-label="Toggle light/dark theme">🌙</button>

                <button type="button" class="admin-refresh-btn" id="adminRefreshBtn" title="Refresh dashboard data">
                    <i class="fa-solid fa-arrows-rotate"></i>
                    <span>Refresh</span>
                </button>

                <a href="/ride/home" class="btn btn-light btn-sm border d-none d-xl-flex align-items-center gap-2" style="font-size: 0.82rem; font-weight: 600;">
                    <i class="fa-solid fa-globe text-primary"></i>
                    <span>Main Hub</span>
                </a>
            </div>
        </header>

        <!-- Main Dashboard Container -->
        <div class="admin-container">

            <!-- ==========================================
                 SECTION A: Overview Metric Cards
                 ========================================== -->
            <div class="row g-4 mb-4">
                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label">Total System Operations</span>
                            <div class="admin-kpi-icon-box" style="background: rgba(6, 182, 212, 0.1); color: #0284c7;">
                                <i class="fa-solid fa-cubes"></i>
                            </div>
                        </div>
                        <div class="admin-kpi-val" id="kpiTotalOps">${overview.totalSystemOperations}</div>
                        <div class="admin-kpi-sub">
                            <i class="fa-solid fa-circle-check text-success me-1"></i>
                            Active operations across all 6 modules
                        </div>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label">Active / In-Progress</span>
                            <div class="admin-kpi-icon-box" style="background: rgba(14, 165, 233, 0.1); color: #0ea5e9;">
                                <i class="fa-solid fa-spinner fa-spin-pulse"></i>
                            </div>
                        </div>
                        <div class="admin-kpi-val" id="kpiActiveOps">${overview.activeOperations}</div>
                        <div class="admin-kpi-sub">
                            Pending rides, rentals & school bus schedules
                        </div>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label">Completed Operations</span>
                            <div class="admin-kpi-icon-box" style="background: rgba(16, 185, 129, 0.1); color: #10b981;">
                                <i class="fa-solid fa-circle-check"></i>
                            </div>
                        </div>
                        <div class="admin-kpi-val" id="kpiCompletedOps">${overview.completedOperations}</div>
                        <div class="admin-kpi-sub">
                            Completed/paid rides & returned rentals
                        </div>
                    </div>
                </div>

                <div class="col-sm-6 col-xl-3">
                    <div class="admin-kpi-card">
                        <div class="admin-kpi-header">
                            <span class="admin-kpi-label">Fleet & Resource Capacity</span>
                            <div class="admin-kpi-icon-box" style="background: rgba(139, 92, 246, 0.1); color: #8b5cf6;">
                                <i class="fa-solid fa-truck-ramp-box"></i>
                            </div>
                        </div>
                        <div class="admin-kpi-val" id="kpiFleetCapacity">${overview.totalFleetCapacity}</div>
                        <div class="admin-kpi-sub">
                            Registered Drivers, Buses & Rental Vehicles
                        </div>
                    </div>
                </div>
            </div>

            <!-- Financial Highlights Banner (Strictly Segregated & Disclaimed) -->
            <div class="admin-finance-banner shadow-sm">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <div>
                        <div class="admin-finance-title">
                            <i class="fa-solid fa-coins"></i>
                            <span>Financial Indicators & Verified Revenue</span>
                        </div>
                        <div class="admin-finance-disclaimer">
                            Financial metrics are strictly segregated based on actual verified transaction states. Estimated fares and uncollected balances are not conflated with revenue.
                        </div>
                    </div>
                    <span class="badge bg-light text-dark px-3 py-2 fw-semibold">Currency: LKR</span>
                </div>

                <div class="row g-4 mt-1">
                    <div class="col-md-4 admin-fin-col">
                        <div class="admin-fin-label">Collected Ride Revenue (Verified Paid)</div>
                        <div class="admin-fin-val" id="finPaidRevenue">${overview.formattedCollectedRideRevenue}</div>
                        <div class="admin-fin-note"><i class="fa-solid fa-check-double me-1"></i>Strictly from completed rides in 'PAID' status</div>
                    </div>
                    <div class="col-md-4 admin-fin-col">
                        <div class="admin-fin-label">Pending / Estimated Ride Fares</div>
                        <div class="admin-fin-val" id="finPendingFares">${overview.formattedPendingRideFares}</div>
                        <div class="admin-fin-note"><i class="fa-solid fa-clock me-1"></i>Active / pending rides (uncollected estimation)</div>
                    </div>
                    <div class="col-md-4 admin-fin-col">
                        <div class="admin-fin-label">Agreed Vehicle Rental Value</div>
                        <div class="admin-fin-val" id="finRentalAgreed">${overview.formattedAgreedRentalValue}</div>
                        <div class="admin-fin-note"><i class="fa-solid fa-file-contract me-1"></i>Confirmed & completed rental contracts</div>
                    </div>
                </div>
            </div>

            <!-- ==========================================
                 SECTION B: Activity Trends & Status Distribution Charts
                 ========================================== -->
            <div class="row g-4 mb-4">
                <div class="col-lg-8">
                    <div class="admin-chart-card h-100">
                        <div class="admin-chart-header">
                            <div>
                                <h3 class="admin-chart-title"><i class="fa-solid fa-chart-line me-2 text-primary"></i> Activity Trends Over Time</h3>
                                <div class="text-muted small">Daily operational volume across active transport modules</div>
                            </div>
                            <span class="badge bg-light text-dark border">Interval: ${overview.startDate} to ${overview.endDate}</span>
                        </div>
                        <div class="admin-chart-wrapper">
                            <canvas id="activityTrendChart" height="280"></canvas>
                        </div>
                    </div>
                </div>

                <div class="col-lg-4">
                    <div class="admin-chart-card h-100">
                        <div class="admin-chart-header">
                            <div>
                                <h3 class="admin-chart-title"><i class="fa-solid fa-chart-pie me-2 text-info"></i> Operations Status Distribution</h3>
                                <div class="text-muted small">Overall lifecycle distribution</div>
                            </div>
                        </div>
                        <div class="admin-chart-wrapper d-flex align-items-center justify-content-center">
                            <canvas id="statusDonutChart" height="260"></canvas>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ==========================================
                 SECTION C: Six Module Summary Cards
                 ========================================== -->
            <div class="d-flex align-items-center justify-content-between mb-3">
                <h4 class="fw-bold mb-0 text-dark"><i class="fa-solid fa-cubes me-2 text-primary"></i> Six Core Project Modules</h4>
                <span class="badge bg-light text-muted border">6 Integrated Team Modules</span>
            </div>

            <div class="row g-4 mb-4">
                <c:forEach var="m" items="${overview.modules}">
                    <div class="col-md-6 col-xl-4" id="module-${m.moduleKey}">
                        <div class="admin-module-card" style="border-top: 4px solid ${m.accentColor};">
                            <div class="admin-module-header">
                                <div>
                                    <span class="admin-mod-num">Module 0${m.moduleNumber}</span>
                                    <h5 class="admin-mod-title">${m.moduleName}</h5>
                                </div>
                                <div class="admin-mod-icon" style="background: ${m.accentColor}1a; color: ${m.accentColor};">
                                    <i class="${m.iconClass}"></i>
                                </div>
                            </div>

                            <div>
                                <c:choose>
                                    <c:when test="${m.hasRealData}">
                                        <div class="d-flex align-items-baseline gap-2 mb-2">
                                            <span class="display-6 fw-bold text-dark">${m.headlineMetricValue}</span>
                                            <span class="text-muted small">${m.headlineMetricLabel}</span>
                                        </div>
                                        <div class="admin-mod-stats">
                                            <c:forEach var="entry" items="${m.subMetrics}">
                                                <div class="admin-stat-item">
                                                    <span>${entry.key}</span>
                                                    <strong>${entry.value}</strong>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="p-3 bg-light border rounded-3 my-2 text-center">
                                            <span class="badge bg-secondary mb-2"><i class="fa-solid fa-code me-1"></i> In Progress (Member Module)</span>
                                            <p class="text-muted small mb-0">${m.statusMessage}</p>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="admin-mod-footer">
                                <span class="badge bg-${m.statusBadge == 'ACTIVE' ? 'success' : 'warning text-dark'}">
                                    <i class="fa-solid fa-circle-dot me-1"></i> ${m.statusBadge}
                                </span>
                                <button type="button" class="admin-btn-detail" onclick="openModuleDetail('${m.moduleKey}')">
                                    <span>View Details</span>
                                    <i class="fa-solid fa-arrow-right ms-1"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- ==========================================
                 SECTION D: Selected Module Detailed View Panel
                 ========================================== -->
            <section id="selectedModuleDetailPanel" class="admin-detail-panel d-none" aria-live="polite">
                <div class="admin-panel-header">
                    <div class="d-flex align-items-center gap-3">
                        <h4 class="fw-bold mb-0 text-dark" id="detailPanelTitle">Module Granular Details</h4>
                        <span id="detailPanelBadge" class="badge bg-success">ACTIVE</span>
                    </div>
                    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="closeDetailPanel()">
                        <i class="fa-solid fa-xmark me-1"></i> Close Details
                    </button>
                </div>
                <div id="detailPanelBody">
                    <!-- Populated dynamically via AJAX from /admin/api/modules/{moduleKey} -->
                </div>
            </section>

            <!-- ==========================================
                 SECTION E: Recent Cross-Module Activity Table
                 ========================================== -->
            <div class="admin-activity-card">
                <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
                    <div>
                        <h4 class="fw-bold mb-0 text-dark"><i class="fa-solid fa-clock-rotate-left me-2 text-primary"></i> Recent Operations & System Events</h4>
                        <div class="text-muted small">Real-time log of recent passenger requests, vehicle reservations and transport check-ins</div>
                    </div>
                    <span class="badge bg-light text-dark border px-3 py-2">Latest Persisted Records</span>
                </div>

                <div class="table-responsive">
                    <table class="table admin-table align-middle">
                        <thead>
                            <tr>
                                <th>Record Ref</th>
                                <th>Module</th>
                                <th>Event Description</th>
                                <th>Timestamp / Date</th>
                                <th>Financial Value</th>
                                <th class="text-end">Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="act" items="${activities}">
                                <tr>
                                    <td>
                                        <span class="fw-bold text-dark">${act.recordReference}</span>
                                    </td>
                                    <td>
                                        <span class="badge" style="background: ${act.moduleBadgeColor}20; color: ${act.moduleBadgeColor}; font-weight: 600;">
                                            ${act.moduleName}
                                        </span>
                                    </td>
                                    <td>
                                        <div class="text-dark small fw-semibold">${act.description}</div>
                                    </td>
                                    <td>
                                        <span class="text-muted small"><i class="fa-regular fa-clock me-1"></i>${act.formattedTimestamp}</span>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty act.financialValue}">
                                                <span class="fw-bold text-dark small">${act.financialValue}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted small">—</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <span class="${act.statusBadgeClass}">${act.status}</span>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty activities}">
                                <tr>
                                    <td colspan="6" class="text-center py-5 text-muted">
                                        No recent operations recorded in the database.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </main>
</div>

<!-- Serialized Initial Chart Data for Immediate Render -->
<script>
    window.initialTrendsData = [
        <c:forEach var="t" items="${overview.trends}" varStatus="status">
            {
                dateLabel: "${t.dateLabel}",
                rideCount: ${t.rideCount},
                rentalCount: ${t.rentalCount},
                schoolCount: ${t.schoolCount}
            }<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    ];

    window.initialStatusData = {
        <c:forEach var="entry" items="${overview.statusDistribution}" varStatus="status">
            "${entry.key}": ${entry.value}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    };
</script>

<!-- Bootstrap JS & Admin JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/admin/js/admin-dashboard.js"></script>
</body>
</html>

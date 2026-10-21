/**
 * NEXT GO - Operations Overview Admin Dashboard JavaScript
 * Handles Chart.js visualization, date filtering, AJAX refresh, and module deep-dives.
 */

let trendChartInstance = null;
let statusChartInstance = null;

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    setupEventListeners();
});

function initCharts() {
    // 1. Activity Trends Chart
    const trendCtx = document.getElementById('activityTrendChart');
    if (trendCtx && window.initialTrendsData) {
        const labels = window.initialTrendsData.map(d => d.dateLabel);
        const rideData = window.initialTrendsData.map(d => d.rideCount);
        const rentalData = window.initialTrendsData.map(d => d.rentalCount);
        const schoolData = window.initialTrendsData.map(d => d.schoolCount);

        trendChartInstance = new Chart(trendCtx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Ride Bookings',
                        data: rideData,
                        borderColor: '#0284c7',
                        backgroundColor: 'rgba(2, 132, 199, 0.1)',
                        fill: true,
                        tension: 0.3,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    },
                    {
                        label: 'Vehicle Rentals',
                        data: rentalData,
                        borderColor: '#8b5cf6',
                        backgroundColor: 'rgba(139, 92, 246, 0.1)',
                        fill: true,
                        tension: 0.3,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    },
                    {
                        label: 'School Arrivals',
                        data: schoolData,
                        borderColor: '#f59e0b',
                        backgroundColor: 'rgba(245, 158, 11, 0.1)',
                        fill: true,
                        tension: 0.3,
                        pointRadius: 4,
                        pointHoverRadius: 6
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            font: { family: 'Inter', size: 12 }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleFont: { family: 'Inter', weight: 'bold' },
                        bodyFont: { family: 'Inter' },
                        padding: 12,
                        cornerRadius: 8
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { font: { family: 'Inter', size: 11 } }
                    },
                    y: {
                        beginAtZero: true,
                        grid: { color: '#f1f5f9' },
                        ticks: { stepSize: 1, font: { family: 'Inter', size: 11 } }
                    }
                }
            }
        });
    }

    // 2. Status Distribution Donut Chart
    const statusCtx = document.getElementById('statusDonutChart');
    if (statusCtx && window.initialStatusData) {
        const labels = Object.keys(window.initialStatusData);
        const dataValues = Object.values(window.initialStatusData);

        statusChartInstance = new Chart(statusCtx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: dataValues,
                    backgroundColor: [
                        '#10b981', // Completed / Paid
                        '#0284c7', // Active / Confirmed
                        '#f59e0b', // Pending Requests
                        '#ef4444'  // Cancelled
                    ],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 16,
                            font: { family: 'Inter', size: 12 }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        padding: 12,
                        cornerRadius: 8
                    }
                }
            }
        });
    }
}

function setupEventListeners() {
    // Quick filter pills
    document.querySelectorAll('.admin-filter-pill').forEach(btn => {
        btn.addEventListener('click', (e) => {
            document.querySelectorAll('.admin-filter-pill').forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            const range = btn.dataset.range;
            
            const customDates = document.getElementById('customDatesWrapper');
            if (range === 'custom') {
                customDates.classList.remove('d-none');
            } else {
                customDates.classList.add('d-none');
                applyDateFilter(range);
            }
        });
    });

    // Custom date apply button
    const applyCustomBtn = document.getElementById('applyCustomRangeBtn');
    if (applyCustomBtn) {
        applyCustomBtn.addEventListener('click', () => {
            const start = document.getElementById('customStartDate').value;
            const end = document.getElementById('customEndDate').value;
            if (!start || !end) {
                alert('Please select both start and end dates.');
                return;
            }
            applyDateFilter('custom', start, end);
        });
    }

    // Refresh button
    const refreshBtn = document.getElementById('adminRefreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => {
            const activePill = document.querySelector('.admin-filter-pill.active');
            const range = activePill ? activePill.dataset.range : '7days';
            const start = document.getElementById('customStartDate') ? document.getElementById('customStartDate').value : '';
            const end = document.getElementById('customEndDate') ? document.getElementById('customEndDate').value : '';
            applyDateFilter(range, start, end);
        });
    }

    // Mobile sidebar toggle
    const toggleBtn = document.getElementById('adminMobileToggle');
    const sidebar = document.querySelector('.admin-sidebar');
    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }
}

function applyDateFilter(range, start = '', end = '') {
    const refreshIcon = document.querySelector('#adminRefreshBtn i');
    if (refreshIcon) refreshIcon.classList.add('fa-spin');

    let url = `/admin/api/overview?range=${encodeURIComponent(range)}`;
    if (range === 'custom' && start && end) {
        url += `&start=${encodeURIComponent(start)}&end=${encodeURIComponent(end)}`;
    }

    fetch(url)
        .then(res => {
            if (!res.ok) throw new Error('Failed to load overview data');
            return res.json();
        })
        .then(data => {
            updateDashboardDom(data);
        })
        .catch(err => {
            console.error('Error refreshing overview:', err);
        })
        .finally(() => {
            if (refreshIcon) refreshIcon.classList.remove('fa-spin');
        });
}

function updateDashboardDom(data) {
    if (!data) return;

    // Update KPIs
    safeText('kpiTotalOps', data.totalSystemOperations);
    safeText('kpiActiveOps', data.activeOperations);
    safeText('kpiCompletedOps', data.completedOperations);
    safeText('kpiFleetCapacity', data.totalFleetCapacity);

    // Update Financials
    safeText('finPaidRevenue', data.formattedCollectedRideRevenue);
    safeText('finPendingFares', data.formattedPendingRideFares);
    safeText('finRentalAgreed', data.formattedAgreedRentalValue);

    // Update Timestamp
    safeText('lastRefreshedTime', data.lastRefreshed);

    // Update Charts
    if (trendChartInstance && data.trends) {
        trendChartInstance.data.labels = data.trends.map(d => d.dateLabel);
        trendChartInstance.data.datasets[0].data = data.trends.map(d => d.rideCount);
        trendChartInstance.data.datasets[1].data = data.trends.map(d => d.rentalCount);
        trendChartInstance.data.datasets[2].data = data.trends.map(d => d.schoolCount);
        trendChartInstance.update();
    }

    if (statusChartInstance && data.statusDistribution) {
        statusChartInstance.data.labels = Object.keys(data.statusDistribution);
        statusChartInstance.data.datasets[0].data = Object.values(data.statusDistribution);
        statusChartInstance.update();
    }
}

function safeText(elementId, text) {
    const el = document.getElementById(elementId);
    if (el) el.textContent = text;
}

/**
 * Loads and displays deep-dive details for a specific module card.
 */
function openModuleDetail(moduleKey) {
    const panel = document.getElementById('selectedModuleDetailPanel');
    if (!panel) return;

    panel.classList.remove('d-none');
    panel.scrollIntoView({ behavior: 'smooth' });

    const titleEl = document.getElementById('detailPanelTitle');
    const badgeEl = document.getElementById('detailPanelBadge');
    const bodyEl = document.getElementById('detailPanelBody');

    if (bodyEl) {
        bodyEl.innerHTML = '<div class="text-center py-5"><i class="fa-solid fa-circle-notch fa-spin fa-2x text-info"></i><p class="text-muted small mt-2">Loading module operational data...</p></div>';
    }

    fetch(`/admin/api/modules/${encodeURIComponent(moduleKey)}`)
        .then(res => res.json())
        .then(details => {
            if (titleEl) titleEl.textContent = details.moduleName;
            if (badgeEl) {
                badgeEl.textContent = details.statusBadge;
                badgeEl.className = details.inProgress ? 'badge bg-secondary' : 'badge bg-success';
            }

            if (details.inProgress) {
                bodyEl.innerHTML = `
                    <div class="p-4 bg-light border rounded-3 text-center">
                        <i class="${details.iconClass} fa-3x text-secondary opacity-50 mb-3"></i>
                        <h5 class="fw-bold">${details.moduleName} — Development In Progress</h5>
                        <p class="text-muted col-lg-8 mx-auto">${details.integrationNotes}</p>
                        <div class="mt-3">
                            <span class="badge bg-warning text-dark px-3 py-2"><i class="fa-solid fa-clock me-1"></i> Scheduled Member Release</span>
                        </div>
                    </div>
                `;
            } else {
                let resourcesHtml = '';
                if (details.resources && details.resources.length > 0) {
                    resourcesHtml = `
                        <h6 class="fw-bold mb-3"><i class="fa-solid fa-layer-group me-2 text-info"></i> Operational Resources</h6>
                        <div class="table-responsive mb-4">
                            <table class="table admin-table align-middle">
                                <thead>
                                    <tr>
                                        ${Object.keys(details.resources[0]).map(k => `<th>${k.toUpperCase()}</th>`).join('')}
                                    </tr>
                                </thead>
                                <tbody>
                                    ${details.resources.map(row => `
                                        <tr>
                                            ${Object.values(row).map(val => `<td>${val}</td>`).join('')}
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    `;
                }

                let recordsHtml = '';
                if (details.records && details.records.length > 0) {
                    recordsHtml = `
                        <h6 class="fw-bold mb-3"><i class="fa-solid fa-clock-rotate-left me-2 text-primary"></i> Recent Module Records</h6>
                        <div class="table-responsive">
                            <table class="table admin-table align-middle">
                                <thead>
                                    <tr>
                                        ${Object.keys(details.records[0]).map(k => `<th>${k.toUpperCase()}</th>`).join('')}
                                    </tr>
                                </thead>
                                <tbody>
                                    ${details.records.map(row => `
                                        <tr>
                                            ${Object.values(row).map(val => `<td>${val}</td>`).join('')}
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    `;
                }

                bodyEl.innerHTML = resourcesHtml + recordsHtml;
            }
        })
        .catch(err => {
            console.error('Failed to load module details:', err);
            bodyEl.innerHTML = '<div class="alert alert-danger">Failed to load module details. Please retry.</div>';
        });
}

function closeDetailPanel() {
    const panel = document.getElementById('selectedModuleDetailPanel');
    if (panel) panel.classList.add('d-none');
}

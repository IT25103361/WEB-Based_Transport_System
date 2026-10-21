document.addEventListener('DOMContentLoaded', () => {
    const mobileToggle = document.getElementById('adminMobileToggle');
    const sidebar = document.querySelector('.admin-sidebar');
    
    if (mobileToggle && sidebar) {
        mobileToggle.addEventListener('click', () => {
            sidebar.classList.toggle('show');
        });
    }

    // Refresh btn
    const refreshBtn = document.getElementById('adminRefreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', () => {
            window.location.reload();
        });
    }
});

function openModuleDetail(key) {
    if(key === 'food') {
        window.location.href = '/shops';
    } else {
        alert('Module ' + key + ' is managed by another team member.');
    }
}

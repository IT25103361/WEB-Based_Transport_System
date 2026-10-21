/**
 * admin.js — Admin dashboard and user management logic
 */

let allUsers = [];
let currentRoleFilter = 'ALL';

document.addEventListener('DOMContentLoaded', async () => {
  // Ensure only ADMIN can access this page
  requireAuth(['ADMIN']);
  
  await loadDashboardStats();
  await loadUsers();
  setupEventListeners();
});

async function loadDashboardStats() {
  try {
    const [orders, shops] = await Promise.all([
      api.get('/api/orders/all'),
      api.get('/api/shops/all')
    ]);
    document.getElementById('stat-orders').textContent = orders.length;
    document.getElementById('stat-shops').textContent = shops.length;
  } catch (err) {
    console.error('Error loading stats:', err);
  }
}

async function loadUsers() {
  showLoading('users-table-container');
  try {
    allUsers = await api.get('/api/auth/users');
    
    // Update stats based on loaded users
    document.getElementById('stat-customers').textContent = allUsers.filter(u => u.role === 'CUSTOMER').length;
    document.getElementById('stat-partners').textContent = allUsers.filter(u => u.role === 'DELIVERY_PARTNER').length;

    renderTable(applyFilter(allUsers));
  } catch (err) {
    toast('Failed to load users: ' + err.message, 'error');
    showEmptyState('users-table-container', 'Failed to load data', '⚠️');
  }
}

function setupEventListeners() {
  const btnRefresh = document.getElementById('btn-refresh');
  if (btnRefresh) {
    btnRefresh.addEventListener('click', () => {
      loadDashboardStats();
      loadUsers();
    });
  }

  document.getElementById('btn-add-user').addEventListener('click', () => {
    document.getElementById('user-form').reset();
    openModal('user-modal');
  });

  document.getElementById('btn-save-user').addEventListener('click', saveUser);

  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentRoleFilter = chip.dataset.role;
      renderTable(applyFilter(allUsers));
    });
  });

  document.getElementById('search-input').addEventListener('input', debounce(() => {
    renderTable(applyFilter(allUsers));
  }, 300));
}

function applyFilter(users) {
  const query = document.getElementById('search-input').value.toLowerCase();
  return users.filter(u => {
    const matchRole = currentRoleFilter === 'ALL' || u.role === currentRoleFilter;
    const matchQuery = !query || 
                       (u.fullName || '').toLowerCase().includes(query) || 
                       (u.email || '').toLowerCase().includes(query);
    return matchRole && matchQuery;
  });
}

function renderTable(users) {
  if (!users.length) {
    showEmptyState('users-table-container', 'No users found.', '👥');
    return;
  }
  document.getElementById('users-table-container').innerHTML = `
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>User</th>
          <th>Role</th>
          <th>Phone</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${users.map(u => `
          <tr>
            <td style="color:var(--clr-text-faint);font-size:0.8rem">#${u.id}</td>
            <td>
              <div style="display:flex;align-items:center;gap:0.5rem">
                <div class="avatar" style="width:36px;height:36px;font-size:0.9rem">${u.fullName?.charAt(0) || '?'}</div>
                <div>
                  <div style="font-weight:600">${escapeHtml(u.fullName)}</div>
                  <div style="font-size:0.75rem;color:var(--clr-text-muted)">${escapeHtml(u.email)}</div>
                </div>
              </div>
            </td>
            <td><span class="badge ${roleBadgeClass(u.role)}">${capitalize(u.role?.replace('_', ' '))}</span></td>
            <td>${escapeHtml(u.phoneNumber || '—')}</td>
            <td><span class="badge ${u.active ? 'badge-success' : 'badge-danger'}">${u.active ? 'Active' : 'Inactive'}</span></td>
            <td>
              <div class="table-actions">
                <button class="btn btn-sm btn-secondary" onclick="toggleStatus(${u.id})">${u.active ? 'Deactivate' : 'Activate'}</button>
                <button class="btn btn-sm btn-danger" onclick="deleteUser(${u.id})">🗑</button>
              </div>
            </td>
          </tr>
        `).join('')}
      </tbody>
    </table>`;
}

function roleBadgeClass(role) {
  if (role === 'ADMIN') return 'badge-danger';
  if (role === 'VENDOR_MANAGER') return 'badge-warning';
  if (role === 'DELIVERY_PARTNER') return 'badge-primary';
  return 'badge-info';
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

async function saveUser() {
  const form = document.getElementById('user-form');
  if (!form.reportValidity()) return;

  const btn = document.getElementById('btn-save-user');
  btn.disabled = true;
  btn.textContent = 'Saving...';

  const payload = {
    fullName: document.getElementById('user-name').value.trim(),
    email: document.getElementById('user-email').value.trim(),
    password: document.getElementById('user-password').value,
    role: document.getElementById('user-role').value,
    phoneNumber: document.getElementById('user-phone').value.trim(),
    gender: document.getElementById('user-gender').value,
    address: document.getElementById('user-address').value.trim(),
    postalCode: document.getElementById('user-postal-code').value.trim()
  };

  try {
    await api.post('/api/auth/add-user', payload);
    toast('User created successfully!', 'success');
    closeModal('user-modal');
    await loadUsers();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Create User';
  }
}

async function toggleStatus(id) {
  if (!await confirmDialog('Are you sure you want to change this user\'s status?')) return;
  try {
    await api.put(`/api/auth/users/${id}/toggle-status`, {});
    toast('Status updated', 'success');
    await loadUsers();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

async function deleteUser(id) {
  if (!await confirmDialog('Are you sure you want to permanently delete this user?')) return;
  try {
    await api.delete(`/api/auth/users/${id}`);
    toast('User deleted', 'success');
    await loadUsers();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

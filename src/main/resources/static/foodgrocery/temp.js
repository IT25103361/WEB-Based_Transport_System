
    const user = JSON.parse(localStorage.getItem('user'));
    if (!user || user.role !== 'ADMIN') window.location.href = 'login.html';

    function logout() {
      localStorage.removeItem('user');
      window.location.href = 'login.html';
    }

    function showToast(message, type = 'info') {
      const container = document.getElementById('toastContainer');
      const toast = document.createElement('div');
      toast.className = `toast ${type}`;
      toast.innerHTML = `<span class="toast-message">${message}</span>`;
      container.appendChild(toast);
      setTimeout(() => { toast.style.animation = 'slideOutRight 0.3s ease forwards'; setTimeout(() => toast.remove(), 300); }, 3000);
    }

    function logout() {
      localStorage.removeItem('user');
      window.location.href = 'login.html';
    }

    function switchTab(tabId, el) {
      document.querySelectorAll('.admin-tab').forEach(t => t.style.display = 'none');
      document.querySelectorAll('.sidebar-item').forEach(i => i.classList.remove('active'));
      document.getElementById('tab-' + tabId).style.display = 'block';
      el.classList.add('active');
    }

    async function loadDashboard() {
      try {
        const [ordersRes, vendorsRes, custRes, ridRes, allUsersRes] = await Promise.all([
          fetch('/api/orders/all'),
          fetch('/api/vendors/all'),
          fetch('/api/auth/customers'),
          fetch('/api/auth/delivery-partners'),
          fetch('/api/auth/customers') // Since we don't have a GET /all-users endpoint yet, let's just combine the lists or create an endpoint if needed. Actually we'll need an endpoint to get ALL users for the admin.
        ]);
        
        // Wait, the backend doesn't have an endpoint to fetch ALL users including VENDOR_MANAGER.
        // For the sake of the dashboard UI, we'll fetch what we have. I will add a GET /all endpoint in a sec.
        const orders = await ordersRes.json();
        const vendors = await vendorsRes.json();
        const customers = await custRes.json();
        const riders = await ridRes.json();

        document.getElementById('dash-orders').textContent = orders.length;
        document.getElementById('dash-vendors').textContent = vendors.length;
        document.getElementById('dash-customers').textContent = customers.length;
        document.getElementById('dash-riders').textContent = riders.length;

        renderOrders(orders);
        renderVendors(vendors);

      } catch (e) {
        showToast('Connection error', 'error');
      }
    }

    async function loadUsers() {
      try {
        const res = await fetch('/api/auth/all-users');
        if (!res.ok) throw new Error('Failed to fetch users');
        const users = await res.json();
        console.log("Users fetched:", users);
        renderUsers(users);
      } catch(e) {
        console.error("Error in loadUsers:", e);
        document.getElementById('allRolesTable').innerHTML = '<tr><td colspan="4" style="color:red;">Error loading roles: ' + e.message + '</td></tr>';
      }
    }

    function renderUsers(users) {
      try {
        const customers = users.filter(u => u && u.role === 'CUSTOMER');
        const staff = users.filter(u => u && u.role !== 'CUSTOMER');

        const userHtml = customers.length === 0 ? '<tr><td colspan="5">No customers found</td></tr>' : customers.map(u => `
          <tr>
            <td>#${u.id || '-'}</td>
            <td><strong>${u.fullName || '-'}</strong></td>
            <td>${u.email || '-'}</td>
            <td>
              <span class="status-chip status-${u.active ? 'delivered' : 'cancelled'}">${u.active ? 'ACTIVE' : 'INACTIVE'}</span>
            </td>
            <td>
              <button class="btn btn-secondary btn-sm" onclick="toggleUserStatus(${u.id})">${u.active ? 'Deactivate' : 'Activate'}</button>
              <button class="btn btn-secondary btn-sm" style="color: red; border-color: red;" onclick="deleteUser(${u.id})">Delete</button>
            </td>
          </tr>
        `).join('');
        document.getElementById('allUsersTable').innerHTML = userHtml;

        const staffHtml = staff.length === 0 ? '<tr><td colspan="5">No roles found</td></tr>' : staff.map(u => {
            const roleStr = u.role ? String(u.role).replace('_', ' ') : 'UNKNOWN';
            return `
          <tr>
            <td>#${u.id || '-'}</td>
            <td><strong>${u.fullName || '-'}</strong></td>
            <td>${u.email || '-'}</td>
            <td><span class="status-chip status-delivered">${roleStr}</span></td>
            <td>
              <button class="btn btn-secondary btn-sm" onclick="toggleUserStatus(${u.id})">${u.active ? 'Deactivate' : 'Activate'}</button>
              <button class="btn btn-secondary btn-sm" style="color: red; border-color: red;" onclick="deleteUser(${u.id})">Delete</button>
            </td>
          </tr>
        `}).join('');
        document.getElementById('allRolesTable').innerHTML = staffHtml;
      } catch (err) {
        document.getElementById('allRolesTable').innerHTML = '<tr><td colspan="5" style="color:red;">Render Error: ' + err.message + '</td></tr>';
      }
    }

    async function toggleUserStatus(id) {
      if(!confirm("Are you sure you want to change this user's status?")) return;
      try {
        const res = await fetch('/api/auth/users/' + id + '/toggle-status', { method: 'PUT' });
        if(res.ok) {
          showToast('User status updated!', 'success');
          loadUsers();
        }
      } catch(e) {
        showToast('Error updating status', 'error');
      }
    }

    async function deleteUser(id) {
      if(!confirm("Are you sure you want to permanently delete this user?")) return;
      try {
        const res = await fetch('/api/auth/users/' + id, { method: 'DELETE' });
        if(res.ok) {
          showToast('User deleted successfully', 'success');
          loadUsers();
        } else {
          const data = await res.json();
          showToast(data.message || 'Error deleting user', 'error');
        }
      } catch(e) {
        showToast('Error deleting user', 'error');
      }
    }

    function openAddUserModal() {
      document.getElementById('addUserModal').classList.add('open');
    }
    
    function closeAddUserModal() {
      document.getElementById('addUserModal').classList.remove('open');
    }

    async function submitAddUser(e) {
      e.preventDefault();
      const btn = document.getElementById('addUserBtn');
      btn.disabled = true;
      btn.textContent = 'Saving...';
      
      const payload = {
        fullName: document.getElementById('add-user-name').value,
        email: document.getElementById('add-user-email').value,
        role: document.getElementById('add-user-role').value,
        password: document.getElementById('add-user-password').value,
        phoneNumber: document.getElementById('add-user-phone').value,
        address: document.getElementById('add-user-address').value,
        postalCode: "00000",
        gender: "OTHER"
      };

      try {
        const res = await fetch('/api/auth/add-user', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        
        if (data.success) {
          showToast('User created successfully!', 'success');
          closeAddUserModal();
          document.getElementById('addUserForm').reset();
          loadUsers();
        } else {
          showToast(data.message, 'error');
        }
      } catch(e) {
        showToast('Error creating user', 'error');
      } finally {
        btn.disabled = false;
        btn.textContent = 'Create User';
      }
    }

    async function openAddVendorModal() {
      document.getElementById('addVendorModal').classList.add('open');
      // Fetch available managers
      const res = await fetch('/api/auth/all-users');
      const users = await res.json();
      const managers = users.filter(u => u.role === 'VENDOR_MANAGER');
      const select = document.getElementById('add-vendor-manager');
      if (managers.length === 0) {
        select.innerHTML = '<option value="">No Vendor Managers found. Create one first!</option>';
      } else {
        select.innerHTML = managers.map(m => `<option value="${m.id}">${m.fullName} (${m.email})</option>`).join('');
      }
    }

    function closeAddVendorModal() {
      document.getElementById('addVendorModal').classList.remove('open');
    }

    async function submitAddVendor(e) {
      e.preventDefault();
      const btn = document.getElementById('addVendorBtn');
      btn.disabled = true;
      btn.textContent = 'Saving...';

      const payload = {
        name: document.getElementById('add-vendor-name').value,
        type: document.getElementById('add-vendor-type').value,
        managerId: document.getElementById('add-vendor-manager').value,
        address: document.getElementById('add-vendor-address').value,
        phone: document.getElementById('add-vendor-phone').value,
        rating: 4.5,
        deliveryFee: 150,
        deliveryTimeMinutes: 30,
        active: true
      };

      if (!payload.managerId) {
        showToast('You must select a manager', 'error');
        btn.disabled = false;
        btn.textContent = 'Create Vendor';
        return;
      }

      try {
        const res = await fetch('/api/vendors', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          showToast('Vendor created!', 'success');
          closeAddVendorModal();
          e.target.reset();
          loadDashboard();
        }
      } catch(e) {
        showToast('Error creating vendor', 'error');
      } finally {
        btn.disabled = false;
        btn.textContent = 'Create Vendor';
      }
    }

    function renderOrders(orders) {
      orders.sort((a,b) => b.id - a.id); // newest first
      const html = orders.map(o => `
        <tr>
          <td>#${o.id}</td>
          <td>${o.customer ? o.customer.fullName : '-'}</td>
          <td>${o.vendor ? o.vendor.name : '-'}</td>
          <td><span class="status-chip status-${o.status.toLowerCase()}">${o.status}</span></td>
          <td>Rs. ${o.totalAmount}</td>
          <td>
            <select class="form-input" style="padding:4px 8px; font-size:12px; width:auto; display:inline-block" onchange="updateOrderStatus(${o.id}, this.value)">
              <option value="">Update...</option>
              <option value="ACCEPTED">Accept</option>
              <option value="PREPARING">Preparing</option>
              <option value="CANCELLED">Cancel</option>
            </select>
          </td>
        </tr>
      `).join('');
      
      document.getElementById('recentOrdersTable').innerHTML = html;
      document.getElementById('allOrdersTable').innerHTML = html;
    }

    function renderVendors(vendors) {
      document.getElementById('allVendorsTable').innerHTML = vendors.map(v => `
        <tr>
          <td>#${v.id}</td>
          <td><strong>${v.name}</strong></td>
          <td>${v.type === 'FOOD' ? 'Restaurant' : 'Grocery'}</td>
          <td>${v.active ? '<span style="color:var(--success)">Active</span>' : '<span style="color:var(--danger)">Inactive</span>'}</td>
          <td>⭐ ${v.rating}</td>
          <td>
            <button class="btn btn-secondary btn-sm" style="color: red; border-color: red;" onclick="hardDeleteVendor(${v.id})">Delete</button>
          </td>
        </tr>
      `).join('');
    }

    async function hardDeleteVendor(id) {
      if(!confirm("Are you sure you want to PERMANENTLY delete this vendor? This cannot be undone.")) return;
      try {
        const res = await fetch('/api/vendors/' + id + '/hard', { method: 'DELETE' });
        if(res.ok) {
          showToast('Vendor permanently deleted!', 'success');
          loadDashboard();
        } else {
          showToast('Error deleting vendor', 'error');
        }
      } catch(e) {
        showToast('Error deleting vendor', 'error');
      }
    }

    async function updateOrderStatus(id, newStatus) {
      if(!newStatus) return;
      try {
        await fetch(`/api/orders/${id}/status`, {
          method: 'PUT', headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ status: newStatus })
        });
        showToast('Order updated', 'success');
        loadDashboard();
      } catch(e) { showToast('Error updating', 'error'); }
    }

    function showToast(message, type = 'info') {
      const container = document.getElementById('toastContainer');
      const toast = document.createElement('div');
      toast.className = `toast ${type}`;
      toast.innerHTML = `<span class="toast-message">${message}</span>`;
      container.appendChild(toast);
      setTimeout(() => { toast.style.animation = 'slideOutRight 0.3s ease forwards'; setTimeout(() => toast.remove(), 300); }, 3000);
    }

    loadDashboard();
  

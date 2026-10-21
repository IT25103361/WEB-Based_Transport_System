/**
 * shops.js — Shop CRUD logic
 */

let allShops = [];
let currentFilter = 'ALL';
let editingShopId = null;

// ── INIT ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  const user = requireAuth(['ADMIN', 'VENDOR_MANAGER']);
  if (user && user.role === 'VENDOR_MANAGER') {
    const titleEl = document.getElementById('page-main-title');
    if (titleEl) titleEl.textContent = 'Vendor Manager Dashboard';
  }
  
  await loadShops();
  setupEventListeners();
});

function setupEventListeners() {
  // Add shop button
  document.getElementById('btn-add-shop').addEventListener('click', () => {
    openAddModal();
  });

  // Save shop
  document.getElementById('btn-save-shop').addEventListener('click', saveShop);

  // Filter chips
  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentFilter = chip.dataset.filter;
      renderTable(applyFilter(allShops));
    });
  });

  // Search
  const searchInput = document.getElementById('search-input');
  searchInput.addEventListener('input', debounce(() => {
    renderTable(applyFilter(allShops));
  }, 300));

  // Form submit on Enter
  document.getElementById('shop-form').addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && e.target.tagName !== 'TEXTAREA') {
      e.preventDefault();
      saveShop();
    }
  });
}

// ── LOAD DATA ─────────────────────────────────────────────
async function loadShops() {
  showLoading('shops-table-container');
  try {
    const [shopsRes, vendorsRes] = await Promise.allSettled([
      api.get('/api/shops/all'),
      api.get('/api/auth/all-users')
    ]);
    
    if (vendorsRes.status === 'fulfilled') {
      const sel = document.getElementById('shop-manager');
      if (sel) {
        sel.innerHTML = '<option value="">-- None --</option>';
        vendorsRes.value.filter(u => u.role === 'VENDOR_MANAGER').forEach(v => {
          const opt = document.createElement('option');
          opt.value = v.id;
          opt.textContent = v.fullName;
          sel.appendChild(opt);
        });
        
        const currentUser = requireAuth(['ADMIN', 'VENDOR_MANAGER']);
        if (currentUser && currentUser.role === 'VENDOR_MANAGER') {
          sel.parentElement.style.display = 'none';
        }
      }
    }

    if (shopsRes.status === 'fulfilled') {
      allShops = shopsRes.value;
      updateStats(allShops);
      renderTable(applyFilter(allShops));
    }
  } catch (err) {
    toast('Failed to load shops: ' + err.message, 'error');
    document.getElementById('shops-table-container').innerHTML =
      `<div class="empty-state"><div class="empty-icon">⚠️</div><p>Failed to load data</p></div>`;
  }
}

function applyFilter(shops) {
  const query = document.getElementById('search-input')?.value.toLowerCase() || '';
  return shops.filter(s => {
    const matchType = currentFilter === 'ALL' || s.type === currentFilter;
    const matchQuery = !query || s.name.toLowerCase().includes(query)
      || (s.city || '').toLowerCase().includes(query)
      || (s.address || '').toLowerCase().includes(query);
    return matchType && matchQuery;
  });
}

// ── STATS ─────────────────────────────────────────────────
function updateStats(shops) {
  document.getElementById('stat-total').textContent = shops.length;
  document.getElementById('stat-active').textContent = shops.filter(s => s.active).length;
  document.getElementById('stat-food').textContent = shops.filter(s => s.type === 'FOOD').length;
  document.getElementById('stat-grocery').textContent = shops.filter(s => s.type === 'GROCERY').length;
}

// ── RENDER TABLE ──────────────────────────────────────────
function renderTable(shops) {
  const container = document.getElementById('shops-table-container');
  if (!shops.length) {
    showEmptyState('shops-table-container', 'No shops found. Add your first shop!', '🏪');
    return;
  }
  container.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Shop</th>
          <th>Type</th>
          <th>City</th>
          <th>Phone</th>
          <th>Hours</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${shops.map(renderRow).join('')}
      </tbody>
    </table>`;
}

function renderRow(shop) {
  return `
    <tr>
      <td style="color:var(--clr-text-faint);font-size:0.8rem">#${shop.id}</td>
      <td>
        <div style="display:flex;align-items:center;gap:0.5rem">
          <div class="avatar" style="width:40px;height:40px;border-radius:8px;overflow:hidden;display:flex;align-items:center;justify-content:center;background:${shop.type === 'FOOD' ? 'var(--gradient-brand)' : 'linear-gradient(135deg,#2EC4B6,#20C997)'}">
            ${shop.imageUrl ? `<img src="${shop.imageUrl}" style="width:100%;height:100%;object-fit:cover;"/>` : (shop.type === 'FOOD' ? '🍔' : (shop.type === 'GROCERY' ? '🥦' : '🏪'))}
          </div>
          <div>
            <div style="font-weight:600">${escapeHtml(shop.name)}</div>
            <div style="font-size:0.75rem;color:var(--clr-text-muted)">${escapeHtml(shop.address || '')}</div>
          </div>
        </div>
      </td>
      <td>${statusBadge(shop.type)}</td>
      <td>${escapeHtml(shop.city || '—')}</td>
      <td>${escapeHtml(shop.phone || '—')}</td>
      <td style="font-size:0.8rem;color:var(--clr-text-muted)">${escapeHtml(shop.openingHours || '—')}</td>
      <td><span class="badge ${shop.active ? 'badge-success' : 'badge-danger'}">${shop.active ? 'Active' : 'Inactive'}</span></td>
      <td onclick="event.stopPropagation()">
        <div class="table-actions">
          <button class="btn btn-sm btn-secondary" onclick="viewShop(${shop.id})" title="View">👁</button>
          <button class="btn btn-sm btn-secondary" onclick="editShop(${shop.id})" title="Edit">✏️</button>
          <button class="btn btn-sm btn-danger" onclick="deleteShop(${shop.id})" title="Delete">🗑</button>
        </div>
      </td>
    </tr>`;
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── ADD MODAL ─────────────────────────────────────────────
function openAddModal() {
  editingShopId = null;
  document.getElementById('modal-title').textContent = 'Add New Shop';
  document.getElementById('shop-form').reset();
  document.getElementById('shop-id').value = '';
  document.getElementById('shop-manager').value = '';
  openModal('shop-modal');
}

// ── EDIT MODAL ────────────────────────────────────────────
async function editShop(id) {
  try {
    const shop = allShops.find(s => s.id === id) || await api.get(`/api/shops/${id}`);
    editingShopId = id;
    document.getElementById('modal-title').textContent = 'Edit Shop';
    document.getElementById('shop-id').value = shop.id;
    document.getElementById('shop-name').value = shop.name || '';
    document.getElementById('shop-type').value = shop.type || '';
    document.getElementById('shop-desc').value = shop.description || '';
    document.getElementById('shop-address').value = shop.address || '';
    document.getElementById('shop-city').value = shop.city || '';
    document.getElementById('shop-phone').value = shop.phone || '';
    document.getElementById('shop-email').value = shop.email || '';
    document.getElementById('shop-hours').value = shop.openingHours || '';
    document.getElementById('shop-image').value = shop.imageUrl || '';
    document.getElementById('shop-manager').value = shop.managerId || '';
    openModal('shop-modal');
  } catch (err) {
    toast('Failed to load shop: ' + err.message, 'error');
  }
}

// ── SAVE (Create / Update) ────────────────────────────────
async function saveShop() {
  const form = document.getElementById('shop-form');
  if (!form.reportValidity()) return;

  const currentUser = requireAuth(['ADMIN', 'VENDOR_MANAGER']);
  let mId = document.getElementById('shop-manager').value;
  if (currentUser && currentUser.role === 'VENDOR_MANAGER') {
    mId = currentUser.id;
  }

  const payload = {
    name: document.getElementById('shop-name').value.trim(),
    type: document.getElementById('shop-type').value,
    description: document.getElementById('shop-desc').value.trim(),
    address: document.getElementById('shop-address').value.trim(),
    city: document.getElementById('shop-city').value.trim(),
    phone: document.getElementById('shop-phone').value.trim(),
    email: document.getElementById('shop-email').value.trim() || null,
    deliveryFee: 150, // Default since field was removed
    deliveryTimeMinutes: 30, // Default since field was removed
    openingHours: document.getElementById('shop-hours').value.trim(),
    imageUrl: document.getElementById('shop-image').value.trim() || null,
    managerId: mId ? parseInt(mId) : null,
    active: true,
  };

  try {
    const btn = document.getElementById('btn-save-shop');
    btn.disabled = true;
    btn.textContent = 'Saving…';

    if (editingShopId) {
      await api.put(`/api/shops/${editingShopId}`, payload);
      toast('Shop updated successfully!', 'success');
    } else {
      await api.post('/api/shops', payload);
      toast('Shop added successfully!', 'success');
    }
    closeModal('shop-modal');
    await loadShops();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  } finally {
    const btn = document.getElementById('btn-save-shop');
    btn.disabled = false;
    btn.textContent = 'Save Shop';
  }
}

// ── VIEW ──────────────────────────────────────────────────
async function viewShop(id) {
  const shop = allShops.find(s => s.id === id);
  if (!shop) return;
  document.getElementById('view-modal-body').innerHTML = `
    <div style="display:flex;flex-direction:column;gap:var(--sp-sm)">
      <div style="display:flex;align-items:center;gap:var(--sp-md);margin-bottom:var(--sp-md)">
        <div class="avatar" style="width:80px;height:80px;border-radius:12px;overflow:hidden;font-size:2rem;display:flex;align-items:center;justify-content:center;
          background:${shop.type === 'FOOD' ? 'var(--gradient-brand)' : 'linear-gradient(135deg,#2EC4B6,#20C997)'}">
          ${shop.imageUrl ? `<img src="${shop.imageUrl}" style="width:100%;height:100%;object-fit:cover;"/>` : (shop.type === 'FOOD' ? '🍔' : (shop.type === 'GROCERY' ? '🥦' : '🏪'))}
        </div>
        <div>
          <h3>${escapeHtml(shop.name)}</h3>
          <div style="color:var(--clr-text-muted);font-size:0.85rem">${escapeHtml(shop.description || '')}</div>
        </div>
      </div>
      ${infoRow('📍 Address', `${shop.address}, ${shop.city}`)}
      ${infoRow('📞 Phone', shop.phone)}
      ${infoRow('📧 Email', shop.email)}
      ${infoRow('🕐 Hours', shop.openingHours)}
      ${infoRow('⭐ Rating', shop.rating?.toFixed(1))}
      ${infoRow('📅 Created', formatDate(shop.createdAt))}
    </div>
    <div style="margin-top:var(--sp-md);display:flex;gap:var(--sp-sm)">
      <button class="btn btn-primary btn-sm" onclick="closeModal('view-modal');editShop(${shop.id})">Edit</button>
      <a href="${getContextPath()}/food/products?shopId=${shop.id}" class="btn btn-secondary btn-sm">View Products</a>
      <a href="${getContextPath()}/food/stock?shopId=${shop.id}" class="btn btn-secondary btn-sm">View Stock</a>
    </div>`;
  openModal('view-modal');
}

function infoRow(label, value) {
  return `
    <div style="display:flex;justify-content:space-between;align-items:center;padding:6px 0;border-bottom:1px solid var(--clr-border)">
      <span style="font-size:0.8rem;color:var(--clr-text-muted)">${label}</span>
      <span style="font-size:0.875rem;font-weight:500">${escapeHtml(String(value || '—'))}</span>
    </div>`;
}

// ── DELETE ────────────────────────────────────────────────
async function deleteShop(id) {
  const shop = allShops.find(s => s.id === id);
  const confirmed = await confirmDialog(`Delete "${shop?.name}"? This will deactivate the shop.`);
  if (!confirmed) return;
  try {
    await api.delete(`/api/shops/${id}`);
    toast('Shop deactivated', 'success');
    await loadShops();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

/**
 * stock.js — Stock management logic
 */

let allStock = [];
let allProducts = [];
let allShops = [];
let currentFilter = 'ALL';
let currentShopFilter = '';
let editingStockId = null;

// ── INIT ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  const params = new URLSearchParams(window.location.search);
  const shopId = params.get('shopId');

  await Promise.all([loadProducts(), loadShops()]);
  await loadStock(shopId);
  setupEventListeners();
  if (shopId) {
    document.getElementById('filter-shop').value = shopId;
    currentShopFilter = shopId;
  }
});

async function loadProducts() {
  try {
    allProducts = await api.get('/api/products');
    const sel = document.getElementById('stock-product');
    allProducts.forEach(p => {
      const opt = document.createElement('option');
      opt.value = p.id;
      opt.textContent = `${p.name} (${p.category || 'N/A'})`;
      sel.appendChild(opt);
    });
  } catch {}
}

async function loadShops() {
  try {
    allShops = await api.get('/api/shops/all');
    const filterSel = document.getElementById('filter-shop');
    const modalSel = document.getElementById('stock-shop');
    allShops.forEach(s => {
      [filterSel, modalSel].forEach(sel => {
        const opt = document.createElement('option');
        opt.value = s.id;
        opt.textContent = s.name + ' (' + s.city + ')';
        sel.appendChild(opt);
      });
    });
  } catch {}
}

async function loadStock(shopId = null) {
  showLoading('stock-table-container');
  try {
    const url = shopId ? `/api/stock/shop/${shopId}` : '/api/stock';
    allStock = await api.get(url);
    await loadLowStockAlert();
    updateStats(allStock);
    renderTable(applyFilter(allStock));
  } catch (err) {
    toast('Failed to load stock: ' + err.message, 'error');
    showEmptyState('stock-table-container', 'Failed to load data', '⚠️');
  }
}

async function loadLowStockAlert() {
  try {
    const lowStock = await api.get('/api/stock/low-stock');
    const alertEl = document.getElementById('low-stock-alert');
    const countEl = document.getElementById('low-stock-count');
    const listEl = document.getElementById('low-stock-list');
    if (lowStock.length > 0) {
      alertEl.style.display = 'block';
      countEl.textContent = lowStock.length;
      listEl.innerHTML = lowStock.slice(0, 5).map(s =>
        `<span style="margin-right:8px;display:inline-block">• ${s.product?.name || '?'}: <strong style="color:var(--clr-danger)">${s.quantity}</strong> left (min: ${s.minStockLevel})</span>`
      ).join('') + (lowStock.length > 5 ? `<span>...and ${lowStock.length - 5} more</span>` : '');
    } else {
      alertEl.style.display = 'none';
    }
  } catch {}
}

function setupEventListeners() {
  document.getElementById('btn-add-stock').addEventListener('click', openAddModal);
  document.getElementById('btn-save-stock').addEventListener('click', saveStock);

  document.getElementById('btn-low-stock').addEventListener('click', () => {
    document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
    document.querySelector('[data-filter="LOW"]').classList.add('active');
    currentFilter = 'LOW';
    renderTable(applyFilter(allStock));
  });

  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentFilter = chip.dataset.filter;
      renderTable(applyFilter(allStock));
    });
  });

  document.getElementById('filter-shop').addEventListener('change', async (e) => {
    currentShopFilter = e.target.value;
    await loadStock(currentShopFilter || null);
  });

  document.getElementById('search-input').addEventListener('input', debounce(() => {
    renderTable(applyFilter(allStock));
  }, 300));
}

// ── FILTER ────────────────────────────────────────────────
function applyFilter(stock) {
  const query = document.getElementById('search-input')?.value.toLowerCase() || '';
  return stock.filter(s => {
    const isLow = s.quantity <= s.minStockLevel;
    const matchFilter = currentFilter === 'ALL'
      || (currentFilter === 'LOW' && isLow && s.quantity > 0)
      || (currentFilter === 'EMPTY' && s.quantity === 0);
    const matchQuery = !query
      || (s.product?.name || '').toLowerCase().includes(query)
      || (s.shop?.name || '').toLowerCase().includes(query);
    return matchFilter && matchQuery;
  });
}

// ── STATS ─────────────────────────────────────────────────
function updateStats(stock) {
  document.getElementById('stat-total').textContent = stock.length;
  const low = stock.filter(s => s.quantity <= s.minStockLevel);
  document.getElementById('stat-low').textContent = low.length;
  document.getElementById('stat-ok').textContent = stock.length - low.length;
  const shops = new Set(stock.map(s => s.shop?.id).filter(Boolean));
  document.getElementById('stat-shops').textContent = shops.size;
}

// ── RENDER TABLE ──────────────────────────────────────────
function renderTable(stock) {
  if (!stock.length) {
    showEmptyState('stock-table-container', 'No stock records found. Add stock entries!', '📦');
    return;
  }
  document.getElementById('stock-table-container').innerHTML = `
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Product</th>
          <th>Shop</th>
          <th>Quantity</th>
          <th>Min Level</th>
          <th>Unit</th>
          <th>Stock Status</th>
          <th>Last Updated</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${stock.map(s => renderRow(s)).join('')}
      </tbody>
    </table>`;
}

function renderRow(s) {
  const pct = Math.min(100, Math.round((s.quantity / (s.minStockLevel * 2 || 1)) * 100));
  const isLow = s.quantity <= s.minStockLevel;
  const isEmpty = s.quantity === 0;
  const statusClass = isEmpty ? 'badge-danger' : isLow ? 'badge-warning' : 'badge-success';
  const statusLabel = isEmpty ? 'Empty' : isLow ? 'Low' : 'OK';
  return `
    <tr>
      <td style="color:var(--clr-text-faint);font-size:0.8rem">#${s.id}</td>
      <td>
        <div style="font-weight:600;font-size:0.875rem">${escapeHtml(s.product?.name || '—')}</div>
        <div style="font-size:0.72rem;color:var(--clr-text-muted)">${escapeHtml(s.product?.category || '')}</div>
      </td>
      <td style="font-size:0.85rem">${escapeHtml(s.shop?.name || 'General')}</td>
      <td>
        <div style="display:flex;flex-direction:column;gap:4px">
          <strong style="font-size:1rem;font-family:'Outfit',sans-serif">${s.quantity}</strong>
          <div class="progress-bar" style="width:80px">
            <div class="progress-bar-fill" style="width:${pct}%;background:${isEmpty ? 'var(--clr-danger)' : isLow ? 'var(--clr-accent)' : 'var(--gradient-brand)'}"></div>
          </div>
        </div>
      </td>
      <td>${s.minStockLevel}</td>
      <td style="color:var(--clr-text-muted)">${escapeHtml(s.unit || '—')}</td>
      <td><span class="badge ${statusClass}">${statusLabel}</span></td>
      <td style="font-size:0.78rem;color:var(--clr-text-muted)">${formatDateTime(s.lastUpdated)}</td>
      <td>
        <div class="table-actions">
          <button class="btn btn-sm btn-secondary" onclick="editStock(${s.id})" title="Edit">✏️</button>
          <button class="btn btn-sm btn-success" onclick="quickAdd(${s.id})" title="Add Qty">+</button>
          <button class="btn btn-sm btn-danger" onclick="deleteStock(${s.id})" title="Delete">🗑</button>
        </div>
      </td>
    </tr>`;
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── ADD ───────────────────────────────────────────────────
function openAddModal() {
  editingStockId = null;
  document.getElementById('modal-title').textContent = 'Add Stock';
  document.getElementById('stock-form').reset();
  document.getElementById('stock-id').value = '';
  document.getElementById('stock-shop').value = currentShopFilter || '';
  openModal('stock-modal');
}

// ── EDIT ──────────────────────────────────────────────────
function editStock(id) {
  const s = allStock.find(x => x.id === id);
  if (!s) return;
  editingStockId = id;
  document.getElementById('modal-title').textContent = 'Edit Stock';
  document.getElementById('stock-id').value = s.id;
  document.getElementById('stock-product').value = s.product?.id || '';
  document.getElementById('stock-shop').value = s.shop?.id || '';
  document.getElementById('stock-quantity').value = s.quantity || 0;
  document.getElementById('stock-min').value = s.minStockLevel || 10;
  document.getElementById('stock-unit').value = s.unit || '';
  document.getElementById('stock-notes').value = s.notes || '';
  openModal('stock-modal');
}

// ── QUICK ADD ─────────────────────────────────────────────
async function quickAdd(id) {
  const qtyStr = await promptDialog('How many units to add?');
  if (!qtyStr || isNaN(parseInt(qtyStr))) return;
  const qty = parseInt(qtyStr);
  try {
    const s = allStock.find(x => x.id === id);
    await api.put(`/api/stock/${id}`, { quantity: (s?.quantity || 0) + qty });
    toast(`Added ${qty} units`, 'success');
    await loadStock(currentShopFilter || null);
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

// ── SAVE ──────────────────────────────────────────────────
async function saveStock() {
  const form = document.getElementById('stock-form');
  if (!form.reportValidity()) return;

  const btn = document.getElementById('btn-save-stock');
  try {
    btn.disabled = true;
    btn.textContent = 'Saving…';

    const shopId = document.getElementById('stock-shop').value || null;

    if (editingStockId) {
      const payload = {
        quantity: parseInt(document.getElementById('stock-quantity').value),
        minStockLevel: parseInt(document.getElementById('stock-min').value) || 10,
        unit: document.getElementById('stock-unit').value.trim() || null,
        notes: document.getElementById('stock-notes').value.trim() || null,
      };
      await api.put(`/api/stock/${editingStockId}`, payload);
      toast('Stock updated!', 'success');
    } else {
      const payload = {
        productId: parseInt(document.getElementById('stock-product').value),
        shopId: shopId ? parseInt(shopId) : null,
        quantity: parseInt(document.getElementById('stock-quantity').value),
        minStockLevel: parseInt(document.getElementById('stock-min').value) || 10,
        unit: document.getElementById('stock-unit').value.trim() || null,
        notes: document.getElementById('stock-notes').value.trim() || null,
      };
      await api.post('/api/stock', payload);
      toast('Stock added!', 'success');
    }
    closeModal('stock-modal');
    await loadStock(currentShopFilter || null);
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Save Stock';
  }
}

// ── DELETE ────────────────────────────────────────────────
async function deleteStock(id) {
  const s = allStock.find(x => x.id === id);
  const ok = await confirmDialog(`Delete stock record for "${s?.product?.name}"?`);
  if (!ok) return;
  try {
    await api.delete(`/api/stock/${id}`);
    toast('Stock record deleted', 'success');
    await loadStock(currentShopFilter || null);
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

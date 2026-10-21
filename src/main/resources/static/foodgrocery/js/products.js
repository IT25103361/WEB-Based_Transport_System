/**
 * products.js — Product CRUD logic
 */

let allProducts = [];
let allVendors = [];
let allShops = [];
let currentFilter = 'ALL';
let currentCategory = '';
let viewMode = 'table';
let editingProductId = null;

// ── INIT ──────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
  // Check shopId from URL
  const params = new URLSearchParams(window.location.search);
  const shopId = params.get('shopId');

  await Promise.all([loadVendors(), loadShopsDropdown()]);
  await loadProducts(shopId);
  setupEventListeners();
});

async function loadVendors() {
  allVendors = [];
}

async function loadShopsDropdown() {
  try {
    allShops = await api.get('/api/shops/all');
    const sel = document.getElementById('product-shop');
    allShops.forEach(s => {
      const opt = document.createElement('option');
      opt.value = s.id;
      opt.textContent = s.name + ' (' + s.city + ')';
      sel.appendChild(opt);
    });
  } catch {}
}

async function loadProducts(shopId = null) {
  showLoading('products-container');
  try {
    const url = shopId ? `/api/products/shop/${shopId}` : '/api/products';
    allProducts = await api.get(url);
    updateStats(allProducts);
    populateCategories(allProducts);
    renderProducts(applyFilter(allProducts));
  } catch (err) {
    toast('Failed to load products: ' + err.message, 'error');
    showEmptyState('products-container', 'Failed to load products', '⚠️');
  }
}

function setupEventListeners() {
  document.getElementById('btn-add-product').addEventListener('click', openAddModal);
  document.getElementById('btn-save-product').addEventListener('click', saveProduct);

  document.querySelectorAll('.filter-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      currentFilter = chip.dataset.filter;
      renderProducts(applyFilter(allProducts));
    });
  });

  document.getElementById('search-input').addEventListener('input', debounce(() => {
    renderProducts(applyFilter(allProducts));
  }, 300));

  document.getElementById('filter-category').addEventListener('change', (e) => {
    currentCategory = e.target.value;
    renderProducts(applyFilter(allProducts));
  });

  document.getElementById('view-toggle').addEventListener('change', (e) => {
    viewMode = e.target.value;
    renderProducts(applyFilter(allProducts));
  });
}

// ── FILTER ────────────────────────────────────────────────
function applyFilter(products) {
  const query = document.getElementById('search-input')?.value.toLowerCase() || '';
  return products.filter(p => {
    const matchFilter = currentFilter === 'ALL'
      || (currentFilter === 'AVAILABLE' && p.available)
      || (currentFilter === 'POPULAR' && p.popular)
      || (currentFilter === 'UNAVAILABLE' && !p.available);
    const matchCategory = !currentCategory || p.category === currentCategory;
    const matchQuery = !query || p.name.toLowerCase().includes(query)
      || (p.category || '').toLowerCase().includes(query)
      || (p.description || '').toLowerCase().includes(query);
    return matchFilter && matchCategory && matchQuery;
  });
}

// ── STATS ─────────────────────────────────────────────────
function updateStats(products) {
  document.getElementById('stat-total').textContent = products.length;
  document.getElementById('stat-available').textContent = products.filter(p => p.available).length;
  document.getElementById('stat-popular').textContent = products.filter(p => p.popular).length;
  document.getElementById('stat-unavailable').textContent = products.filter(p => !p.available).length;
}

function populateCategories(products) {
  const cats = [...new Set(products.map(p => p.category).filter(Boolean))].sort();
  const sel = document.getElementById('filter-category');
  sel.innerHTML = '<option value="">All Categories</option>';
  cats.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c;
    opt.textContent = c;
    sel.appendChild(opt);
  });
}

// ── RENDER ────────────────────────────────────────────────
function renderProducts(products) {
  if (!products.length) {
    showEmptyState('products-container', 'No products found. Add your first product!', '📦');
    return;
  }
  if (viewMode === 'grid') {
    renderGrid(products);
  } else {
    renderTable(products);
  }
}

function renderTable(products) {
  document.getElementById('products-container').innerHTML = `
    <table>
      <thead>
        <tr>
          <th>#</th>
          <th>Product</th>
          <th>Category</th>
          <th>Price</th>
          <th>Stock</th>
          <th>Shop</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${products.map(renderRow).join('')}
      </tbody>
    </table>`;
}

function renderRow(p) {
  const vendorName = p.vendor?.name || allVendors.find(v => v.id === p.vendorId)?.name || '—';
  const shopName = p.shop?.name || allShops.find(s => s.id === p.shopId)?.name || '—';
  const stock = p.stockQuantity ?? 0;
  const stockClass = stock === 0 ? 'badge-danger' : stock < 10 ? 'badge-warning' : 'badge-success';
  return `
    <tr>
      <td style="color:var(--clr-text-faint);font-size:0.8rem">#${p.id}</td>
      <td>
        <div style="display:flex;align-items:center;gap:0.5rem">
          <div style="width:36px;height:36px;background:var(--clr-surface-3);border-radius:8px;
            display:flex;align-items:center;justify-content:center;font-size:1.1rem;flex-shrink:0">
            ${p.imageUrl ? `<img src="${p.imageUrl}" style="width:100%;height:100%;object-fit:cover;border-radius:8px"/>` : '📦'}
          </div>
          <div>
            <div style="font-weight:600;font-size:0.875rem">${escapeHtml(p.name)}</div>
            <div style="font-size:0.72rem;color:var(--clr-text-muted)">${escapeHtml(p.description?.substring(0,40) || '')}${p.description?.length > 40 ? '…' : ''}</div>
          </div>
        </div>
      </td>
      <td><span class="tag">${escapeHtml(p.category || '—')}</span></td>
      <td style="font-weight:600;color:var(--clr-primary)">${formatCurrency(p.price)}</td>
      <td><span class="badge ${stockClass}">${stock}</span></td>
      <td style="font-size:0.8rem;color:var(--clr-text-muted)">${escapeHtml(shopName)}</td>
      <td>
        <div style="display:flex;gap:4px;flex-direction:column">
          <span class="badge ${p.available ? 'badge-success' : 'badge-danger'}">${p.available ? 'Available' : 'Unavailable'}</span>
          ${p.popular ? '<span class="badge badge-warning">⭐ Popular</span>' : ''}
        </div>
      </td>
      <td>
        <div class="table-actions">
          <button class="btn btn-sm btn-info" onclick="viewProduct(${p.id})">👁</button>
          <button class="btn btn-sm btn-secondary" onclick="editProduct(${p.id})">✏️</button>
          <button class="btn btn-sm btn-danger" onclick="deleteProduct(${p.id})">🗑</button>
        </div>
      </td>
    </tr>`;
}

function renderGrid(products) {
  document.getElementById('products-container').innerHTML = `
    <div class="grid-auto" style="padding:var(--sp-md)">
      ${products.map(p => {
        const stock = p.stockQuantity ?? 0;
        return `
        <div class="product-card">
          <div class="product-card-img" style="background:var(--clr-surface-3)">
            ${p.imageUrl ? `<img src="${p.imageUrl}" style="width:100%;height:100%;object-fit:cover"/>` : '📦'}
          </div>
          <div class="product-card-body">
            <div class="product-card-name">${escapeHtml(p.name)}</div>
            <div style="font-size:0.78rem;color:var(--clr-text-muted);margin-bottom:6px">${escapeHtml(p.category || '')}</div>
            <div class="product-card-price">${formatCurrency(p.price)}</div>
          </div>
          <div class="product-card-footer">
            <span class="badge ${p.available ? 'badge-success' : 'badge-danger'}">${p.available ? 'Available' : 'Unavail.'}</span>
            <div class="table-actions">
              <button class="btn btn-sm btn-info" onclick="viewProduct(${p.id})">👁</button>
              <button class="btn btn-sm btn-secondary" onclick="editProduct(${p.id})">✏️</button>
              <button class="btn btn-sm btn-danger" onclick="deleteProduct(${p.id})">🗑</button>
            </div>
          </div>
        </div>`;
      }).join('')}
    </div>`;
}

function escapeHtml(str) {
  return String(str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

// ── ADD MODAL ─────────────────────────────────────────────
function openAddModal() {
  editingProductId = null;
  document.getElementById('modal-title').textContent = 'Add New Product';
  document.getElementById('product-form').reset();
  document.getElementById('product-id').value = '';
  document.getElementById('product-available').checked = true;
  openModal('product-modal');
}

// ── VIEW MODAL ────────────────────────────────────────────
function viewProduct(id) {
  const p = allProducts.find(x => x.id === id);
  if (!p) return;
  
  document.getElementById('view-name').textContent = p.name || 'Unnamed Product';
  document.getElementById('view-category').textContent = p.category || 'No Category';
  document.getElementById('view-price').textContent = formatCurrency(p.price);
  document.getElementById('view-desc').textContent = p.description || 'No description available.';
  document.getElementById('view-stock').textContent = p.stockQuantity ?? 0;
  
  const shopName = p.shop?.name || allShops.find(s => s.id === p.shopId)?.name || '—';
  document.getElementById('view-shop').textContent = shopName;
  
  const statusSpan = document.getElementById('view-status');
  statusSpan.className = 'badge ' + (p.available ? 'badge-success' : 'badge-danger');
  statusSpan.textContent = p.available ? 'Available' : 'Unavailable';
  
  const imgEl = document.getElementById('view-image');
  const placeholderEl = document.getElementById('view-image-placeholder');
  if (p.imageUrl) {
      imgEl.src = p.imageUrl;
      imgEl.style.display = 'block';
      placeholderEl.style.display = 'none';
  } else {
      imgEl.style.display = 'none';
      placeholderEl.style.display = 'flex';
  }
  
  openModal('view-modal');
}

// ── EDIT MODAL ────────────────────────────────────────────
function editProduct(id) {
  const p = allProducts.find(x => x.id === id);
  if (!p) return;
  editingProductId = id;
  document.getElementById('modal-title').textContent = 'Edit Product';
  document.getElementById('product-id').value = p.id;
  document.getElementById('product-name').value = p.name || '';
  document.getElementById('product-category').value = p.category || '';
  document.getElementById('product-desc').value = p.description || '';
  document.getElementById('product-price').value = p.price || '';
  document.getElementById('product-stock').value = p.stockQuantity || 0;
  document.getElementById('product-shop').value = p.shop?.id || p.shopId || '';
  document.getElementById('product-image').value = p.imageUrl || '';
  document.getElementById('product-available').checked = p.available;
  document.getElementById('product-popular').checked = p.popular;
  openModal('product-modal');
}

// ── SAVE ──────────────────────────────────────────────────
async function saveProduct() {
  const form = document.getElementById('product-form');
  if (!form.reportValidity()) return;

  const vendorId = null;
  const shopId = document.getElementById('product-shop').value || null;

  const payload = {
    name: document.getElementById('product-name').value.trim(),
    category: document.getElementById('product-category').value.trim(),
    description: document.getElementById('product-desc').value.trim(),
    price: parseFloat(document.getElementById('product-price').value),
    stockQuantity: parseInt(document.getElementById('product-stock').value) || 0,
    imageUrl: document.getElementById('product-image').value.trim() || null,
    available: document.getElementById('product-available').checked,
    popular: document.getElementById('product-popular').checked,
  };

  const btn = document.getElementById('btn-save-product');
  try {
    btn.disabled = true;
    btn.textContent = 'Saving...';

    const urlParams = shopId ? `?shopId=${shopId}` : '';
    
    if (editingProductId) {
      await api.put(`/api/products/${editingProductId}${urlParams}`, payload);
      toast('Product updated!', 'success');
    } else {
      await api.post(`/api/products${urlParams}`, payload);
      toast('Product added!', 'success');
    }
    closeModal('product-modal');
    await loadProducts();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Save Product';
  }
}

// ── DELETE ────────────────────────────────────────────────
async function deleteProduct(id) {
  const p = allProducts.find(x => x.id === id);
  const ok = await confirmDialog(`Remove "${p?.name}" from available products?`);
  if (!ok) return;
  try {
    await api.delete(`/api/products/${id}`);
    toast('Product removed', 'success');
    await loadProducts();
  } catch (err) {
    toast('Error: ' + err.message, 'error');
  }
}

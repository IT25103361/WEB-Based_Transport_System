/**
 * login.js — Authentication logic
 */

// ── TABS ──────────────────────────────────────────────────
document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById('tab-' + btn.dataset.tab).classList.add('active');
  });
});

// ── LOGIN FORM ────────────────────────────────────────────
document.getElementById('login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  await doLogin(
    document.getElementById('login-email').value.trim(),
    document.getElementById('login-password').value
  );
});

async function doLogin(email, password) {
  const btn = document.getElementById('login-btn');
  const errEl = document.getElementById('login-error');
  btn.disabled = true;
  btn.textContent = 'Signing in…';
  errEl.style.display = 'none';

  try {
    const res = await api.post('/api/auth/login', { email, password });
    if (res.success) {
      setCurrentUser(res.user || { id: res.userId, fullName: res.name, role: res.role, email });
      toast('Welcome back, ' + (res.name || email) + '!', 'success');
      setTimeout(() => {
        const role = res.role || res.user?.role;
        const ctx = (typeof getContextPath === 'function') ? getContextPath() : '';
        if (role === 'ADMIN') window.location.href = ctx + '/food/admin';
        else if (role === 'VENDOR_MANAGER') window.location.href = ctx + '/food/shops';
        else if (role === 'DELIVERY_PARTNER') window.location.href = ctx + '/food/partner';
        else if (role === 'CUSTOMER') window.location.href = ctx + '/food/customer';
        else window.location.href = ctx + '/food/customer';
      }, 500);
    } else {
      errEl.textContent = res.message || 'Login failed';
      errEl.style.display = 'block';
    }
  } catch (err) {
    errEl.textContent = 'Invalid credentials. Please try again.';
    errEl.style.display = 'block';
  } finally {
    btn.disabled = false;
    btn.textContent = '🚀 Sign In';
  }
}



// ── REGISTER FORM ─────────────────────────────────────────
document.getElementById('register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  const btn = document.getElementById('register-btn');
  const errEl = document.getElementById('register-error');
  const okEl = document.getElementById('register-success');
  btn.disabled = true;
  btn.textContent = 'Creating account…';
  errEl.style.display = 'none';
  okEl.style.display = 'none';

    const payload = {
    fullName: document.getElementById('reg-name').value.trim(),
    email: document.getElementById('reg-email').value.trim(),
    password: document.getElementById('reg-password').value,
    phoneNumber: document.getElementById('reg-phone').value.trim(),
    gender: document.getElementById('reg-gender').value,
    role: document.getElementById('reg-role').value,
    address: document.getElementById('reg-address').value.trim(),
    postalCode: document.getElementById('reg-postal-code').value.trim(),
  };

  try {
    const res = await api.post('/api/auth/register', payload);
    if (res.success !== false) {
      okEl.textContent = '✅ Account created! You can now sign in.';
      okEl.style.display = 'block';
      document.getElementById('register-form').reset();
      setTimeout(() => {
        document.querySelector('[data-tab="login"]').click();
      }, 1500);
    } else {
      errEl.textContent = res.message || 'Registration failed';
      errEl.style.display = 'block';
    }
  } catch (err) {
    errEl.textContent = 'Registration failed: ' + err.message;
    errEl.style.display = 'block';
  } finally {
    btn.disabled = false;
    btn.textContent = '✨ Create Account';
  }
});

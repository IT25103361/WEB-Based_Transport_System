/* ============================================================
   Courier — single shared script
   Auth, fetch, helpers, theme toggle. All in one file.
   ============================================================ */

function appPath(path) {
  const meta = document.querySelector('meta[name="ctx"]');
  const ctx = (meta && meta.content) ? meta.content : "";
  if (!path.startsWith("/")) path = "/" + path;
  return ctx + path;
}

/* ---------------- Theme (light/dark) ---------------- */
(function () {
  const KEY = "courier.theme";
  const root = document.documentElement;

  function applyTheme(t) {
    root.setAttribute("data-theme", t);
    document.querySelectorAll("[data-theme-btn]").forEach(b => {
      b.textContent = t === "dark" ? "☀️" : "🌙";
      b.setAttribute("aria-label", t === "dark" ? "Switch to light mode" : "Switch to dark mode");
    });
  }

  // Apply immediately (script runs in <head>, before <body> exists)
  applyTheme(localStorage.getItem(KEY) === "dark" ? "dark" : "light");

  // Update button icons when DOM is ready
  document.addEventListener("DOMContentLoaded", () => applyTheme(root.getAttribute("data-theme") || "light"));

  // Toggle on click
  document.addEventListener("click", (e) => {
    const btn = e.target.closest("[data-theme-btn]");
    if (!btn) return;
    e.preventDefault();
    const next = root.getAttribute("data-theme") === "dark" ? "light" : "dark";
    localStorage.setItem(KEY, next);
    applyTheme(next);
  });
})();

/* ---------------- Auth ---------------- */
const Auth = {
  KEY: "courier_auth",
  save(d) { localStorage.setItem(this.KEY, JSON.stringify(d)); },
  get() { try { return JSON.parse(localStorage.getItem(this.KEY)); } catch { return null; } },
  token() { const a = this.get(); return a ? a.token : null; },
  role() { const a = this.get(); return a ? a.role : null; },
  clear() { localStorage.removeItem(this.KEY); },
  logout() { this.clear(); window.location.href = appPath("/login?logout=1"); },
  requireRole(role) {
    const a = this.get();
    if (!a || !a.token) { window.location.href = appPath("/login"); return null; }
    if (role && a.role !== role) {
      window.location.href = appPath(a.role === "PARTNER" ? "/partner" : "/sender");
      return null;
    }
    return a;
  }
};

/* ---------------- Fetch wrapper ---------------- */
async function api(path, opts = {}) {
  const token = Auth.token();
  const headers = Object.assign({ "Content-Type": "application/json" }, opts.headers || {});
  if (token) headers["Authorization"] = "Bearer " + token;

  const res = await fetch(path.startsWith("http") ? path : appPath(path), Object.assign({}, opts, { headers }));
  const text = await res.text();
  let body = null;
  if (text) { try { body = JSON.parse(text); } catch { body = text; } }

  if (!res.ok) {
    const msg = (body && (body.error || body.message)) || (res.status + " " + res.statusText);
    throw new Error(msg);
  }
  return body;
}

/* ---------------- Toast ---------------- */
function toast(message, type = "ok") {
  let wrap = document.querySelector(".toast-wrap");
  if (!wrap) {
    wrap = document.createElement("div");
    wrap.className = "toast-wrap";
    document.body.appendChild(wrap);
  }
  const el = document.createElement("div");
  el.className = "toast" + (type ? " " + type : "");
  el.textContent = message;
  wrap.appendChild(el);
  setTimeout(() => el.remove(), 3200);
}

/* ---------------- Formatters ---------------- */
function fmtMoney(v) {
  if (v === null || v === undefined) return "—";
  return "Rs. " + Number(v).toLocaleString("en-LK", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
function fmtDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  if (isNaN(d)) return "—";
  return d.toLocaleString(undefined, { month: "short", day: "numeric", hour: "2-digit", minute: "2-digit" });
}
function statusLabel(s) {
  const m = {
    REQUESTED: "Requested", MATCHED: "Matched", ACCEPTED: "Accepted",
    PICKED_UP: "Picked up", IN_TRANSIT: "In transit", ARRIVED: "Arrived",
    DELIVERED: "Delivered", CANCELLED: "Cancelled", FAILED: "Failed", RETURNED: "Returned"
  };
  return m[s] || s || "";
}
function pillClass(s) { return "pill pill-" + String(s || "").toLowerCase(); }
function escapeHtml(s) {
  const d = document.createElement("div");
  d.textContent = s == null ? "" : String(s);
  return d.innerHTML;
}

/* ---------------- Geo ---------------- */
function haversineKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2)**2 +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2)**2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}
function estimatePrice(tier, pickup, drop, weightKg) {
  if (tier === "INSTANT") {
    if (!pickup || !drop) return null;
    const km = haversineKm(pickup.lat, pickup.lng, drop.lat, drop.lng);
    return 300 + 60 * km;
  }
  const w = weightKg && weightKg > 0 ? weightKg : 1;
  return 200 + 50 * w;
}

/* ---------------- Status constants ---------------- */
const STATUS_FLOW = ["REQUESTED", "MATCHED", "PICKED_UP", "IN_TRANSIT", "ARRIVED", "DELIVERED"];
const TERMINAL_STATUSES = ["DELIVERED", "CANCELLED", "FAILED", "RETURNED"];
const SENDER_CANCELLABLE = ["REQUESTED", "MATCHED", "ACCEPTED"];

const SENDER_CANCEL_REASONS = [
  { value: "CHANGED_MIND",     label: "Changed my mind" },
  { value: "WRONG_ADDRESS",    label: "Wrong address entered" },
  { value: "NO_LONGER_NEEDED", label: "No longer needed" },
  { value: "OTHER",            label: "Other" }
];

/* ---------------- Cancel modal ---------------- */
function askCancelReason(title, reasons) {
  return new Promise((resolve) => {
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";
    overlay.innerHTML = `
      <div class="modal">
        <h3>${escapeHtml(title)}</h3>
        <div class="reason-list">
          ${reasons.map(r => `
            <label class="reason-opt">
              <input type="radio" name="cancelReason" value="${r.value}" />
              <span>${escapeHtml(r.label)}</span>
            </label>`).join("")}
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline" data-act="back" type="button">Back</button>
          <button class="btn btn-danger"  data-act="ok"   type="button">Confirm cancel</button>
        </div>
      </div>`;
    document.body.appendChild(overlay);
    overlay.addEventListener("click", (e) => {
      const act = e.target.closest("[data-act]")?.dataset.act;
      if (e.target === overlay || act === "back") { overlay.remove(); resolve(null); return; }
      if (act === "ok") {
        const picked = overlay.querySelector('input[name="cancelReason"]:checked');
        if (!picked) { toast("Pick a reason", "error"); return; }
        overlay.remove();
        resolve({ reason: picked.value });
      }
    });
  });
}
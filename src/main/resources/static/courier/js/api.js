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
    if (window.NextGoTheme) {
        // Theme is centrally managed by nextgo-theme.js
        return;
    }

    const SHARED_KEY = "nextgo.theme";
    const LEGACY_KEY = "courier.theme";
    const root = document.documentElement;

    function safeGet(k) {
        try { return localStorage.getItem(k); } catch (e) { return null; }
    }

    function safeSet(k, v) {
        try { localStorage.setItem(k, v); } catch (e) {}
    }

    function applyTheme(t) {
        root.setAttribute("data-theme", t);
        root.setAttribute("data-bs-theme", t);
        document.querySelectorAll("[data-theme-btn]").forEach(b => {
            b.textContent = t === "dark" ? "☀️" : "🌙";
            b.setAttribute("aria-label", t === "dark" ? "Switch to light mode" : "Switch to dark mode");
            b.setAttribute("title", t === "dark" ? "Switch to light mode" : "Switch to dark mode");
        });
    }

    // Apply immediately
    const initial = safeGet(SHARED_KEY) || safeGet(LEGACY_KEY) || "light";
    applyTheme(initial === "dark" ? "dark" : "light");

    // Update button icons when DOM is ready
    document.addEventListener("DOMContentLoaded", () => {
        applyTheme(root.getAttribute("data-theme") || "light");
    });

    // Toggle on click
    document.addEventListener("click", (e) => {
        const btn = e.target.closest("[data-theme-btn]");
        if (!btn) return;
        e.preventDefault();
        const next = root.getAttribute("data-theme") === "dark" ? "light" : "dark";
        safeSet(SHARED_KEY, next);
        safeSet(LEGACY_KEY, next);
        applyTheme(next);
    });
})();

/* ---------------- CSRF Cookie Helper ---------------- */
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return decodeURIComponent(parts.pop().split(';').shift());
    return null;
}

/* ---------------- Auth ---------------- */
const Auth = {
    KEY: "courier_auth",
    // Clear any old legacy token so it never overrides the shared session
    clearLegacy() {
        try { localStorage.removeItem(this.KEY); } catch (e) {}
    },
    save(d) {
        this.clearLegacy();
    },
    get() {
        this.clearLegacy();
        const metaName = document.querySelector('meta[name="user-name"]');
        const metaRole = document.querySelector('meta[name="user-role"]');
        const metaEmail = document.querySelector('meta[name="user-email"]');
        if (metaName && metaRole) {
            return {
                name: metaName.content,
                role: metaRole.content,
                email: metaEmail ? metaEmail.content : ""
            };
        }
        return null;
    },
    token() { return null; },
    role() { const a = this.get(); return a ? a.role : null; },
    clear() { this.clearLegacy(); },
    logout() {
        this.clearLegacy();
        window.location.href = appPath("/courier/logout");
    },
    requireRole(role) {
        this.clearLegacy();
        const a = this.get();
        if (!a) {
            window.location.href = appPath("/login?redirect=" + encodeURIComponent(window.location.pathname));
            return null;
        }
        if (role && a.role !== role) {
            if (role === "PARTNER" && a.role !== "PARTNER") {
                window.location.href = appPath("/courier/register-partner");
                return null;
            }
            window.location.href = appPath(a.role === "PARTNER" ? "/courier/partner" : "/courier/sender");
            return null;
        }
        return a;
    }
};

/* ---------------- Fetch wrapper ---------------- */
async function api(path, opts = {}) {
    const headers = Object.assign({ "Content-Type": "application/json" }, opts.headers || {});

    // Attach CSRF token for mutating requests (POST, PUT, DELETE, PATCH)
    const method = (opts.method || "GET").toUpperCase();
    if (!["GET", "HEAD", "OPTIONS", "TRACE"].includes(method)) {
        const csrfToken = getCookie("XSRF-TOKEN");
        if (csrfToken) {
            headers["X-XSRF-TOKEN"] = csrfToken;
        }
    }

    const fullPath = path.startsWith("http") ? path : appPath(path);
    const res = await fetch(fullPath, Object.assign({}, opts, { headers, credentials: "same-origin" }));

    if (res.status === 401) {
        if (path.includes("/track/")) {
            throw new Error("Session expired. Please sign in to view tracking details.");
        }
        window.location.href = appPath("/login?redirect=" + encodeURIComponent(window.location.pathname));
        return null;
    }

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

/* ---------------- Track parcel modal ---------------- */
(function initTrackModal() {
    function getModal() {
        return document.getElementById("trackModal");
    }

    function openModal() {
        const overlay = getModal();
        if (!overlay) return;
        const input = overlay.querySelector("#trackCodeInput");
        const err = overlay.querySelector("#trackModalError");
        const submitBtn = overlay.querySelector("#submitTrackModalBtn");
        if (err) { err.textContent = ""; err.style.display = "none"; }
        if (submitBtn) { submitBtn.disabled = false; submitBtn.textContent = "Track parcel"; }
        if (input) { input.value = ""; }
        overlay.style.display = "flex";
        if (input) setTimeout(() => input.focus(), 50);
    }

    function closeModal() {
        const overlay = getModal();
        if (!overlay) return;
        overlay.style.display = "none";
        const err = overlay.querySelector("#trackModalError");
        if (err) { err.textContent = ""; err.style.display = "none"; }
    }

    function submitModal() {
        const overlay = getModal();
        if (!overlay) return;
        const input = overlay.querySelector("#trackCodeInput");
        const err = overlay.querySelector("#trackModalError");
        const submitBtn = overlay.querySelector("#submitTrackModalBtn");

        const raw = input ? input.value : "";
        const trimmed = (raw || "").trim();

        if (!trimmed) {
            if (err) {
                err.textContent = "Please enter a tracking ID.";
                err.style.display = "block";
            }
            if (input) input.focus();
            return;
        }

        const codeRegex = /^CR-[A-Za-z0-9]{4,12}$/i;
        if (!codeRegex.test(trimmed)) {
            if (err) {
                err.textContent = "Invalid tracking ID format. Expected format: CR-XXXXXX (e.g. CR-9A4B2C).";
                err.style.display = "block";
            }
            if (input) input.focus();
            return;
        }

        if (err) err.style.display = "none";
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<span class="spinner"></span> Loading...`;
        }

        const formatted = trimmed.toUpperCase();
        window.location.href = appPath("/courier/track?code=" + encodeURIComponent(formatted));
    }

    document.addEventListener("click", (e) => {
        if (e.target.closest("#navTrackBtn")) {
            e.preventDefault();
            openModal();
            return;
        }
        if (e.target.closest("#closeTrackModalBtn") || e.target.closest("#cancelTrackModalBtn")) {
            e.preventDefault();
            closeModal();
            return;
        }
        const overlay = getModal();
        if (overlay && e.target === overlay) {
            closeModal();
            return;
        }
        if (e.target.closest("#submitTrackModalBtn")) {
            e.preventDefault();
            submitModal();
            return;
        }
    });

    document.addEventListener("keydown", (e) => {
        const overlay = getModal();
        if (!overlay || overlay.style.display === "none") return;

        if (e.key === "Escape") {
            closeModal();
        } else if (e.key === "Enter" && e.target && e.target.id === "trackCodeInput") {
            e.preventDefault();
            submitModal();
        }
    });
})();
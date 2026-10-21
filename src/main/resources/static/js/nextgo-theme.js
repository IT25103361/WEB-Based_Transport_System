/**
 * NEXT GO — Unified Global Theme Controller
 * Manages shared Light / Dark mode across all application modules:
 * Home, Ride, Courier, School, Vehicle Rental, Admin, and Emergency.
 *
 * Primary storage key: "nextgo.theme" ('light' | 'dark')
 * Legacy storage keys synchronized: "nextgo_theme", "courier.theme", "nextgo.emergency.theme"
 */
(function (window, document) {
  "use strict";

  const SHARED_KEY = "nextgo.theme";
  const LEGACY_KEYS = ["nextgo_theme", "courier.theme", "nextgo.emergency.theme"];
  const VALID_THEMES = ["light", "dark"];

  function safeGet(key) {
    try {
      return localStorage.getItem(key);
    } catch (e) {
      return null;
    }
  }

  function safeSet(key, val) {
    try {
      localStorage.setItem(key, val);
    } catch (e) {}
  }

  /**
   * Resolves the active theme preference.
   * If the shared key is absent, migrates valid existing preference from legacy keys;
   * otherwise defaults to 'light'.
   */
  function getTheme() {
    let current = safeGet(SHARED_KEY);
    if (VALID_THEMES.includes(current)) {
      return current;
    }

    // Migrate from legacy keys if present
    for (let i = 0; i < LEGACY_KEYS.length; i++) {
      const legacy = safeGet(LEGACY_KEYS[i]);
      if (VALID_THEMES.includes(legacy)) {
        safeSet(SHARED_KEY, legacy);
        return legacy;
      }
    }

    // Default to 'light' per specification
    const defaultTheme = "light";
    safeSet(SHARED_KEY, defaultTheme);
    return defaultTheme;
  }

  /**
   * Applies the theme to the document and updates all interactive toggle buttons.
   */
  function applyTheme(theme) {
    const t = theme === "dark" ? "dark" : "light";
    const isDark = t === "dark";
    const root = document.documentElement;

    root.setAttribute("data-theme", t);
    root.setAttribute("data-bs-theme", t);
    if (document.body) {
      document.body.setAttribute("data-theme", t);
    }

    // Update all theme toggle buttons across the DOM
    const buttons = document.querySelectorAll(
      "[data-nextgo-theme-toggle], [data-theme-toggle], [data-theme-btn], [data-emergency-theme-toggle], .theme-toggle-btn"
    );

    buttons.forEach(btn => {
      // Ensure button never submits forms
      if (btn.tagName === "BUTTON" && btn.getAttribute("type") !== "button") {
        btn.setAttribute("type", "button");
      }

      btn.setAttribute("aria-label", isDark ? "Switch to light mode" : "Switch to dark mode");
      btn.setAttribute("title", isDark ? "Switch to light mode" : "Switch to dark mode");
      btn.setAttribute("aria-pressed", isDark ? "true" : "false");

      // School Transport button structure
      const iconSpan = btn.querySelector(".theme-toggle-icon");
      const labelSpan = btn.querySelector(".theme-toggle-label");
      if (iconSpan || labelSpan) {
        if (iconSpan) {
          iconSpan.innerHTML = isDark
            ? '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"></circle><line x1="12" y1="1" x2="12" y2="3"></line><line x1="12" y1="21" x2="12" y2="23"></line><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line><line x1="1" y1="12" x2="3" y2="12"></line><line x1="21" y1="12" x2="23" y2="12"></line><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line></svg>'
            : '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path></svg>';
        }
        if (labelSpan) {
          labelSpan.textContent = isDark ? "Light" : "Dark";
        }
      } else {
        // Standard emoji buttons (Courier, Emergency, Home, Rental, Admin, Ride)
        btn.textContent = isDark ? "☀️" : "🌙";
      }
    });

    // Update Chart.js if present (Admin Dashboard)
    updateCharts(isDark);

    // Notify custom listeners
    window.dispatchEvent(new CustomEvent("nextgo:themechange", { detail: { theme: t, isDark: isDark } }));
  }

  function setTheme(theme) {
    const t = theme === "dark" ? "dark" : "light";
    safeSet(SHARED_KEY, t);
    // Keep legacy module keys in sync
    LEGACY_KEYS.forEach(k => safeSet(k, t));
    applyTheme(t);
  }

  function toggleTheme() {
    const current = getTheme();
    const next = current === "dark" ? "light" : "dark";
    setTheme(next);
  }

  function updateCharts(isDark) {
    const textColor = isDark ? "#94a3b8" : "#64748b";
    const gridColor = isDark ? "rgba(255, 255, 255, 0.08)" : "#f1f5f9";

    if (window.trendChartInstance) {
      try {
        const chart = window.trendChartInstance;
        if (chart.options && chart.options.scales) {
          if (chart.options.scales.x && chart.options.scales.x.ticks) chart.options.scales.x.ticks.color = textColor;
          if (chart.options.scales.y) {
            if (chart.options.scales.y.ticks) chart.options.scales.y.ticks.color = textColor;
            if (chart.options.scales.y.grid) chart.options.scales.y.grid.color = gridColor;
          }
        }
        if (chart.options && chart.options.plugins && chart.options.plugins.legend && chart.options.plugins.legend.labels) {
          chart.options.plugins.legend.labels.color = textColor;
        }
        chart.update();
      } catch (e) {}
    }

    if (window.statusChartInstance) {
      try {
        const chart = window.statusChartInstance;
        if (chart.options && chart.options.plugins && chart.options.plugins.legend && chart.options.plugins.legend.labels) {
          chart.options.plugins.legend.labels.color = textColor;
        }
        chart.update();
      } catch (e) {}
    }
  }

  // Early execution in head (before DOM is ready) to eliminate theme flash
  const initialTheme = getTheme();
  applyTheme(initialTheme);

  // Re-apply when DOM is ready to wire buttons and charts
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", function () {
      applyTheme(getTheme());
    });
  } else {
    applyTheme(getTheme());
  }

  // Consolidated global click delegation for all theme buttons
  document.addEventListener("click", function (e) {
    const btn = e.target.closest(
      "[data-nextgo-theme-toggle], [data-theme-toggle], [data-theme-btn], [data-emergency-theme-toggle], .theme-toggle-btn"
    );
    if (!btn) return;
    // Prevent double toggle if button has inline onclick (e.g., School transport toggleAppTheme)
    if (btn.hasAttribute("onclick") && btn.getAttribute("onclick").indexOf("toggleAppTheme") !== -1) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    toggleTheme();
  });

  // Cross-tab synchronization via storage event
  window.addEventListener("storage", function (e) {
    if (e.key === SHARED_KEY || LEGACY_KEYS.includes(e.key)) {
      if (e.newValue && VALID_THEMES.includes(e.newValue)) {
        applyTheme(e.newValue);
      }
    }
  });

  // Public API
  window.NextGoTheme = {
    getTheme: getTheme,
    setTheme: setTheme,
    toggleTheme: toggleTheme,
    applyTheme: applyTheme
  };

})(window, document);

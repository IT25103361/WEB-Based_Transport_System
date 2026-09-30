(async () => {
  pageShell("Notifications", "Important updates from your child's school journey.", skeletons(3));
  const main = document.querySelector(".app-main");
  syncTopbarChildSelector();

  try {
    const studentId = selectedStudent();
    let notices = await fetchCombinedNotifications(true);
    let arrival = null;
    if (studentId) {
      try { arrival = await Api.getArrivalStatus(studentId); } catch {}
    }

    const PAGE_SIZE = 20;
    let currentPage = 1;

    function renderNotificationsShell() {
      const totalCount = notices.length;
      main.innerHTML = `
        <header class="topbar">
          ${renderTopbarLeft("Notifications", "Important updates from your child's school journey.")}
          ${renderTopControls(null, studentId)}
        </header>
        <section class="notifications-shell">
          ${arrival?.confirmed || arrival?.safeArrival || arrival?.arrived ? `
            <article class="safe-arrival glass">
              <div class="safe-check">${SVG_ICONS.check}</div>
              <div>
                <h2>SAFE ARRIVAL CONFIRMED</h2>
                <p>${escapeHtml(field(arrival, "studentName", "name", "Your child"))} has arrived at ${escapeHtml(field(arrival, "schoolName", "school"))}. ${escapeHtml(field(arrival, "arrivalTime", "time", ""))}</p>
              </div>
            </article>
          ` : ""}
          <div class="notification-tools">
            <div>
              <span class="eyebrow">ASSIGNED ACTIVITY</span>
              <p class="muted" id="notifActivityCount" style="margin:2px 0 0">${totalCount} transport update${totalCount === 1 ? "" : "s"}</p>
            </div>
            <div class="notification-tools-actions">
              <button class="button secondary small" id="markAll" ${totalCount ? "" : "disabled"}>Mark All as Read</button>
              <button class="button danger small" id="deleteAllNotifs" ${totalCount ? "" : "disabled"}>Delete All</button>
            </div>
          </div>
          <div class="notification-list" id="notificationsContainer"></div>
          <div id="notifPaginationContainer"></div>
        </section>
      `;

      syncTopbarChildSelector();
      bindHeaderActions();
      renderPageItems();
    }

    function renderPageItems() {
      const container = document.getElementById("notificationsContainer");
      const pagContainer = document.getElementById("notifPaginationContainer");
      const countEl = document.getElementById("notifActivityCount");
      const markAllBtn = document.getElementById("markAll");
      const deleteBtn = document.getElementById("deleteAllNotifs");

      if (countEl) {
        countEl.textContent = `${notices.length} transport update${notices.length === 1 ? "" : "s"}`;
      }
      if (markAllBtn) markAllBtn.disabled = !notices.length;
      if (deleteBtn) deleteBtn.disabled = !notices.length;

      if (!container) return;

      if (!notices.length) {
        container.innerHTML = '<section class="glass empty-state">You’re all caught up.<br>No transport notifications.</section>';
        if (pagContainer) pagContainer.innerHTML = "";
        return;
      }

      const totalPages = Math.ceil(notices.length / PAGE_SIZE) || 1;
      if (currentPage > totalPages) currentPage = totalPages;
      if (currentPage < 1) currentPage = 1;

      const startIndex = (currentPage - 1) * PAGE_SIZE;
      const endIndex = Math.min(startIndex + PAGE_SIZE, notices.length);
      const pageSlice = notices.slice(startIndex, endIndex);

      container.innerHTML = pageSlice.map(noticeMarkup).join("");

      // Bind individual mark as read buttons
      container.querySelectorAll("[data-notice]").forEach(b => {
        b.addEventListener("click", async () => {
          const nid = b.dataset.notice;
          b.disabled = true;
          try {
            if (nid && !String(nid).startsWith("loc_") && !isNaN(Number(nid))) {
              await Api.markNotificationRead(nid);
            }
          } catch {}

          // Update in-memory notice
          const itm = notices.find(n => String(n.id) === String(nid));
          if (itm) {
            itm.read = true;
            itm.isRead = true;
            itm.readStatus = true;
          }

          // Update local storage
          const list = getLocalNotifications();
          const loc = list.find(n => String(n.id) === String(nid));
          if (loc) {
            loc.read = true;
            loc.isRead = true;
            loc.readStatus = true;
            saveLocalNotifications(list);
          }

          const card = b.closest(".notice");
          if (card) {
            card.classList.remove("unread");
            card.querySelector(".unread-dot")?.remove();
          }
          b.remove();

          refreshNotificationsUI();
        });
      });

      // Bind cancel/delete notification buttons
      container.querySelectorAll("[data-cancel-notice]").forEach(b => {
        b.addEventListener("click", async (e) => {
          e.stopPropagation();
          const nid = b.dataset.cancelNotice;
          b.disabled = true;
          try {
            if (nid && !String(nid).startsWith("loc_") && !isNaN(Number(nid))) {
              await Api.deleteNotification(nid);
            }
          } catch {}

          // Remove in local storage
          const list = getLocalNotifications();
          const filtered = list.filter(n => String(n.id) !== String(nid));
          saveLocalNotifications(filtered);

          // Remove in memory & re-render
          notices = notices.filter(n => String(n.id) !== String(nid));
          renderPageItems();
          refreshNotificationsUI();
        });
      });

      // Render Pagination Bar if more than 1 page
      if (pagContainer) {
        if (totalPages > 1) {
          pagContainer.innerHTML = `
            <div class="notif-pagination-bar">
              <span class="pagination-info">Showing ${startIndex + 1}–${endIndex} of ${notices.length} notifications</span>
              <div class="pagination-buttons">
                <button class="pagination-btn" id="prevPageBtn" ${currentPage === 1 ? "disabled" : ""}>&larr; Prev</button>
                ${generatePageButtons(currentPage, totalPages)}
                <button class="pagination-btn" id="nextPageBtn" ${currentPage === totalPages ? "disabled" : ""}>Next &rarr;</button>
              </div>
            </div>
          `;

          // Pagination events
          document.getElementById("prevPageBtn")?.addEventListener("click", () => {
            if (currentPage > 1) {
              currentPage--;
              renderPageItems();
              scrollToShellTop();
            }
          });

          document.getElementById("nextPageBtn")?.addEventListener("click", () => {
            if (currentPage < totalPages) {
              currentPage++;
              renderPageItems();
              scrollToShellTop();
            }
          });

          pagContainer.querySelectorAll("[data-page-num]").forEach(btn => {
            btn.addEventListener("click", () => {
              const targetPage = Number(btn.dataset.pageNum);
              if (targetPage && targetPage !== currentPage) {
                currentPage = targetPage;
                renderPageItems();
                scrollToShellTop();
              }
            });
          });
        } else {
          pagContainer.innerHTML = "";
        }
      }
    }

    function generatePageButtons(current, total) {
      let buttonsHtml = "";
      const maxVisible = 5;
      let start = Math.max(1, current - 2);
      let end = Math.min(total, start + maxVisible - 1);
      if (end - start < maxVisible - 1) {
        start = Math.max(1, end - maxVisible + 1);
      }

      for (let p = start; p <= end; p++) {
        buttonsHtml += `<button class="pagination-btn ${p === current ? 'active' : ''}" data-page-num="${p}">${p}</button>`;
      }
      return buttonsHtml;
    }

    function scrollToShellTop() {
      const shell = document.querySelector(".notifications-shell");
      if (shell) {
        shell.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    }

    function bindHeaderActions() {
      // Mark All as Read button
      const markAllBtn = document.getElementById("markAll");
      markAllBtn?.addEventListener("click", async e => {
        const btn = e.currentTarget;
        btn.disabled = true;
        const originalText = btn.textContent;
        btn.textContent = "Updating...";
        try {
          if (typeof Api !== "undefined" && Api.markAllNotificationsRead) {
            await Api.markAllNotificationsRead();
          }
          // Mark all notices in memory
          notices.forEach(n => {
            n.read = true;
            n.isRead = true;
            n.readStatus = true;
          });

          // Mark local notifications
          const list = getLocalNotifications();
          list.forEach(n => {
            n.read = true;
            n.isRead = true;
            n.readStatus = true;
          });
          saveLocalNotifications(list);

          renderPageItems();
          refreshNotificationsUI();
        } catch (err) {
          console.warn("Mark all read failed:", err.message);
        } finally {
          btn.disabled = false;
          btn.textContent = originalText;
        }
      });

      // Delete All Notifications button
      const deleteAllBtn = document.getElementById("deleteAllNotifs");
      deleteAllBtn?.addEventListener("click", async e => {
        if (!notices.length) return;
        const confirmed = confirm("Are you sure you want to delete all notifications? This cannot be undone.");
        if (!confirmed) return;

        const btn = e.currentTarget;
        btn.disabled = true;
        const origText = btn.textContent;
        btn.textContent = "Deleting...";

        try {
          // Delete in backend
          if (typeof Api !== "undefined" && Api.deleteAllNotifications) {
            await Api.deleteAllNotifications();
          }
        } catch (err) {
          console.warn("Backend deleteAll error:", err.message);
        }

        // Clear local storage
        saveLocalNotifications([]);

        // Clear in-memory array
        notices = [];
        currentPage = 1;

        renderPageItems();
        refreshNotificationsUI();
      });
    }

    window.refreshNotificationsPage = () => {
      notices.forEach(n => { n.read = true; n.isRead = true; n.readStatus = true; });
      renderPageItems();
    };

    renderNotificationsShell();

  } catch (err) {
    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("Notifications", "School Student Transport")}
        ${renderTopControls()}
      </header>
      ${errorView(err.message)}
    `;
  }
})();

function noticeMarkup(n) {
  const type = String(field(n, "type", "category", "info")).toLowerCase();
  const read = Boolean(n.read === true || n.isRead === true || n.readStatus === true);
  const icon = type.includes("arrival") ? SVG_ICONS.check : type.includes("delay") ? SVG_ICONS.schedule : type.includes("gps") ? SVG_ICONS.tracking : SVG_ICONS.bell;
  const cl = type.includes("arrival") ? "safe" : type.includes("delay") ? "delay" : type.includes("gps") ? "gps" : "";

  return `
    <article class="notice glass ${cl} ${read ? "" : "unread"}">
      <div class="notice-icon">${icon}</div>
      <div class="notice-body">
        <h3>${escapeHtml(field(n, "title", "subject", "Transport update"))}</h3>
        <p>${escapeHtml(field(n, "message", "body", ""))}</p>
      </div>
      <div class="notice-meta-actions">
        <time>${formatFullNoticeTime(field(n, "createdAt", "timestamp", "time", "date"))}</time>
        <div class="notice-actions-group">
          ${read ? "" : `<button class="button secondary" data-notice="${escapeHtml(String(field(n, "id", "notificationId", "")))}">Mark as Read</button>`}
          <button class="notice-cancel-btn" data-cancel-notice="${escapeHtml(String(field(n, "id", "notificationId", "")))}" title="Cancel / Delete notification" aria-label="Cancel notification">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
          </button>
        </div>
      </div>
      ${read ? "" : '<i class="unread-dot"></i>'}
    </article>
  `;
}

function formatFullNoticeTime(ts) {
  if (!ts) return "Just now";
  try {
    const d = new Date(ts);
    if (isNaN(d.getTime())) return String(ts);
    return d.toLocaleDateString([], { month: "short", day: "numeric" }) + " " + d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  } catch {
    return "Recently";
  }
}

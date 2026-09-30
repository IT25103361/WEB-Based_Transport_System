(async () => {
  pageShell("Parent profile", "Your contact information and account settings.", skeletons(1));
  const main = document.querySelector(".app-main");
  syncTopbarChildSelector();
  try {
    const p = await Api.getParent(), name = personName(p);
    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("Parent profile", "Your contact information and account settings.")}
        ${renderTopControls()}
      </header>
      <section class="profile-card glass">
        <div class="profile-head">
          <div class="profile-avatar">${escapeHtml(name.slice(0, 1))}</div>
          <div>
            <h2>${escapeHtml(name)}</h2>
            <p>${escapeHtml(field(p, "email", "emailAddress"))}</p>
          </div>
        </div>
        <div class="profile-row">
          <span>Email address</span>
          <b>${escapeHtml(field(p, "email", "emailAddress"))}</b>
        </div>
        <div class="profile-row">
          <span>Phone number</span>
          <b>${escapeHtml(field(p, "phoneNumber", "phone", "mobile"))}</b>
        </div>
        <div class="profile-actions">
          <button class="button secondary" disabled title="No profile update endpoint was included in the supplied API">Edit Profile</button>
          <button class="button secondary" disabled title="No password-change endpoint was included in the supplied API">Change Password</button>
          <button class="button danger" onclick="Api.logout()">Logout</button>
        </div>
        <p class="muted" style="margin:16px 0 0">Profile editing and password changes will activate when their existing backend endpoints are available.</p>
      </section>
    `;
    syncTopbarChildSelector();
  } catch (err) {
    main.innerHTML = `
      <header class="topbar">
        ${renderTopbarLeft("Parent profile", "School Student Transport")}
        ${renderTopControls()}
      </header>
      ${errorView(err.message)}
    `;
  }
})();

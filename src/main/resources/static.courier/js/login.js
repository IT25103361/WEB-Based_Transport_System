(function () {
  const a = Auth.get();
  if (a && a.token) {
    window.location.href = appPath(a.role === "PARTNER" ? "/partner" : "/sender");
    return;
  }

  const msgBox = document.getElementById("msgBox");
  const params = new URLSearchParams(window.location.search);
  if (params.get("logout")) {
    msgBox.innerHTML = `<div class="alert alert-ok">You've been logged out.</div>`;
  }

  const form = document.getElementById("loginForm");
  const btn = document.getElementById("submitBtn");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    msgBox.innerHTML = "";
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Logging in…';

    try {
      const body = await api("/api/courier/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email: document.getElementById("email").value.trim(),
          password: document.getElementById("password").value
        })
      });
      Auth.save(body);
      window.location.href = appPath(body.role === "PARTNER" ? "/partner" : "/sender");
    } catch (err) {
      msgBox.innerHTML = `<div class="alert alert-error">${err.message || "Invalid email or password"}</div>`;
      btn.disabled = false;
      btn.textContent = "Log in";
    }
  });
})();

(function () {
  const a = Auth.get();
  if (a && a.token) {
    window.location.href = appPath(a.role === "PARTNER" ? "/partner" : "/sender");
    return;
  }

  const form = document.getElementById("regForm");
  const msgBox = document.getElementById("msgBox");
  const btn = document.getElementById("submitBtn");

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    msgBox.innerHTML = "";
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Creating account…';

    try {
      const body = await api("/api/courier/auth/register", {
        method: "POST",
        body: JSON.stringify({
          name: document.getElementById("name").value.trim(),
          email: document.getElementById("email").value.trim(),
          phone: document.getElementById("phone").value.trim(),
          password: document.getElementById("password").value,
          role: "SENDER"
        })
      });
      Auth.save(body);
      window.location.href = appPath("/sender");
    } catch (err) {
      msgBox.innerHTML = `<div class="message error">${err.message || "Could not create account"}</div>`;
      btn.disabled = false;
      btn.textContent = "Create sender account";
    }
  });
})();

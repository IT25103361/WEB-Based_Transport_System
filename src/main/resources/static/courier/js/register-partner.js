(function () {
  const form = document.getElementById("regForm");
  const msgBox = document.getElementById("msgBox");
  const btn = document.getElementById("submitBtn");

  if (!form) return;

  form.addEventListener("submit", async (e) => {
    e.preventDefault();
    msgBox.innerHTML = "";
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Updating partner profile…';

    try {
      const vehicleType = document.getElementById("vehicleType").value;
      const phoneInput = document.getElementById("phone");
      const phone = phoneInput ? phoneInput.value.trim() : null;

      await api("/api/courier/partner/register", {
        method: "POST",
        body: JSON.stringify({
          vehicleType: vehicleType,
          phone: phone
        })
      });
      window.location.href = appPath("/courier/partner");
    } catch (err) {
      msgBox.innerHTML = `<div class="alert alert-error" style="background:#fee2e2;color:#b91c1c;padding:10px;border-radius:8px;margin-bottom:12px;font-size:0.875rem;">${err.message || "Could not complete onboarding"}</div>`;
      btn.disabled = false;
      btn.textContent = "Start Delivering";
    }
  });
})();

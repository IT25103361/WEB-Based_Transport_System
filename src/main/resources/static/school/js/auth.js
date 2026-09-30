document.getElementById("loginForm")?.addEventListener("submit", async event => {
  event.preventDefault(); const email = document.getElementById("email"), password = document.getElementById("password"), error = document.getElementById("loginError"), button = event.submitter;
  error.textContent = ""; [email,password].forEach(i => i.removeAttribute("aria-invalid"));
  if (!email.value || !password.value) { error.textContent="Enter your email and password to continue."; (!email.value?email:password).focus(); return; }
  button.disabled=true;button.textContent="Signing in…";
  try { const result=await Api.login(email.value,password.value); const token=result?.token||result?.accessToken||result?.jwt; const parentId=result?.parentId||result?.parent?.id||result?.user?.parentId||result?.user?.id; if(!token||!parentId) throw new Error("Sign-in response is missing the required session information."); localStorage.setItem("nextgo_token",token);localStorage.setItem("nextgo_parent_id",parentId);location.href="dashboard"; }
  catch(err){ error.textContent=err.message;email.setAttribute("aria-invalid","true");password.setAttribute("aria-invalid","true");button.disabled=false;button.innerHTML="Sign In <span>→</span>"; }
});

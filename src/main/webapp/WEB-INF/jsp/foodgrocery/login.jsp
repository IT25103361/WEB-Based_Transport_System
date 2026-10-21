<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Authentication - NEXT GO</title>
    <link rel="stylesheet" href="${contextPath}/foodgrocery/css/style.css?v=3">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: url('${contextPath}/foodgrocery/images/bg-food.png') no-repeat center center fixed !important; background-size: cover !important; }
        .login-card { max-width: 450px; width: 100%; padding: 2rem; background: var(--bg-card); border-radius: 20px; border: 2px solid var(--clr-primary); box-shadow: 0 0 20px rgba(58, 76, 240, 0.3); text-align: center; }
        .login-card h2 { color: var(--clr-primary-light); margin-bottom: 1.5rem; }
        .tabs { display: flex; border-bottom: 1px solid var(--clr-border); margin-bottom: 1.5rem; }
        .tab-btn { flex: 1; padding: 1rem; background: none; border: none; color: var(--clr-text-muted); cursor: pointer; font-weight: 600; font-size: 1rem; transition: all 0.3s; border-bottom: 2px solid transparent; }
        .tab-btn.active { color: var(--clr-primary); border-bottom-color: var(--clr-primary); }
        .tab-content { display: none; }
        .tab-content.active { display: block; }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
    </style>
</head>
<body>
    <a href="${contextPath}/ride/home" style="position: absolute; top: 2rem; left: 2rem; text-decoration: none; color: var(--clr-text-muted); display: flex; align-items: center; gap: 0.5rem; font-weight: 600; font-size: 0.95rem; background: var(--bg-card); padding: 0.6rem 1.2rem; border-radius: 12px; border: 1px solid var(--clr-border); box-shadow: 0 4px 6px rgba(0,0,0,0.1); transition: all 0.2s;" onmouseover="this.style.color='var(--clr-primary)'; this.style.borderColor='var(--clr-primary)'" onmouseout="this.style.color='var(--clr-text-muted)'; this.style.borderColor='var(--clr-border)'">
        <span style="font-size: 1.2rem; line-height: 1;">&larr;</span> Go to NEXTGO Hub
    </a>
    
    <div class="login-card">
        <h2>🍔 NEXT GO</h2>
        
        <div class="tabs">
            <button class="tab-btn active" data-tab="login">Sign In</button>
            <button class="tab-btn" data-tab="register">Register</button>
        </div>

        <div id="tab-login" class="tab-content active">
            <form id="login-form">
                <div class="form-group" style="text-align: left;">
                    <label class="form-label">Email</label>
                    <input type="text" id="login-email" class="form-control" placeholder="Enter email or username" required>
                </div>
                <div class="form-group" style="text-align: left;">
                    <label class="form-label">Password</label>
                    <input type="password" id="login-password" class="form-control" placeholder="Enter password" required>
                </div>
                <div id="login-error" style="color: var(--clr-danger); margin-bottom: 1rem; display: none;"></div>
                <button type="submit" class="btn btn-full btn-primary" id="login-btn" style="margin-top: 1rem; padding: 0.8rem;">🚀 Sign In</button>
            </form>
        </div>

        <div id="tab-register" class="tab-content">
            <form id="register-form">
                <div class="form-group" style="text-align: left;">
                    <label class="form-label">Full Name</label>
                    <input type="text" id="reg-name" class="form-control" required>
                </div>
                <div class="form-row">
                    <div class="form-group" style="text-align: left;">
                        <label class="form-label">Email</label>
                        <input type="email" id="reg-email" class="form-control" required>
                    </div>
                    <div class="form-group" style="text-align: left;">
                        <label class="form-label">Phone Number</label>
                        <input type="text" id="reg-phone" class="form-control" required>
                    </div>
                </div>
                <div class="form-group" style="text-align: left;">
                    <label class="form-label">Password</label>
                    <input type="password" id="reg-password" class="form-control" required>
                </div>
                <div class="form-row">
                    <div class="form-group" style="text-align: left;">
                        <label class="form-label">Role</label>
                        <select id="reg-role" class="form-control" required>
                            <option value="CUSTOMER">Customer</option>
                            <option value="DELIVERY_PARTNER">Delivery Partner</option>
                        </select>
                    </div>
                    <div class="form-group" style="text-align: left;">
                        <label class="form-label">Gender</label>
                        <select id="reg-gender" class="form-control" required onchange="toggleJokeGenders()">
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                </div>
                <div class="form-group" id="joke-gender-group" style="text-align: left; display: none;">
                    <label class="form-label" style="color: var(--clr-primary-light);">Wait, what kind of other?</label>
                    <select class="form-control" id="joke-gender-select">
                        <option value="DOG">DOG 🐕</option>
                        <option value="Doggy bog">Doggy bog 🐩</option>
                        <option value="puppy">puppy 🐶</option>
                        <option value="alien">Alien 👽</option>
                        <option value="helicopter">Attack Helicopter 🚁</option>
                    </select>
                </div>
                <div class="form-row">
                    <div class="form-group" style="text-align: left;">
                        <label class="form-label">Address</label>
                        <input type="text" id="reg-address" class="form-control" required>
                    </div>
                    <div class="form-group" style="text-align: left;">
                        <label class="form-label">Postal Code</label>
                        <input type="text" id="reg-postal-code" class="form-control" required>
                    </div>
                </div>
                <div id="register-error" style="color: var(--clr-danger); margin-bottom: 1rem; display: none;"></div>
                <div id="register-success" style="color: var(--clr-success); margin-bottom: 1rem; display: none;"></div>
                <button type="submit" class="btn btn-full btn-primary" id="register-btn" style="margin-top: 1rem; padding: 0.8rem;">✨ Create Account</button>
            </form>
        </div>

    </div>
    
    <script>
        function toggleJokeGenders() {
            const genderSelect = document.getElementById('reg-gender');
            const jokeGroup = document.getElementById('joke-gender-group');
            if (genderSelect.value === 'OTHER') {
                jokeGroup.style.display = 'block';
            } else {
                jokeGroup.style.display = 'none';
            }
        }
        
        // Auto-select register tab if URL is /register
        document.addEventListener('DOMContentLoaded', () => {
            if (window.location.pathname.includes('register')) {
                document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
                
                const regBtn = document.querySelector('.tab-btn[data-tab="register"]');
                const regTab = document.getElementById('tab-register');
                
                if (regBtn) regBtn.classList.add('active');
                if (regTab) regTab.classList.add('active');
            }
        });
    </script>
    <script>window.APP_CONTEXT_PATH = '${contextPath}';</script>
    <script src="${contextPath}/foodgrocery/js/api.js?v=4"></script>
    <script src="${contextPath}/foodgrocery/js/login.js?v=3"></script>
</body>
</html>

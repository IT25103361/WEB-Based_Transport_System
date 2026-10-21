<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Rider Dashboard - NEXT GO</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --clr-primary: #D83131;
            --clr-success: #28a745;
            --clr-text: #333;
            --clr-text-muted: #888;
        }
        body { margin: 0; font-family: 'Inter', sans-serif; background: url('${contextPath}/foodgrocery/images/hero-banner.png') center/cover fixed; }
        .overlay { min-height: 100vh; background: rgba(255,255,255,0.6); padding: 2rem; box-sizing: border-box; }
        .dashboard-container { max-width: 800px; margin: 0 auto; background: rgba(255,255,255,0.85); backdrop-filter: blur(15px); padding: 2rem; border-radius: 16px; box-shadow: 0 10px 40px rgba(0,0,0,0.15); border: 1px solid rgba(255,255,255,0.5); }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
        .header h1 { margin: 0; font-weight: 800; color: var(--clr-text); font-size: 2rem; }
        .btn-logout { background: transparent; border: 2px solid var(--clr-primary); color: var(--clr-primary); padding: 0.5rem 1rem; border-radius: 8px; cursor: pointer; font-weight: 600; }
        .btn-logout:hover { background: var(--clr-primary); color: white; }
        .tabs { display: flex; border-bottom: 2px solid #eee; margin-bottom: 1.5rem; }
        .tab { padding: 1rem 2rem; cursor: pointer; font-weight: 600; color: var(--clr-text-muted); border-bottom: 3px solid transparent; transition: all 0.3s; }
        .tab.active { color: var(--clr-primary); border-bottom-color: var(--clr-primary); }
        .job-card { background: #fff; border-radius: 12px; padding: 1.5rem; margin-bottom: 1rem; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border: 1px solid #f0f0f0; }
        .job-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f0f0f0; padding-bottom: 0.5rem; margin-bottom: 1rem; }
        .job-id { font-weight: 700; color: var(--clr-text); font-size: 1.1rem; }
        .route-point { margin-bottom: 0.8rem; }
        .route-label { font-size: 0.8rem; color: var(--clr-text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: 1px; }
        .route-addr { font-size: 1.1rem; color: var(--clr-text); font-weight: 500; }
        .badge { padding: 0.3rem 0.8rem; border-radius: 20px; font-size: 0.8rem; font-weight: 700; background: #e3f2fd; color: #0d47a1; }
        .btn { padding: 0.6rem 1.5rem; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 1rem; color: white; border: none; }
        .btn-primary { background: var(--clr-primary); box-shadow: 0 4px 10px rgba(216,49,49,0.3); }
        .btn-success { background: var(--clr-success); box-shadow: 0 4px 10px rgba(40,167,69,0.3); }
        .empty-state { text-align: center; padding: 3rem; color: var(--clr-text-muted); font-weight: 500; }
    </style>
</head>
<body>
    <div class="overlay">
        <div class="dashboard-container">
            <div class="header">
                <h1><i class="fa-solid fa-motorcycle" style="color: var(--clr-primary);"></i> Rider Dashboard</h1>
                <button class="btn-logout" onclick="logout()">Logout</button>
            </div>
            
            <div class="tabs">
                <div class="tab active" data-tab="new">Available Orders</div>
                <div class="tab" data-tab="active">My Deliveries</div>
                <div class="tab" data-tab="history">Completed</div>
            </div>
            
            <div id="new-jobs-container">
                <!-- Injected via JS -->
            </div>
            
            <div id="active-jobs-container" style="display:none;">
                <!-- Injected via JS -->
            </div>
            
            <div id="history-jobs-container" style="display:none;">
                <!-- Injected via JS -->
            </div>
        </div>
    </div>

    <!-- Scripts -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.6.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    <script>
        window.APP_CONTEXT_PATH = '${contextPath}';
        <c:if test="${not empty foodUser}">
        window.SERVER_FOOD_USER = {
            id: ${foodUser.id},
            userId: ${foodUser.id},
            fullName: '<c:out value="${foodUser.fullName}" />',
            email: '<c:out value="${foodUser.email}" />',
            role: '<c:out value="${foodUser.role.name()}" />'
        };
        </c:if>
    </script>
    <script src="${contextPath}/foodgrocery/js/api.js"></script>
    <script>
        function logout() {
            localStorage.removeItem('foodAppUser');
            window.location.href = '/login.html';
        }
        function statusBadge(status) {
            return `<span class="badge">${status}</span>`;
        }
        function formatCurrency(amount) {
            return 'Rs. ' + amount.toFixed(2);
        }
        
        // Custom Modal and Toast implementation
        function createModal(msg, type = 'toast') {
            return new Promise((resolve) => {
                const overlay = document.createElement('div');
                overlay.style.cssText = 'position:fixed; inset:0; background:rgba(0,0,0,0.5); display:flex; align-items:center; justify-content:center; z-index:9999; backdrop-filter:blur(3px); opacity:0; transition:opacity 0.3s;';
                
                const box = document.createElement('div');
                box.style.cssText = 'background:white; padding:2rem; border-radius:16px; text-align:center; box-shadow:0 10px 40px rgba(0,0,0,0.2); max-width:400px; width:90%; transform:translateY(-20px); transition:transform 0.3s; border: 2px solid var(--clr-primary);';
                
                const icon = document.createElement('div');
                icon.innerHTML = type === 'success' ? '<i class="fa-solid fa-circle-check" style="font-size:3rem; color:var(--clr-success); margin-bottom:1rem;"></i>' 
                               : type === 'confirm' ? '<i class="fa-solid fa-circle-question" style="font-size:3rem; color:var(--clr-primary); margin-bottom:1rem;"></i>'
                               : '<i class="fa-solid fa-bell" style="font-size:3rem; color:var(--clr-primary); margin-bottom:1rem;"></i>';
                
                const text = document.createElement('h3');
                text.style.cssText = 'margin:0 0 1.5rem 0; color:var(--clr-text); font-weight:600; font-size:1.2rem;';
                text.innerText = msg;
                
                box.appendChild(icon);
                box.appendChild(text);
                
                const btnContainer = document.createElement('div');
                btnContainer.style.cssText = 'display:flex; justify-content:center; gap:1rem;';
                
                const okBtn = document.createElement('button');
                okBtn.className = 'btn btn-primary';
                okBtn.innerText = 'OK';
                okBtn.onclick = () => { closeAndResolve(true); };
                
                if (type === 'confirm') {
                    const cancelBtn = document.createElement('button');
                    cancelBtn.style.cssText = 'background:transparent; border:2px solid var(--clr-text-muted); color:var(--clr-text-muted); padding:0.6rem 1.5rem; border-radius:8px; cursor:pointer; font-weight:600; font-size:1rem;';
                    cancelBtn.innerText = 'Cancel';
                    cancelBtn.onclick = () => { closeAndResolve(false); };
                    btnContainer.appendChild(cancelBtn);
                    okBtn.innerText = 'Confirm';
                }
                
                btnContainer.appendChild(okBtn);
                box.appendChild(btnContainer);
                overlay.appendChild(box);
                document.body.appendChild(overlay);
                
                // Animate in
                requestAnimationFrame(() => {
                    overlay.style.opacity = '1';
                    box.style.transform = 'translateY(0)';
                });
                
                function closeAndResolve(val) {
                    overlay.style.opacity = '0';
                    box.style.transform = 'translateY(-20px)';
                    setTimeout(() => {
                        document.body.removeChild(overlay);
                        resolve(val);
                    }, 300);
                }
            });
        }

        window.confirmDialog = async function(msg) {
            return await createModal(msg, 'confirm');
        };
        window.toast = function(msg, type = 'info') {
            createModal(msg, type);
        };
    </script>
    <script src="${contextPath}/foodgrocery/js/partner.js"></script>
</body>
</html>

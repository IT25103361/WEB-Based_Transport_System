<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 - Not Found - NEXT GO</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/foodgrocery/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { display: flex; align-items: center; justify-content: center; min-height: 100vh; text-align: center; }
        .error-card { max-width: 600px; padding: 4rem; background: var(--bg-card); border-radius: 20px; border: 2px solid var(--clr-danger); box-shadow: 0 0 30px rgba(252, 75, 108, 0.2); }
        .error-code { font-size: 8rem; font-weight: 900; color: var(--clr-danger); line-height: 1; margin-bottom: 1rem; }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="error-code">404</div>
        <h2 style="color: white; margin-bottom: 1rem;">Page Not Found</h2>
        <p style="color: var(--text-muted); margin-bottom: 2rem;">The module or page you are looking for does not exist or has been moved.</p>
        <a href="${pageContext.request.contextPath}/food" class="btn btn-primary" style="padding: 1rem 2rem; font-size: 1.1rem;">⬅ Return to Hub</a>
    </div>
</body>
</html>

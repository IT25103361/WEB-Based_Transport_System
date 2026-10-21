<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 - Server Error - NEXT GO</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/foodgrocery/css/style.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <style>
        body { display: flex; align-items: center; justify-content: center; min-height: 100vh; text-align: center; }
        .error-card { max-width: 600px; padding: 4rem; background: var(--bg-card); border-radius: 20px; border: 2px solid var(--clr-warning); box-shadow: 0 0 30px rgba(255, 193, 7, 0.2); }
        .error-code { font-size: 8rem; font-weight: 900; color: var(--clr-warning); line-height: 1; margin-bottom: 1rem; }
    </style>
</head>
<body>
    <div class="error-card">
        <div class="error-code">500</div>
        <h2 style="color: white; margin-bottom: 1rem;">Internal Server Error</h2>
        <p style="color: var(--text-muted); margin-bottom: 2rem;">Oops! Something broke on the backend of the NEXT GO system. Our team has been notified.</p>
        <a href="${pageContext.request.contextPath}/food" class="btn btn-outline border-yellow text-yellow" style="padding: 1rem 2rem; font-size: 1.1rem;">⬅ Return to Hub</a>
    </div>
</body>
</html>

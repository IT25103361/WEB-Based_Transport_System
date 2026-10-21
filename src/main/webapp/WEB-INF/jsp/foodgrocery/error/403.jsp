<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>403 - Forbidden</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/foodgrocery/css/style.css">
    <style>
        .error-container { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100vh; text-align: center; }
        .error-code { font-size: 8rem; font-weight: 900; color: var(--clr-warning); line-height: 1; }
        .error-message { font-size: 2rem; margin-bottom: var(--sp-md); font-weight: 700; }
        .error-desc { color: var(--clr-text-muted); margin-bottom: var(--sp-xl); font-size: 1.1rem; }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-code">403</div>
        <div class="error-message">Access Denied</div>
        <div class="error-desc">You don't have permission to access this resource.</div>
        <a href="${pageContext.request.contextPath}/food" class="btn btn-primary">Go to Homepage</a>
    </div>
</body>
</html>

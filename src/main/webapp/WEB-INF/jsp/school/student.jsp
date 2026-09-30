<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="app-context" content="${pageContext.request.contextPath}">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>My Student | NEXTGO School Transport</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/animations.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/responsive.css">
</head>
<body data-page="student">
<div id="app"></div>
<script src="${pageContext.request.contextPath}/school/js/api.js"></script>
<script src="${pageContext.request.contextPath}/school/js/road_data.js"></script>
<script src="${pageContext.request.contextPath}/school/js/simulation.js"></script>
<script src="${pageContext.request.contextPath}/school/js/main.js"></script>
<script src="${pageContext.request.contextPath}/school/js/student.js"></script>
</body>
</html>
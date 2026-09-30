<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="app-context" content="${pageContext.request.contextPath}">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Live Bus Tracking | NEXTGO</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <!-- Leaflet CSS for real interactive map -->
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
  <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/animations.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/tracking.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/responsive.css">
</head>
<body data-page="tracking">
<div id="app"></div>

<!-- Leaflet JS -->
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
<script src="${pageContext.request.contextPath}/school/js/api.js"></script>
<script src="${pageContext.request.contextPath}/school/js/road_data.js"></script>
<script src="${pageContext.request.contextPath}/school/js/simulation.js"></script>
<script src="${pageContext.request.contextPath}/school/js/main.js"></script>
<script src="${pageContext.request.contextPath}/school/js/tracking.js"></script>
</body>
</html>

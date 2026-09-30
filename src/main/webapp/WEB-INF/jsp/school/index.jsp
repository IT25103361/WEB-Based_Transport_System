<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="app-context" content="${pageContext.request.contextPath}">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>NEXTGO | School Transport</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/style.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/animations.css">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/school/css/responsive.css">
</head>
<body class="landing">
<header class="landing-header">
  <nav class="public-nav">
    <a class="brand" href="${pageContext.request.contextPath}/school/index">
        <span class="brand-icon">
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
            <polyline points="2 17 12 22 22 17"></polyline>
            <polyline points="2 12 12 17 22 12"></polyline>
          </svg>
        </span>
      NEXTGO
    </a>
    <div class="nav-links-wrap">
      <a href="${pageContext.request.contextPath}/ride/home">All services</a>
      <a href="#safety">Safety</a>
      <a href="#how-it-works">How it works</a>
      <button class="theme-toggle-btn" id="themeToggleBtn" type="button" onclick="toggleAppTheme()" title="Toggle Theme" aria-label="Toggle theme">
        <span class="theme-toggle-icon"></span> <span class="theme-toggle-label">Theme</span>
      </button>
      <a class="button secondary header-login-btn" href="${pageContext.request.contextPath}/school/login">Parent sign in</a>
    </div>
  </nav>
</header>

<main>
  <section class="hero">

    <div class="hero-copy entrance">
      <span class="eyebrow">SCHOOL STUDENT TRANSPORT</span>
      <h1>Safe Journeys.<br><em>Smarter</em> School Transport.</h1>
      <p>Track your child's school journey, monitor the bus in real time, and receive important arrival updates.</p>
      <div class="button-row">
        <a class="button" href="${pageContext.request.contextPath}/school/login">Get Started <span>&rarr;</span></a>
        <a class="button secondary" href="${pageContext.request.contextPath}/school/tracking">Explore Tracking <span>&rarr;</span></a>
      </div>
    </div>

    <div class="hero-visual" aria-label="Illustration of a school bus on a route">
      <img src="${pageContext.request.contextPath}/school/assets/images/hero-school-bus.svg" alt="Yellow school bus travelling safely">
      <div class="float-pill live"><span class="pulse-dot"></span> LIVE GPS</div>
      <div class="float-pill eta">12 <small>MIN ETA</small></div>
    </div>

    <div class="route-orbit orbit-a"></div>
    <div class="route-orbit orbit-b"></div>
  </section>

  <section class="section" id="how-it-works">
    <div class="section-intro">
      <span class="eyebrow">HOW IT WORKS</span>
      <h2>Every school journey,<br>clearly in view.</h2>
    </div>

    <div class="steps">
      <article class="glass step">
        <b>01</b>
        <span class="icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
          </span>
        <h3>Select your child</h3>
        <p>See the transport details that matter for each student.</p>
      </article>

      <article class="glass step">
        <b>02</b>
        <span class="icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polygon points="3 11 22 2 13 21 11 13 3 11"></polygon>
            </svg>
          </span>
        <h3>Track the school bus</h3>
        <p>Follow the route, current position, next stop and ETA.</p>
      </article>

      <article class="glass step">
        <b>03</b>
        <span class="icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
          </span>
        <h3>Receive updates</h3>
        <p>Know about departures, arrivals, delays and GPS status.</p>
      </article>

      <article class="glass step">
        <b>04</b>
        <span class="icon">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          </span>
        <h3>Confirm safe arrival</h3>
        <p>Get reassurance when your child arrives at school.</p>
      </article>
    </div>
  </section>

  <section class="section safety" id="safety">
    <div class="safety-image">
      <img src="${pageContext.request.contextPath}/school/assets/images/school-transport.svg" alt="School bus route and school building">
    </div>
    <div>
      <span class="eyebrow">BUILT FOR REASSURANCE</span>
      <h2>Safety is more than a destination.</h2>
      <p>GPS visibility, route awareness and arrival notifications help parents stay confidently connected throughout the journey.</p>
      <div class="safety-points">
        <span><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg> Live location monitoring</span>
        <span><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg> Smart route notifications</span>
        <span><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="20 6 9 17 4 12"></polyline></svg> Arrival confirmation</span>
      </div>
    </div>
  </section>

  <section class="final-cta glass">
    <span class="eyebrow">READY WHEN YOU ARE</span>
    <h2>Ready to monitor your child's journey?</h2>
    <a class="button" href="${pageContext.request.contextPath}/school/login">Get Started &rarr;</a>
  </section>
</main>

<footer>
    <span class="brand">
      <span class="brand-icon">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round">
          <polygon points="12 2 2 7 12 12 22 7 12 2"></polygon>
          <polyline points="2 17 12 22 22 17"></polyline>
          <polyline points="2 12 12 17 22 12"></polyline>
        </svg>
      </span>
      NEXTGO
    </span>
  <span>School Student Transport</span>
  <div>
    <a href="${pageContext.request.contextPath}/school/dashboard">Dashboard</a>
    <a href="${pageContext.request.contextPath}/school/tracking">Live Tracking</a>
    <a href="${pageContext.request.contextPath}/school/schedule">Schedule</a>
  </div>
  <span>&copy; 2026 NEXTGO</span>
</footer>
<script src="${pageContext.request.contextPath}/school/js/main.js"></script>
</body>
</html>
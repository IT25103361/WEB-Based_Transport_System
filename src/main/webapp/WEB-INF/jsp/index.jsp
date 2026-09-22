<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>RESQ - Emergency Response</title>

    <style>

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: Arial, Helvetica, sans-serif;
            background: #f4f8fc;
            color: #17365d;
        }

        /* =========================
           TOP BAR
        ========================= */

        .topbar {
            height: 72px;
            background: white;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 6%;
            border-bottom: 1px solid #e5edf5;
        }

        .logo {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .logo-icon {
            width: 45px;
            height: 45px;
            border-radius: 13px;
            background: #146ee8;
            color: white;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 25px;
            font-weight: bold;
        }

        .logo-text h2 {
            font-size: 22px;
        }

        .logo-text span {
            font-size: 11px;
            color: #718096;
        }

        .help-button {
            border: none;
            background: #146ee8;
            color: white;
            padding: 13px 22px;
            border-radius: 12px;
            font-weight: bold;
            cursor: pointer;
        }

        /* =========================
           MAIN
        ========================= */

        .container {
            max-width: 1050px;
            margin: auto;
            padding: 35px 20px 60px;
        }

        .welcome {
            text-align: center;
            margin-bottom: 35px;
        }

        .welcome .small-title {
            display: inline-block;
            background: #e5f1ff;
            color: #146ee8;
            padding: 8px 16px;
            border-radius: 30px;
            font-size: 13px;
            font-weight: bold;
            margin-bottom: 15px;
        }

        .welcome h1 {
            font-size: 42px;
            margin-bottom: 10px;
            color: #17365d;
        }

        .welcome p {
            color: #718096;
            font-size: 16px;
        }

        /* =========================
           PROGRESS
        ========================= */

        .progress {
            display: flex;
            justify-content: center;
            align-items: center;
            margin: 30px auto 35px;
            max-width: 750px;
        }

        .progress-step {
            display: flex;
            flex-direction: column;
            align-items: center;
            position: relative;
            flex: 1;
        }

        .circle {
            width: 45px;
            height: 45px;
            border-radius: 50%;
            background: #e4ebf3;
            color: #718096;
            display: flex;
            justify-content: center;
            align-items: center;
            font-weight: bold;
            z-index: 2;
        }

        .progress-step.active .circle {
            background: #146ee8;
            color: white;
        }

        .progress-step.completed .circle {
            background: #19a974;
            color: white;
        }

        .progress-step span {
            margin-top: 8px;
            font-size: 12px;
            color: #718096;
        }

        .progress-line {
            height: 3px;
            background: #e4ebf3;
            flex: 1;
            margin-top: -25px;
        }

        /* =========================
           STEP CARD
        ========================= */

        .step-card {
            background: white;
            border-radius: 24px;
            padding: 35px;
            box-shadow: 0 8px 30px rgba(35, 75, 115, 0.08);
            border: 1px solid #e5edf5;
        }

        .step {
            display: none;
        }

        .step.active {
            display: block;
        }

        .step-title {
            text-align: center;
            margin-bottom: 30px;
        }

        .step-title .big-icon {
            font-size: 55px;
            margin-bottom: 10px;
        }

        .step-title h2 {
            font-size: 28px;
            color: #17365d;
            margin-bottom: 8px;
        }

        .step-title p {
            color: #718096;
        }

        /* =========================
           EMERGENCY CHOICES
        ========================= */

        .emergency-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 20px;
        }

        .choice-card {
            border: 3px solid #edf2f7;
            background: white;
            border-radius: 20px;
            padding: 28px 15px;
            text-align: center;
            cursor: pointer;
            transition: 0.2s;
        }

        .choice-card:hover {
            transform: translateY(-4px);
            border-color: #146ee8;
            box-shadow: 0 8px 20px rgba(20, 110, 232, 0.12);
        }

        .choice-card.selected {
            border-color: #146ee8;
            background: #f0f7ff;
        }

        .choice-icon {
            font-size: 65px;
            margin-bottom: 15px;
        }

        .choice-card h3 {
            font-size: 20px;
            margin-bottom: 7px;
        }

        .choice-card p {
            font-size: 13px;
            color: #718096;
            line-height: 1.4;
        }

        /* =========================
           SITUATION
        ========================= */

        .situation-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 15px;
        }

        .situation {
            border: 2px solid #e5edf5;
            background: white;
            border-radius: 17px;
            padding: 22px 10px;
            text-align: center;
            cursor: pointer;
            font-weight: bold;
        }

        .situation:hover,
        .situation.selected {
            border-color: #146ee8;
            background: #eef6ff;
        }

        .situation .icon {
            display: block;
            font-size: 42px;
            margin-bottom: 10px;
        }

        /* =========================
           FORM
        ========================= */

        .form-box {
            max-width: 650px;
            margin: auto;
        }

        .input-group {
            margin-bottom: 18px;
        }

        .input-group label {
            display: block;
            font-weight: bold;
            margin-bottom: 7px;
            font-size: 14px;
        }

        .input-group input,
        .input-group textarea {
            width: 100%;
            border: 2px solid #e2e8f0;
            border-radius: 12px;
            padding: 14px;
            font-size: 15px;
            outline: none;
        }

        .input-group input:focus,
        .input-group textarea:focus {
            border-color: #146ee8;
        }

        textarea {
            min-height: 100px;
            resize: vertical;
        }

        .location-button {
            width: 100%;
            padding: 15px;
            border: 2px dashed #146ee8;
            background: #f1f7ff;
            color: #146ee8;
            border-radius: 12px;
            cursor: pointer;
            font-weight: bold;
            margin-bottom: 18px;
        }

        /* =========================
           CONFIRMATION
        ========================= */

        .summary {
            max-width: 650px;
            margin: auto;
        }

        .summary-item {
            display: flex;
            align-items: center;
            gap: 18px;
            padding: 18px;
            border-bottom: 1px solid #edf2f7;
        }

        .summary-icon {
            width: 55px;
            height: 55px;
            border-radius: 15px;
            background: #eef6ff;
            display: flex;
            justify-content: center;
            align-items: center;
            font-size: 28px;
        }

        .summary-item small {
            color: #718096;
            display: block;
            margin-bottom: 4px;
        }

        .summary-item strong {
            font-size: 17px;
        }

        /* =========================
           BUTTONS
        ========================= */

        .buttons {
            display: flex;
            justify-content: space-between;
            margin-top: 30px;
            gap: 15px;
        }

        .btn {
            border: none;
            border-radius: 13px;
            padding: 15px 25px;
            font-size: 15px;
            font-weight: bold;
            cursor: pointer;
        }

        .btn-back {
            background: #edf2f7;
            color: #4a5568;
        }

        .btn-next {
            background: #146ee8;
            color: white;
            margin-left: auto;
        }

        .btn-emergency {
            width: 100%;
            background: #e53935;
            color: white;
            font-size: 19px;
            padding: 20px;
            border-radius: 15px;
            border: none;
            font-weight: bold;
            cursor: pointer;
            margin-top: 25px;
        }

        .btn-emergency:hover {
            background: #c62828;
        }

        /* =========================
           VOICE
        ========================= */

        .voice-button {
            margin: 25px auto 0;
            display: block;
            border: 2px solid #146ee8;
            background: white;
            color: #146ee8;
            border-radius: 30px;
            padding: 12px 25px;
            cursor: pointer;
            font-weight: bold;
        }

        /* =========================
           FOOTER
        ========================= */

        footer {
            background: #17365d;
            color: white;
            text-align: center;
            padding: 22px;
            font-size: 13px;
        }

        /* =========================
           MOBILE
        ========================= */

        @media(max-width: 700px) {

            .topbar {
                padding: 0 20px;
            }

            .help-button {
                display: none;
            }

            .welcome h1 {
                font-size: 30px;
            }

            .step-card {
                padding: 22px;
            }

            .emergency-grid {
                grid-template-columns: 1fr;
            }

            .situation-grid {
                grid-template-columns: repeat(2, 1fr);
            }

            .progress-step span {
                font-size: 10px;
            }
        }

    </style>
</head>

<body>

<!-- =========================
     HEADER
========================= -->

<header class="topbar">

    <div class="logo">

        <div class="logo-icon">
            +
        </div>

        <div class="logo-text">
            <h2>RESQ</h2>
            <span>Emergency Response Platform</span>
        </div>

    </div>

    <button class="help-button">
        🔊 HELP
    </button>

</header>


<div class="container">

    <!-- =========================
         WELCOME
    ========================= -->

    <div class="welcome">

        <div class="small-title">
            🛡️ EMERGENCY ASSISTANCE
        </div>

        <h1>How can we help you?</h1>

        <p>
            Choose an option below. We'll guide you step by step.
        </p>

    </div>


    <!-- =========================
         PROGRESS
    ========================= -->

    <div class="progress">

        <div class="progress-step active" id="progress1">
            <div class="circle">1</div>
            <span>Help</span>
        </div>

        <div class="progress-line"></div>

        <div class="progress-step" id="progress2">
            <div class="circle">2</div>
            <span>Problem</span>
        </div>

        <div class="progress-line"></div>

        <div class="progress-step" id="progress3">
            <div class="circle">3</div>
            <span>Details</span>
        </div>

        <div class="progress-line"></div>

        <div class="progress-step" id="progress4">
            <div class="circle">4</div>
            <span>Confirm</span>
        </div>

    </div>


    <div class="step-card">


        <!-- =====================================================
             STEP 1
        ====================================================== -->

        <div class="step active" id="step1">

            <div class="step-title">

                <div class="big-icon">
                    🚨
                </div>

                <h2>What kind of help do you need?</h2>

                <p>
                    Tap the picture that matches your emergency.
                </p>

            </div>


            <div class="emergency-grid">


                <div class="choice-card"
                     onclick="selectEmergency('Ambulance', this)">

                    <div class="choice-icon">
                        🚑
                    </div>

                    <h3>Medical</h3>

                    <p>
                        Someone is sick, injured or needs medical help.
                    </p>

                </div>


                <div class="choice-card"
                     onclick="selectEmergency('Police', this)">

                    <div class="choice-icon">
                        🚓
                    </div>

                    <h3>Police</h3>

                    <p>
                        Danger, crime or someone needs police assistance.
                    </p>

                </div>


                <div class="choice-card"
                     onclick="selectEmergency('Fire & Rescue', this)">

                    <div class="choice-icon">
                        🚒
                    </div>

                    <h3>Fire / Rescue</h3>

                    <p>
                        Fire, accident, rescue or dangerous situation.
                    </p>

                </div>

            </div>


            <button class="voice-button" onclick="speakText('Please choose the type of emergency you need help with.')">
                🔊 Listen
            </button>

        </div>



        <!-- =====================================================
             STEP 2
        ====================================================== -->

        <div class="step" id="step2">

            <div class="step-title">

                <div class="big-icon">
                    🆘
                </div>

                <h2>What happened?</h2>

                <p>
                    Choose the picture that best describes the situation.
                </p>

            </div>


            <div class="situation-grid">


                <div class="situation"
                     onclick="selectSituation('Injury', this)">
                    <span class="icon">🤕</span>
                    Injury
                </div>


                <div class="situation"
                     onclick="selectSituation('Accident', this)">
                    <span class="icon">🚗</span>
                    Accident
                </div>


                <div class="situation"
                     onclick="selectSituation('Fire', this)">
                    <span class="icon">🔥</span>
                    Fire
                </div>


                <div class="situation"
                     onclick="selectSituation('Danger', this)">
                    <span class="icon">⚠️</span>
                    Danger
                </div>


                <div class="situation"
                     onclick="selectSituation('Unconscious person', this)">
                    <span class="icon">😵</span>
                    Unconscious
                </div>


                <div class="situation"
                     onclick="selectSituation('Other emergency', this)">
                    <span class="icon">❓</span>
                    Other
                </div>

            </div>


            <button class="voice-button"
                    onclick="speakText('Please choose what happened.')">
                🔊 Listen
            </button>


            <div class="buttons">

                <button class="btn btn-back"
                        onclick="previousStep(1)">
                    ← Back
                </button>

                <button class="btn btn-next"
                        onclick="nextFromStep2()">
                    Continue →
                </button>

            </div>

        </div>



        <!-- =====================================================
             STEP 3
        ====================================================== -->

        <div class="step" id="step3">

            <div class="step-title">

                <div class="big-icon">
                    📍
                </div>

                <h2>Where do you need help?</h2>

                <p>
                    Give us your location and a way to contact you.
                </p>

            </div>


            <div class="form-box">


                <button type="button"
                        class="location-button"
                        onclick="useLocation()">

                    📍 USE MY CURRENT LOCATION

                </button>


                <div class="input-group">

                    <label>
                        📍 Emergency location
                    </label>

                    <input
                            type="text"
                            id="location"
                            placeholder="Example: Malabe, near SLIIT"
                            required>

                </div>


                <div class="input-group">

                    <label>
                        👤 Your name
                    </label>

                    <input
                            type="text"
                            id="requesterName"
                            placeholder="Enter your name"
                            required>

                </div>


                <div class="input-group">

                    <label>
                        📞 Phone number
                    </label>

                    <input
                            type="text"
                            id="phoneNumber"
                            placeholder="10 digit phone number"
                            maxlength="10"
                            required>

                </div>


                <div class="input-group">

                    <label>
                        📝 Tell us more
                    </label>

                    <textarea
                            id="description"
                            placeholder="Briefly explain what happened..."></textarea>

                </div>


            </div>


            <div class="buttons">

                <button class="btn btn-back"
                        onclick="previousStep(2)">
                    ← Back
                </button>

                <button class="btn btn-next"
                        onclick="goToConfirmation()">
                    Continue →
                </button>

            </div>

        </div>



        <!-- =====================================================
             STEP 4
        ====================================================== -->

        <div class="step" id="step4">

            <div class="step-title">

                <div class="big-icon">
                    ✅
                </div>

                <h2>Check your emergency request</h2>

                <p>
                    Make sure everything is correct before sending.
                </p>

            </div>


            <div class="summary">


                <div class="summary-item">

                    <div class="summary-icon">
                        🚨
                    </div>

                    <div>
                        <small>Emergency service</small>
                        <strong id="summaryService">-</strong>
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-icon">
                        🆘
                    </div>

                    <div>
                        <small>What happened</small>
                        <strong id="summarySituation">-</strong>
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-icon">
                        📍
                    </div>

                    <div>
                        <small>Location</small>
                        <strong id="summaryLocation">-</strong>
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-icon">
                        👤
                    </div>

                    <div>
                        <small>Requester</small>
                        <strong id="summaryName">-</strong>
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-icon">
                        📞
                    </div>

                    <div>
                        <small>Phone</small>
                        <strong id="summaryPhone">-</strong>
                    </div>

                </div>


            </div>


            <!-- REAL FORM SENT TO SPRING BOOT -->

            <form action="${pageContext.request.contextPath}/emergency/add"
                  method="post"
                  onsubmit="return prepareForm();">

                <!-- These hidden values connect the new UI
                     to your existing EmergencyService entity -->

                <input type="hidden"
                       name="serviceType"
                       id="formServiceType">

                <input type="hidden"
                       name="requesterName"
                       id="formRequesterName">

                <input type="hidden"
                       name="phoneNumber"
                       id="formPhoneNumber">

                <input type="hidden"
                       name="location"
                       id="formLocation">

                <input type="hidden"
                       name="description"
                       id="formDescription">

                <input type="hidden"
                       name="status"
                       value="Pending">


                <button type="submit"
                        class="btn-emergency">

                    🚨 SEND EMERGENCY REQUEST

                </button>

            </form>


            <div class="buttons">

                <button class="btn btn-back"
                        onclick="previousStep(3)">
                    ← Change Details
                </button>

            </div>


            <button class="voice-button"
                    onclick="speakText('Please check your emergency details. When you are ready, press the red button to send the emergency request.')">

                🔊 Listen

            </button>

        </div>

    </div>

</div>


<footer>

    RESQ Emergency Response Platform

</footer>



<script>

    let currentStep = 1;

    let selectedService = "";
    let selectedSituation = "";


    /* ==========================================
       SELECT EMERGENCY
    ========================================== */

    function selectEmergency(service, element) {

        selectedService = service;

        document
            .querySelectorAll(".choice-card")
            .forEach(card => card.classList.remove("selected"));

        element.classList.add("selected");

        setTimeout(function () {

            showStep(2);

        }, 250);
    }


    /* ==========================================
       SELECT SITUATION
    ========================================== */

    function selectSituation(situation, element) {

        selectedSituation = situation;

        document
            .querySelectorAll(".situation")
            .forEach(card => card.classList.remove("selected"));

        element.classList.add("selected");

    }


    /* ==========================================
       STEP 2 CONTINUE
    ========================================== */

    function nextFromStep2() {

        if (selectedSituation === "") {

            alert("Please choose what happened.");

            return;

        }

        showStep(3);
    }


    /* ==========================================
       LOCATION
    ========================================== */

    function useLocation() {

        const locationInput =
            document.getElementById("location");

        if (!navigator.geolocation) {

            alert("Location is not supported by this browser.");

            return;
        }

        locationInput.value = "Getting your location...";

        navigator.geolocation.getCurrentPosition(

            function(position) {

                const latitude =
                    position.coords.latitude;

                const longitude =
                    position.coords.longitude;

                locationInput.value =
                    "Current location (" +
                    latitude.toFixed(5) +
                    ", " +
                    longitude.toFixed(5) +
                    ")";

            },

            function() {

                locationInput.value = "";

                alert(
                    "Unable to get your location. Please enter it manually."
                );

            }

        );

    }


    /* ==========================================
       CONFIRMATION
    ========================================== */

    function goToConfirmation() {

        const name =
            document.getElementById("requesterName").value.trim();

        const phone =
            document.getElementById("phoneNumber").value.trim();

        const location =
            document.getElementById("location").value.trim();

        const description =
            document.getElementById("description").value.trim();


        if (location === "") {

            alert("Please provide the emergency location.");

            return;

        }


        if (name === "") {

            alert("Please enter your name.");

            return;

        }


        if (!/^[0-9]{10}$/.test(phone)) {

            alert("Phone number must contain exactly 10 digits.");

            return;

        }


        if (selectedService === "") {

            alert("Please select an emergency service.");

            return;

        }


        document.getElementById("summaryService").innerText =
            selectedService;

        document.getElementById("summarySituation").innerText =
            selectedSituation;

        document.getElementById("summaryLocation").innerText =
            location;

        document.getElementById("summaryName").innerText =
            name;

        document.getElementById("summaryPhone").innerText =
            phone;


        showStep(4);

    }


    /* ==========================================
       PREPARE REAL SPRING FORM
    ========================================== */

    function prepareForm() {

        document.getElementById("formServiceType").value =
            selectedService;

        document.getElementById("formRequesterName").value =
            document.getElementById("requesterName").value;

        document.getElementById("formPhoneNumber").value =
            document.getElementById("phoneNumber").value;

        document.getElementById("formLocation").value =
            document.getElementById("location").value;

        let description =
            document.getElementById("description").value;

        if (selectedSituation !== "") {

            description =
                "Situation: " +
                selectedSituation +
                ". " +
                description;

        }

        document.getElementById("formDescription").value =
            description;


        return true;

    }


    /* ==========================================
       SHOW STEP
    ========================================== */

    function showStep(stepNumber) {

        document
            .querySelectorAll(".step")
            .forEach(step => step.classList.remove("active"));


        document
            .getElementById("step" + stepNumber)
            .classList.add("active");


        currentStep = stepNumber;


        updateProgress(stepNumber);


        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

    }


    /* ==========================================
       BACK
    ========================================== */

    function previousStep(stepNumber) {

        showStep(stepNumber);

    }


    /* ==========================================
       PROGRESS
    ========================================== */

    function updateProgress(stepNumber) {

        for (let i = 1; i <= 4; i++) {

            const progress =
                document.getElementById("progress" + i);

            progress.classList.remove("active");
            progress.classList.remove("completed");


            if (i < stepNumber) {

                progress.classList.add("completed");

            }
            else if (i === stepNumber) {

                progress.classList.add("active");

            }

        }

    }


    /* ==========================================
       TEXT TO SPEECH
    ========================================== */

    function speakText(text) {

        if (!("speechSynthesis" in window)) {

            alert("Voice support is not available in this browser.");

            return;

        }

        window.speechSynthesis.cancel();

        const speech =
            new SpeechSynthesisUtterance(text);

        speech.rate = 0.9;

        speech.pitch = 1;

        window.speechSynthesis.speak(speech);

    }

</script>

</body>
</html>
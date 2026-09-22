<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Emergency Request</title>
    <style>
        body { font-family: Arial, sans-serif; background:#f5f7fb; padding:30px; }
        .card { max-width:700px; margin:auto; background:white; padding:25px; border-radius:12px; box-shadow:0 2px 10px rgba(0,0,0,.08); }
        label { display:block; margin-top:12px; font-weight:bold; }
        input, select, textarea { width:100%; box-sizing:border-box; padding:10px; margin-top:5px; border:1px solid #ccc; border-radius:6px; }
        textarea { min-height:80px; }
        button, a { display:inline-block; padding:10px 16px; margin-top:15px; border:0; border-radius:6px; background:#222; color:white; text-decoration:none; cursor:pointer; }
    </style>
</head>
<body>
<div class="card">
    <h1>Edit Emergency Request</h1>

    <form action="${pageContext.request.contextPath}/emergency/update" method="post">
        <input type="hidden" name="id" value="${emergencyService.id}">

        <label>Service Type</label>
        <select name="serviceType" required>
            <option ${emergencyService.serviceType == 'Ambulance' ? 'selected' : ''}>Ambulance</option>
            <option ${emergencyService.serviceType == 'Police' ? 'selected' : ''}>Police</option>
            <option ${emergencyService.serviceType == 'Fire & Rescue' ? 'selected' : ''}>Fire & Rescue</option>
            <option ${emergencyService.serviceType == 'Roadside Assistance' ? 'selected' : ''}>Roadside Assistance</option>
        </select>

        <label>Requester Name</label>
        <input type="text" name="requesterName" value="${emergencyService.requesterName}" required>

        <label>Phone Number</label>
        <input type="text" name="phoneNumber" value="${emergencyService.phoneNumber}" required>

        <label>Location</label>
        <input type="text" name="location" value="${emergencyService.location}" required>

        <label>Description</label>
        <textarea name="description">${emergencyService.description}</textarea>

        <label>Status</label>
        <select name="status" required>
            <option ${emergencyService.status == 'Pending' ? 'selected' : ''}>Pending</option>
            <option ${emergencyService.status == 'Accepted' ? 'selected' : ''}>Accepted</option>
            <option ${emergencyService.status == 'Completed' ? 'selected' : ''}>Completed</option>
            <option ${emergencyService.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
        </select>

        <button type="submit">Update Request</button>
        <a href="${pageContext.request.contextPath}/">Back</a>
    </form>
</div>
</body>
</html>

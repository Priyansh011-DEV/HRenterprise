const BASE_URL = "http://localhost:8081";

/* =========================
   🔐 AUTH FUNCTIONS
========================= */

function login() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch(`${BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    })
    .then(res => res.text())
    .then(token => {
        localStorage.setItem("token", token);

        const payload = parseJwt(token);
        const roles = payload.roles || payload.authorities || [];

        if (roles.includes("ROLE_EMPLOYEE")) {
            window.location.href = "employee.html";
        } else {
            window.location.href = "dashboard.html";
        }
    })
    .catch(err => {
        console.error(err);
        alert("Login failed ❌");
    });
}

function logout() {
    localStorage.removeItem("token");
    window.location.href = "login.html";
}

/* =========================
   🔐 JWT HELPER
========================= */

function parseJwt(token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch {
        return {};
    }
}

/* =========================
   🔒 PAGE PROTECTION
========================= */

function checkAuth() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const payload = parseJwt(token);
    const roles = payload.roles || payload.authorities || [];
    const page = window.location.pathname;

    if (page.includes("dashboard") && roles.includes("ROLE_EMPLOYEE")) {
        window.location.href = "employee.html";
    }

    if (page.includes("employee") && !roles.includes("ROLE_EMPLOYEE")) {
        window.location.href = "dashboard.html";
    }
}

/* =========================
   🔧 COMMON HELPERS
========================= */

function authHeader() {
    return {
        "Authorization": "Bearer " + localStorage.getItem("token")
    };
}

function getStatusColor(status) {
    if (status === "APPROVED") return "green";
    if (status === "REJECTED") return "red";
    return "orange";
}

function disableAllButtons() {
    document.querySelectorAll("button").forEach(btn => btn.disabled = true);
}

function enableAllButtons() {
    document.querySelectorAll("button").forEach(btn => btn.disabled = false);
}

/* =========================
   👨‍💼 EMPLOYEE MODULE
========================= */

function applyLeave() {

    const data = {
        leaveType: document.getElementById("leaveType").value,
        startDate: document.getElementById("startDate").value,
        endDate: document.getElementById("endDate").value,
        reason: document.getElementById("reason").value
    };

    disableAllButtons();

    fetch(`${BASE_URL}/api/leaves/apply`, {
        method: "POST",
        headers: {
            ...authHeader(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(() => {
        alert("Leave Applied ✅");
        loadMyLeaves();
    })
    .catch(err => {
        console.error(err);
        alert("Error applying leave ❌");
    })
    .finally(() => enableAllButtons());
}

function loadMyLeaves() {

    fetch(`${BASE_URL}/api/leaves/my`, {
        headers: authHeader()
    })
    .then(res => res.json())
    .then(data => {

        let html = "";

        data.forEach(l => {
            const color = getStatusColor(l.status);

            html += `
            <div class="card">
                <p><b>${l.leaveType}</b></p>
                <p>${l.startDate} → ${l.endDate}</p>
                <p>Status: <span style="color:${color}">${l.status}</span></p>
            </div>`;
        });

        document.getElementById("leaveList").innerHTML = html;
    })
    .catch(err => {
        console.error(err);
        alert("Failed to load leaves ❌");
    });
}

/* =========================
   🏢 ADMIN / HR MODULE
========================= */

function loadEmployees() {

    fetch(`${BASE_URL}/employees`, {
        headers: authHeader()
    })
    .then(res => res.json())
    .then(data => {

        let html = "";

        data.forEach(emp => {
            html += `
            <div class="card">
                <p><b>${emp.name}</b></p>
                <p>${emp.email}</p>
                <p>${emp.department}</p>
                <p>₹${emp.salary}</p>
            </div>`;
        });

        document.getElementById("employees").innerHTML = html;
    })
    .catch(err => {
        console.error(err);
        alert("Failed to load employees ❌");
    });
}

function loadLeaves() {

    fetch(`${BASE_URL}/api/leaves/all`, {
        headers: authHeader()
    })
    .then(res => res.json())
    .then(data => {

        let html = "";

        const payload = parseJwt(localStorage.getItem("token"));
        const roles = payload.roles || payload.authorities || [];

        const isHR = roles.includes("ROLE_HR");
        const isAdmin = roles.includes("ROLE_ADMIN");

        data.forEach(l => {

            const color = getStatusColor(l.status);

            let showButtons = false;

            if (l.status === "PENDING") {
                if (isHR && l.employeeRole === "EMPLOYEE") {
                    showButtons = true;
                }
                if (isAdmin) {
                    showButtons = true;
                }
            }

            html += `
            <div class="card">
                <p><b>${l.employeeName}</b> (${l.employeeRole})</p>
                <p>${l.leaveType}</p>
                <p>${l.startDate} → ${l.endDate}</p>
                <p>Status: <span style="color:${color}">${l.status}</span></p>

                ${showButtons ? `
                    <button onclick="approveLeave(${l.id})">Approve</button>
                    <button onclick="rejectLeave(${l.id})">Reject</button>
                ` : ""}
            </div>`;
        });

        document.getElementById("leaveRequests").innerHTML = html;
    })
    .catch(err => {
        console.error(err);
        alert("Failed to load leaves ❌");
    });
}

/* =========================
   🏢 ACTIONS (APPROVE / REJECT)
========================= */

function approveLeave(id) {
    disableAllButtons();
    updateLeaveStatus(id, "APPROVED");
}

function rejectLeave(id) {
    disableAllButtons();
    updateLeaveStatus(id, "REJECTED");
}

function updateLeaveStatus(id, status) {

    fetch(`${BASE_URL}/api/leaves/${id}/status?status=${status}`, {
        method: "PUT",
        headers: authHeader()
    })
    .then(res => res.json())
    .then(() => {
        alert(`Leave ${status} ✅`);
        loadLeaves();
    })
    .catch(err => {
        console.error(err);
        alert("Error updating leave ❌");
    })
    .finally(() => enableAllButtons());
}
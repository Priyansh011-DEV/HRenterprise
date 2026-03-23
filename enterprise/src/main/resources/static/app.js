const BASE_URL = "http://localhost:8081";

/* =========================
   🔐 AUTH FUNCTIONS
========================= */

function login() {
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!username || !password) {
        alert("Please enter username and password");
        return;
    }

    fetch(`${BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password })
    })
    .then(res => {
        if (!res.ok) throw new Error("Invalid credentials");
        return res.text();
    })
    .then(token => {
        token = token.trim(); // remove any whitespace/newlines
        localStorage.setItem("token", token);

        const payload = parseJwt(token);
        const roles = payload.roles || [];

        console.log("Logged in. Roles:", roles);

        if (roles.includes("EMPLOYEE")) {
            window.location.href = "employee.html";
        } else {
            window.location.href = "dashboard.html"; // ADMIN or HR
        }
    })
    .catch(err => {
        console.error(err);
        alert("Login failed ❌ " + err.message);
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

function getRoles() {
    const token = localStorage.getItem("token");
    if (!token) return [];
    return parseJwt(token).roles || [];
}

function isAdmin() { return getRoles().includes("ADMIN"); }
function isHR()    { return getRoles().includes("HR"); }
function isEmployee() { return getRoles().includes("EMPLOYEE"); }

/* =========================
   🔒 PAGE PROTECTION
========================= */

function checkAuth() {
    const token = localStorage.getItem("token");

    if (!token) {
        window.location.href = "login.html";
        return;
    }

    const page = window.location.pathname;

    // Employee trying to access admin/hr dashboard → redirect
    if (page.includes("dashboard") && isEmployee()) {
        window.location.href = "employee.html";
        return;
    }

    // Admin/HR trying to access employee page → redirect
    if (page.includes("employee") && !isEmployee()) {
        window.location.href = "dashboard.html";
        return;
    }
}

/* =========================
   🔧 COMMON HELPERS
========================= */

function authHeader() {
    return {
        "Authorization": "Bearer " + localStorage.getItem("token"),
        "Content-Type": "application/json"
    };
}

function getStatusColor(status) {
    if (status === "APPROVED") return "#22c55e";
    if (status === "REJECTED") return "#ef4444";
    return "#f59e0b"; // PENDING = orange
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
        endDate:   document.getElementById("endDate").value,
        reason:    document.getElementById("reason").value
    };

    if (!data.startDate || !data.endDate || !data.reason) {
        alert("Please fill all fields");
        return;
    }

    disableAllButtons();

    fetch(`${BASE_URL}/api/leaves/apply`, {
        method: "POST",
        headers: authHeader(),
        body: JSON.stringify(data)
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to apply leave");
        return res.json();
    })
    .then(() => {
        alert("Leave Applied ✅");
        loadMyLeaves();
    })
    .catch(err => {
        console.error(err);
        alert("Error applying leave ❌ " + err.message);
    })
    .finally(() => enableAllButtons());
}

function loadMyLeaves() {
    fetch(`${BASE_URL}/api/leaves/my`, {
        headers: authHeader()
    })
    .then(res => {
        if (!res.ok) throw new Error("Unauthorized or server error");
        return res.json();
    })
    .then(data => {
        const container = document.getElementById("leaveList");

        if (!data || data.length === 0) {
            container.innerHTML = "<p>No leaves found.</p>";
            return;
        }

        let html = "";
        data.forEach(l => {
            const color = getStatusColor(l.status);
            html += `
            <div class="card">
                <p><b>${l.leaveType}</b></p>
                <p>${l.startDate} → ${l.endDate}</p>
                <p>Status: <span style="color:${color}; font-weight:bold">${l.status}</span></p>
            </div>`;
        });

        container.innerHTML = html;
    })
    .catch(err => {
        console.error(err);
        alert("Failed to load leaves ❌ " + err.message);
    });
}

/* =========================
   🏢 ADMIN / HR MODULE
========================= */

function loadEmployees() {
    fetch(`${BASE_URL}/employees`, {
        headers: authHeader()
    })
    .then(res => {
        if (!res.ok) throw new Error("Unauthorized or server error");
        return res.json();
    })
    .then(data => {
        const container = document.getElementById("employees");

        if (!data || data.length === 0) {
            container.innerHTML = "<p>No employees found.</p>";
            return;
        }

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

        container.innerHTML = html;
    })
    .catch(err => {
        console.error(err);
        alert("Failed to load employees ❌ " + err.message);
    });
}

function loadLeaves() {
    fetch(`${BASE_URL}/api/leaves/all`, {
        headers: authHeader()
    })
    .then(res => {
        if (!res.ok) throw new Error("Unauthorized or server error");
        return res.json();
    })
    .then(data => {
        const container = document.getElementById("leaveRequests");

        if (!data || data.length === 0) {
            container.innerHTML = "<p>No leave requests found.</p>";
            return;
        }

        let html = "";

        data.forEach(l => {
            const color = getStatusColor(l.status);

            // Role-based approve/reject button logic
            let showButtons = false;

            if (l.status === "PENDING") {
                if (isAdmin()) {
                    // Admin can approve everyone
                    showButtons = true;
                } else if (isHR() && l.employeeRole === "EMPLOYEE") {
                    // HR can only approve EMPLOYEE leaves
                    showButtons = true;
                }
            }

            html += `
            <div class="card">
                <p><b>${l.employeeName}</b>
                   <span style="background:#334155; padding:2px 8px; border-radius:4px; font-size:12px">
                     ${l.employeeRole}
                   </span>
                </p>
                <p>${l.leaveType} &nbsp;|&nbsp; ${l.startDate} → ${l.endDate}</p>
                <p>Status: <span style="color:${color}; font-weight:bold">${l.status}</span></p>
                ${showButtons ? `
                    <button onclick="approveLeave(${l.id})">✅ Approve</button>
                    <button onclick="rejectLeave(${l.id})"
                            style="background: linear-gradient(135deg, #ef4444, #dc2626)">
                        ❌ Reject
                    </button>
                ` : ""}
            </div>`;
        });

        container.innerHTML = html;
    })
    .catch(err => {
        console.error(err);
        alert("Failed to load leaves ❌ " + err.message);
    });
}

/* =========================
   ✅ APPROVE / REJECT
========================= */

function approveLeave(id) {
    if (!confirm("Approve this leave?")) return;
    disableAllButtons();
    updateLeaveStatus(id, "APPROVED");
}

function rejectLeave(id) {
    if (!confirm("Reject this leave?")) return;
    disableAllButtons();
    updateLeaveStatus(id, "REJECTED");
}

function updateLeaveStatus(id, status) {
    fetch(`${BASE_URL}/api/leaves/${id}/status?status=${status}`, {
        method: "PUT",
        headers: authHeader()
    })
    .then(res => {
        if (!res.ok) throw new Error("Failed to update leave");
        return res.json();
    })
    .then(() => {
        alert(`Leave ${status} ✅`);
        loadLeaves();
    })
    .catch(err => {
        console.error(err);
        alert("Error updating leave ❌ " + err.message);
    })
    .finally(() => enableAllButtons());
}
/* =========================
   🏢 DASHBOARD INIT
   Shows/hides sections based on role
========================= */

function initDashboard() {
    const admin = isAdmin();
    const hr = isHR();

    // Update navbar title
    document.getElementById("dashTitle").textContent =
        admin ? "Admin Dashboard" : "HR Dashboard";

    // Add Employee card — visible to both HR and ADMIN
    document.getElementById("addEmployeeCard").style.display = (admin || hr) ? "block" : "none";

    // Add HR card — ADMIN only
    document.getElementById("addHrCard").style.display = admin ? "block" : "none";
}


/* =========================
   ➕ ADD EMPLOYEE / HR
========================= */

function addEmployee(role) {
    // Pick correct input IDs based on role
    const prefix = role === "HR" ? "hr" : "emp";

    const data = {
        name:       document.getElementById(`${prefix}Name`).value.trim(),
        email:      document.getElementById(`${prefix}Email`).value.trim(),
        department: document.getElementById(`${prefix}Department`).value.trim(),
        salary:     parseFloat(document.getElementById(`${prefix}Salary`).value),
        username:   document.getElementById(`${prefix}Username`).value.trim(),
        password:   document.getElementById(`${prefix}Password`).value.trim(),
        role:       role
    };

    // Basic validation
    if (!data.name || !data.email || !data.department || !data.salary || !data.username || !data.password) {
        alert("Please fill all fields");
        return;
    }

    disableAllButtons();

    fetch(`${BASE_URL}/employees`, {
        method: "POST",
        headers: authHeader(),
        body: JSON.stringify(data)
    })
    .then(res => {
        if (!res.ok) return res.text().then(err => { throw new Error(err) });
        return res.json();
    })
    .then(() => {
        alert(`${role} added successfully ✅`);
        clearForm(prefix);
        loadEmployees(); // refresh list
    })
    .catch(err => {
        console.error(err);
        alert("Failed to add " + role + " ❌ " + err.message);
    })
    .finally(() => enableAllButtons());
}

function clearForm(prefix) {
    ["Name", "Email", "Department", "Salary", "Username", "Password"].forEach(field => {
        const el = document.getElementById(`${prefix}${field}`);
        if (el) el.value = "";
    });
}


/* =========================
   ❌ REMOVE EMPLOYEE / HR
   HR → can remove EMPLOYEE only
   ADMIN → can remove both
========================= */

function removeEmployee(id, role) {
    const currentUserIsHR = isHR();
    const currentUserIsAdmin = isAdmin();

    // HR trying to remove HR — block it
    if (currentUserIsHR && role === "HR") {
        alert("HR cannot remove another HR ❌");
        return;
    }

    if (!confirm(`Remove this ${role}?`)) return;

    disableAllButtons();

    fetch(`${BASE_URL}/employees/${id}`, {
        method: "DELETE",
        headers: authHeader()
    })
    .then(res => {
        if (!res.ok) return res.text().then(err => { throw new Error(err) });
        return res.text();
    })
    .then(() => {
        alert("Removed successfully ✅");
        loadEmployees();
    })
    .catch(err => {
        console.error(err);
        alert("Failed to remove ❌ " + err.message);
    })
    .finally(() => enableAllButtons());
}


/* =========================
   👥 LOAD EMPLOYEES
   Override app.js loadEmployees with role-aware version
========================= */

function loadEmployees() {
    fetch(`${BASE_URL}/employees`, {
        headers: authHeader()
    })
    .then(res => {
        if (!res.ok) throw new Error("Unauthorized or server error");
        return res.json();
    })
    .then(data => {
        const container = document.getElementById("employees");

        if (!data || data.length === 0) {
            container.innerHTML = "<p>No employees found.</p>";
            return;
        }

        const admin = isAdmin();
        const hr = isHR();

        let html = "";

        data.forEach(emp => {
            const empRole = emp.user?.role || "EMPLOYEE";

            // Decide if remove button should show
            let showRemove = false;
            if (admin) showRemove = true; // admin can remove all
            if (hr && empRole === "EMPLOYEE") showRemove = true; // HR can remove EMPLOYEE only

            html += `
            <div class="card">
                <p><b>${emp.name}</b>
                   <span style="background:#334155; padding:2px 8px; border-radius:4px; font-size:12px; margin-left:8px">
                     ${empRole}
                   </span>
                </p>
                <p>📧 ${emp.email}</p>
                <p>🏢 ${emp.department}</p>
                <p>💰 ₹${emp.salary}</p>
                ${showRemove ? `
                    <button onclick="removeEmployee(${emp.id}, '${empRole}')"
                            style="background: linear-gradient(135deg, #ef4444, #dc2626)">
                        ❌ Remove
                    </button>
                ` : ""}
            </div>`;
        });

        container.innerHTML = html;
    })
    .catch(err => {
        console.error(err);
        alert("Failed to load employees ❌ " + err.message);
    });
}
/* =========================
   👤 MY PROFILE
========================= */

function loadMyProfile() {
    fetch(`${BASE_URL}/employees/me`, {
        headers: authHeader()
    })
    .then(res => {
        if (!res.ok) throw new Error("Could not load profile");
        return res.json();
    })
    .then(emp => {
        const container = document.getElementById("myProfile");

        container.innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 10px;">
            <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 8px;">
                <div style="
                    width: 52px; height: 52px;
                    background: linear-gradient(135deg, #38bdf8, #818cf8);
                    border-radius: 50%;
                    display: flex; align-items: center; justify-content: center;
                    font-family: 'Syne', sans-serif;
                    font-size: 22px; font-weight: 800;
                    color: #080c14;
                ">
                    ${emp.name ? emp.name.charAt(0).toUpperCase() : '?'}
                </div>
                <div>
                    <div style="font-size: 18px; font-weight: 600; color: #e2e8f0">${emp.name}</div>
                    <div style="font-size: 13px; color: #64748b">${emp.email}</div>
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px;">
                <div style="background: rgba(56,189,248,0.05); border: 1px solid rgba(56,189,248,0.1); border-radius: 10px; padding: 14px;">
                    <div style="font-size: 11px; color: #64748b; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px;">Department</div>
                    <div style="font-size: 15px; font-weight: 500; color: #e2e8f0">${emp.department || '—'}</div>
                </div>
                <div style="background: rgba(56,189,248,0.05); border: 1px solid rgba(56,189,248,0.1); border-radius: 10px; padding: 14px;">
                    <div style="font-size: 11px; color: #64748b; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px;">Salary</div>
                    <div style="font-size: 15px; font-weight: 500; color: #34d399">₹${emp.salary?.toLocaleString('en-IN') || '—'}</div>
                </div>
                <div style="background: rgba(56,189,248,0.05); border: 1px solid rgba(56,189,248,0.1); border-radius: 10px; padding: 14px;">
                    <div style="font-size: 11px; color: #64748b; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px;">Role</div>
                    <div style="font-size: 15px; font-weight: 500; color: #818cf8">${emp.user?.role || 'EMPLOYEE'}</div>
                </div>
                <div style="background: rgba(56,189,248,0.05); border: 1px solid rgba(56,189,248,0.1); border-radius: 10px; padding: 14px;">
                    <div style="font-size: 11px; color: #64748b; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 4px;">Company</div>
                    <div style="font-size: 15px; font-weight: 500; color: #e2e8f0">${emp.company?.name || '—'}</div>
                </div>
            </div>
        </div>`;
    })
    .catch(err => {
        console.error(err);
        document.getElementById("myProfile").innerHTML =
            `<p class="empty-state">Could not load profile ❌</p>`;
    });
}
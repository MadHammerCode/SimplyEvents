document.addEventListener("DOMContentLoaded", function() {
    // Load role-utils script if not already loaded
    if (!window.getCurrentUserRole) {
        const script = document.createElement('script');
        script.src = '/js/role-utils.js';
        script.onload = () => {
            setupAdminActions();
            loadUsers();
            loadStats();
        };
        script.onerror = () => {
            console.error('Failed to load role-utils.js');
            window.location.href = '/login';
        };
        document.head.appendChild(script);
    } else {
        setupAdminActions();
        loadUsers();
        loadStats();
    }
});

function setupAdminActions() {
    const actionsToggle = document.getElementById("actionsToggle");
    const actionsMenu = document.getElementById("actionsMenu");
    const goToDashboard = document.getElementById("goToDashboard");
    const goToBackoffice = document.getElementById("goToBackoffice");
    const goToFrontoffice = document.getElementById("goToFrontoffice");
    const logoutBtn = document.getElementById("logoutBtn");

    // Toggle menu
    if (actionsToggle) {
        actionsToggle.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();
            const isOpen = !actionsMenu.classList.contains("hidden");
            if (isOpen) {
                actionsMenu.classList.add("hidden");
                actionsToggle.setAttribute("aria-expanded", "false");
            } else {
                actionsMenu.classList.remove("hidden");
                actionsToggle.setAttribute("aria-expanded", "true");
            }
        });
    }

    // Close on outside click
    document.addEventListener("click", (e) => {
        if (actionsMenu && !actionsMenu.classList.contains("hidden")) {
            const isInside = actionsToggle?.contains(e.target) || actionsMenu?.contains(e.target);
            if (!isInside) {
                actionsMenu.classList.add("hidden");
                if (actionsToggle) actionsToggle.setAttribute("aria-expanded", "false");
            }
        }
    });

    // Navigate
    if (goToDashboard) {
        goToDashboard.addEventListener("click", () => {
            window.location.href = "/dashboard";
        });
    }

    if (goToBackoffice) {
        goToBackoffice.addEventListener("click", () => {
            window.location.href = "/backoffice-dashboard";
        });
    }

    if (goToFrontoffice) {
        goToFrontoffice.addEventListener("click", () => {
            window.location.href = "/frontoffice-checkin";
        });
    }
    // Logout
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            handleLogout();
        });
    }
}

function loadStats() {
    fetch("/api/admin/stats")
        .then(res => {
            if (!res.ok) throw new Error("Could not load stats.");
            return res.json();
        })
        .then(stats => {
            renderStats(stats);
        })
        .catch(err => {
            console.error("Stats error:", err.message);
        });
}

function renderStats(stats) {
    document.getElementById("statTotalUsers").textContent = stats.totalUsers || "0";
    document.getElementById("statCustomers").textContent = stats.customers || "0";
    document.getElementById("statVendors").textContent = stats.vendors || "0";
    document.getElementById("statAdmins").textContent = stats.admins || "0";
}

function loadUsers() {
    const tbody = document.getElementById("userTableBody");
    tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">Loading...</td></tr>';

    fetch("/api/admin/users")
        .then(res => {
            if (res.status === 403) throw new Error("Access Denied: You are not an Admin.");
            if (!res.ok) throw new Error("Could not load users.");
            return res.json();
        })
        .then(users => {
            renderTable(users);
        })
        .catch(err => {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:red;">${err.message}</td></tr>`;
        });
}

function renderTable(users) {
    const tbody = document.getElementById("userTableBody");
    tbody.innerHTML = "";

    if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No users found.</td></tr>';
        return;
    }

    users.forEach(user => {
        const tr = document.createElement("tr");

        let badgeClass = "badge";
        if (user.role === "ADMIN") badgeClass = "badge badge--admin";
        if (user.role === "BACKOFFICE") badgeClass = "badge badge--backoffice";
        if (user.role === "FRONTOFFICE") badgeClass = "badge badge--frontoffice";

        const isDisabled = user.isCurrentUser ? "disabled" : "";

        tr.innerHTML = `
            <td>#${user.id}</td>
            <td>${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td><span class="${badgeClass}">${user.role}</span></td>
            <td>
                <select id="role-select-${user.id}" class="filter-group-select" ${isDisabled}>
                    <option value="CUSTOMER" ${user.role === 'CUSTOMER' ? 'selected' : ''}>Customer</option>
                    <option value="FRONTOFFICE" ${user.role === 'FRONTOFFICE' ? 'selected' : ''}>Frontoffice</option>
                    <option value="BACKOFFICE" ${user.role === 'BACKOFFICE' ? 'selected' : ''}>Backoffice</option>
                    <option value="ADMIN" ${user.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                </select>
            </td>
            <td>
                ${user.isCurrentUser
            ? '<span style="color:#999; font-size:0.85rem; font-style:italic;">(You)</span>'
            : `<button class="action-btn action-btn--edit" onclick="promoteUser(${user.id})">Save</button>`
        }
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function promoteUser(userId) {
    const select = document.getElementById(`role-select-${userId}`);
    const newRole = select.value;

    select.disabled = true;

    fetch(`/api/admin/promote?userId=${userId}&role=${newRole}`, {
        method: "POST"
    })
        .then(async res => {
            if (!res.ok) throw new Error("Update failed");

            showStatus("User updated successfully!", "success");
            loadUsers();
        })
        .catch(err => {
            showStatus(err.message, "error");
            select.disabled = false;
        });
}

function escapeHtml(text) {
    if (!text) return "";
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function showStatus(msg, type) {
    const el = document.getElementById("statusBox");
    el.textContent = msg;
    el.className = "status-banner"; // reset
    el.classList.add(type === "error" ? "status-banner--error" : "status-banner--success");
    el.classList.remove("hidden");

    setTimeout(() => {
        el.classList.add("hidden");
    }, 3000);
}
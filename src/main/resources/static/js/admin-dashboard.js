document.addEventListener("DOMContentLoaded", function() {
    loadUsers();
});

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

        let badgeClass = "role-badge--user";
        if (user.role === "ADMIN") badgeClass = "role-badge--admin";
        if (user.role === "BACKOFFICE") badgeClass = "role-badge--backoffice";
        if (user.role === "FRONTOFFICE") badgeClass = "role-badge--frontoffice";

        const isDisabled = user.isCurrentUser ? "disabled" : "";

        tr.innerHTML = `
            <td>#${user.id}</td>
            <td>${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)}</td>
            <td>${escapeHtml(user.email)}</td>
            <td><span class="role-badge ${badgeClass}">${user.role}</span></td>
            <td>
                <select id="role-select-${user.id}" class="role-select" ${isDisabled}>
                    <option value="CUSTOMER" ${user.role === 'CUSTOMER' ? 'selected' : ''}>Customer</option>
                    <option value="FRONTOFFICE" ${user.role === 'FRONTOFFICE' ? 'selected' : ''}>Frontoffice</option>
                    <option value="BACKOFFICE" ${user.role === 'BACKOFFICE' ? 'selected' : ''}>Backoffice</option>
                    <option value="ADMIN" ${user.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                </select>
            </td>
            <td>
                ${user.isCurrentUser
            ? '<span style="color:#999; font-size:0.85rem; font-style:italic;">(You)</span>'
            : `<button class="action-btn" onclick="promoteUser(${user.id})">Save</button>`
        }
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function promoteUser(userId) {
    const select = document.getElementById(`role-select-${userId}`);
    const newRole = select.value;
    const statusBox = document.getElementById("statusBox");

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
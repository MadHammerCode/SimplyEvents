let userData = null;

/* ------ Helper ------ */

function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function showError(boxId, msg) {
    const box = document.getElementById(boxId);
    if (!box) return;
    if (!msg) {
        box.classList.add("hidden");
        box.innerHTML = "";
        return;
    }
    box.classList.remove("hidden");
    box.innerHTML = Array.isArray(msg)
        ? msg.map((m) => `<div>${escapeHtml(m)}</div>`).join("")
        : escapeHtml(msg);
}

function showSuccess(boxId, msg) {
    const box = document.getElementById(boxId);
    if (!box) return;
    box.classList.remove("hidden");
    box.innerHTML = escapeHtml(msg);
    setTimeout(() => {
        box.classList.add("hidden");
    }, 3500);
}

/* ------ Load user data ------ */

function loadProfile() {
    fetch("/api/users/me", { credentials: 'same-origin' })
        .then((res) => {
            if (!res.ok) throw new Error("Error loading profile");
            return res.json();
        })
        .then((data) => {
            userData = data;

            document.getElementById("profileName").textContent =
                `${data.firstName || ""} ${data.lastName || ""}`.trim() || "Profil";

            document.getElementById("profileEmail").textContent = data.email || "-";
            document.getElementById("profileRole").textContent = data.role || "User";

            // Generate avatar initials
            const firstName = data.firstName || "";
            const lastName = data.lastName || "";
            const initials = (firstName.charAt(0) + lastName.charAt(0)).toUpperCase();

            const avatarImg = document.getElementById("avatarImg");
            const avatarWrapper = document.getElementById("avatarImg").parentElement;

            if (initials.length === 2 && initials !== "  ") {
                // Hide image and show initials
                avatarImg.style.display = "none";
                let initialsSpan = avatarWrapper.querySelector(".avatar-initials");
                if (!initialsSpan) {
                    initialsSpan = document.createElement("span");
                    initialsSpan.className = "avatar-initials";
                    avatarWrapper.appendChild(initialsSpan);
                }
                initialsSpan.textContent = initials;
            } else {
                // Show default image
                avatarImg.style.display = "block";
                const initialsSpan = avatarWrapper.querySelector(".avatar-initials");
                if (initialsSpan) initialsSpan.remove();
            }

            document.getElementById("firstName").value = data.firstName || "";
            document.getElementById("lastName").value = data.lastName || "";
            document.getElementById("email").value = data.email || "";
            document.getElementById("phone").value = data.phone || "";
            document.getElementById("address").value = data.address || "";
        })
        .catch((err) => {
            console.error(err);
            showError("errorBox", "Profile could not be loaded.");
        });
}

/* ------ Save profile ------ */

function saveProfile() {
    const errors = [];
    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const email = document.getElementById("email").value.trim();

    if (!firstName) errors.push("First name is required.");
    if (!lastName) errors.push("Last name is required.");
    if (!email) errors.push("E-Mail is required.");

    if (errors.length) {
        showError("errorBox", errors);
        return;
    }

    showError("errorBox", null);

    const body = {
        firstName,
        lastName,
        email,
        phone: document.getElementById("phone").value.trim(),
        address: document.getElementById("address").value.trim()
    };

    fetch("/api/users/me", {
        method: "PUT",
        credentials: 'same-origin',
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    })
        .then((res) => {
            if (!res.ok) throw new Error("Profile could not be saved.");
            return res.json().catch(() => ({}));
        })
        .then(() => {
            showSuccess("passwordSuccessBox", "Profile has been updated.");
            loadProfile();
        })
        .catch(() => {
            showError("errorBox", "Profile could not be saved.");
        });
}

/* ------ Change password ------ */

function changePassword() {
    const current = document.getElementById("currentPassword").value.trim();
    const newPass = document.getElementById("newPassword").value.trim();
    const confirm = document.getElementById("confirmPassword").value.trim();

    const errors = [];
    if (!current) errors.push("Current password is required.");
    if (!newPass) errors.push("New password required.");
    if (newPass !== confirm) errors.push("Passwords do not match.");

    if (errors.length) {
        showError("passwordErrorBox", errors);
        return;
    }

    showError("passwordErrorBox", null);

    fetch("/api/users/me/password", {
        method: "PUT",
        credentials: 'same-origin',
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            currentPassword: current,
            newPassword: newPass
        })
    })
        .then((res) => {
            if (!res.ok) throw new Error("Password change failed");
            return res.json().catch(() => ({}));
        })
        .then(() => {
            showSuccess("passwordSuccessBox", "Password changed successfully.");
            document.getElementById("currentPassword").value = "";
            document.getElementById("newPassword").value = "";
            document.getElementById("confirmPassword").value = "";
        })
        .catch(() => {
            showError("passwordErrorBox", "Password could not be changed.");
        });
}

/* ------ Navigation ------ */

function setupNavigation() {
    const btnDash = document.getElementById("btnToDashboard");
    const btnBookings = document.getElementById("btnToBookings");
    const btnLogout = document.getElementById("btnLogout");

    if (btnDash) btnDash.addEventListener("click", () => (window.location.href = "/dashboard"));
    if (btnBookings)
        btnBookings.addEventListener("click", () => (window.location.href = "/my-bookings"));
    if (btnLogout) {
        btnLogout.addEventListener("click", () => {
            localStorage.removeItem("simplyevents_currentUser");
            window.location.href = "/login";
        });
    }
}

/* ------ Init ------ */

document.addEventListener("DOMContentLoaded", () => {
    setupNavigation();
    loadProfile();

    document
        .getElementById("btnSaveProfile")
        .addEventListener("click", saveProfile);

    document
        .getElementById("btnChangePassword")
        .addEventListener("click", changePassword);
});
document.addEventListener("DOMContentLoaded", () => {
    setupNavbarLogic();
});

function setupNavbarLogic() {
    const navLinks = document.getElementById("navLinks");
    const navToggle = document.getElementById("navToggle");
    const logoutBtn = document.getElementById("logoutBtn");

    // Mobile Menu Toggle
    if (navToggle && navLinks) {
        navToggle.addEventListener("click", () => {
            navLinks.classList.toggle("nav-open");
        });
    }

    // Read users from localStorage (you can link to backend later)
    let user = null;
    try {
        user = JSON.parse(localStorage.getItem("simplyevents_currentUser"));
    } catch (_) {}

    const guestEls = document.querySelectorAll(".nav-guest-only");
    const authEls = document.querySelectorAll(".nav-auth-only");
    const backofficeEls = document.querySelectorAll(".nav-role-backoffice");
    const frontofficeEls = document.querySelectorAll(".nav-role-frontoffice");

    // Hide everyone first
    guestEls.forEach(el => el.classList.add("hidden"));
    authEls.forEach(el => el.classList.add("hidden"));
    backofficeEls.forEach(el => el.classList.add("hidden"));
    frontofficeEls.forEach(el => el.classList.add("hidden"));

    // No user → only guest links
    if (!user) {
        guestEls.forEach(el => el.classList.remove("hidden"));
    } else {
        // Logged In → Auth-Links
        authEls.forEach(el => el.classList.remove("hidden"));

        if (user.role === "BACKOFFICE") {
            backofficeEls.forEach(el => el.classList.remove("hidden"));
        }
        if (user.role === "FRONTOFFICE") {
            frontofficeEls.forEach(el => el.classList.remove("hidden"));
        }
    }

    // Logout
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            try {
                localStorage.removeItem("simplyevents_currentUser");
            } catch (_) {}
            window.location.href = "/login";
        });
    }
}
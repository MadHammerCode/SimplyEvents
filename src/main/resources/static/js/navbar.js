document.addEventListener("DOMContentLoaded", () => {
    setupNavbarLogic();
});

function setupNavbarLogic() {
    const navLinks = document.getElementById("navLinks");
    const navToggle = document.getElementById("navToggle");
    const logoutBtn = document.getElementById("logoutBtn");
    const navUserName = document.getElementById("navUserName");

    const userMenuWrapper = document.getElementById("userMenuWrapper");
    const navUserBtn = document.getElementById("navUserBtn");
    const userMenu = document.getElementById("userMenu");

    const menuCustomerEls = document.querySelectorAll(".nav-menu-customer");
    const menuBackofficeEls = document.querySelectorAll(".nav-menu-backoffice");
    const menuFrontofficeEls = document.querySelectorAll(".nav-menu-frontoffice");

    // Mobile Menu Toggle
    if (navToggle && navLinks) {
        navToggle.addEventListener("click", () => {
            navLinks.classList.toggle("nav-open");
        });
    }

    // Read user from localStorage (you can link to backend later)
    let user = null;
    try {
        const raw = localStorage.getItem("simplyevents_currentUser");
        if (raw) {
            user = JSON.parse(raw);
            // If the backend/client stored JSON *as a string* (double-stringified), parse again
            if (typeof user === "string") {
                user = JSON.parse(user);
            }
        }
    } catch (_) {
        user = null;
    }

    const guestEls = document.querySelectorAll(".nav-guest-only");
    const authEls = document.querySelectorAll(".nav-auth-only");

    // Hide everyone first
    guestEls.forEach(el => el.classList.add("hidden"));
    authEls.forEach(el => el.classList.add("hidden"));

    if (userMenuWrapper) userMenuWrapper.classList.add("hidden");
    if (userMenu) userMenu.classList.add("hidden");
    if (navUserBtn) navUserBtn.setAttribute("aria-expanded", "false");

    // Hide all dropdown role items
    menuCustomerEls.forEach(el => el.classList.add("hidden"));
    menuBackofficeEls.forEach(el => el.classList.add("hidden"));
    menuFrontofficeEls.forEach(el => el.classList.add("hidden"));

    if (navUserName) {
        navUserName.textContent = "";
    }

    // No user → only guest links
    if (!user) {
        guestEls.forEach(el => el.classList.remove("hidden"));
    } else {
        // Logged In → Auth-Links
        authEls.forEach(el => el.classList.remove("hidden"));

        const niceName = (
            [user.firstName || user.fname, user.lastName || user.lname]
                .filter(Boolean)
                .join(" ")
        ).trim();

        const fallback = user.email || user.username || user.login || "";
        if (navUserName) {
            navUserName.textContent = niceName || fallback;
            navUserName.classList.toggle("hidden", !(niceName || fallback));
        }

        // Show the user dropdown wrapper
        if (userMenuWrapper) userMenuWrapper.classList.remove("hidden");

        // Role-based dropdown items (robust: supports user.role and/or user.roles[])
        const role = String(user.role || "").toUpperCase();
        const roles = Array.isArray(user.roles)
            ? user.roles.map(r => String(r).toUpperCase())
            : [];

        const isCustomer = role === "CUSTOMER" || role === "USER" || roles.includes("ROLE_USER") || roles.includes("USER");
        const isBackoffice = role === "BACKOFFICE" || role === "VENDOR" || roles.includes("ROLE_VENDOR") || roles.includes("VENDOR");
        const isFrontoffice = role === "FRONTOFFICE" || roles.includes("ROLE_FRONTOFFICE") || roles.includes("FRONTOFFICE");

        if (isCustomer) {
            // Customer: Profile + My Bookings + Logout
            menuCustomerEls.forEach(el => el.classList.remove("hidden"));
        }

        if (isBackoffice) {
            // Backoffice: Backoffice + Check in + Logout
            menuBackofficeEls.forEach(el => el.classList.remove("hidden"));
        }

        if (isFrontoffice) {
            // Frontoffice: Check in + Logout
            menuFrontofficeEls.forEach(el => el.classList.remove("hidden"));
        }
    }

    // Dropdown open/close
    function closeUserMenu() {
        if (userMenu) userMenu.classList.add("hidden");
        if (navUserBtn) navUserBtn.setAttribute("aria-expanded", "false");
    }

    function toggleUserMenu() {
        if (!userMenu) return;
        const isOpen = !userMenu.classList.contains("hidden");
        if (isOpen) {
            closeUserMenu();
        } else {
            userMenu.classList.remove("hidden");
            if (navUserBtn) navUserBtn.setAttribute("aria-expanded", "true");
        }
    }

    if (navUserBtn) {
        navUserBtn.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();
            toggleUserMenu();
        });
    }

    // Close on outside click
    document.addEventListener("click", (e) => {
        if (!userMenu || userMenu.classList.contains("hidden")) return;
        const target = e.target;
        const clickedInside = (userMenuWrapper && userMenuWrapper.contains(target));
        if (!clickedInside) closeUserMenu();
    });

    // Close on ESC
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") closeUserMenu();
    });

    // When clicking a menu link, close dropdown (also closes mobile nav)
    if (userMenu) {
        userMenu.addEventListener("click", (e) => {
            const a = e.target && e.target.closest ? e.target.closest("a") : null;
            if (a) {
                closeUserMenu();
                if (navLinks) navLinks.classList.remove("nav-open");
            }
        });
    }

    // Logout
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            try {
                localStorage.removeItem("simplyevents_currentUser");
            } catch (_) {}
            if (navLinks) navLinks.classList.remove("nav-open");
            closeUserMenu();
            window.location.href = "/dashboard";
        });
    }
}


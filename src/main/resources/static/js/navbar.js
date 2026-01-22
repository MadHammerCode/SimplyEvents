document.addEventListener("DOMContentLoaded", () => {
    // Load role-utils.js if not already loaded
    if (!window.getCurrentUserRole) {
        const script = document.createElement('script');
        script.src = '/js/role-utils.js';
        script.onload = () => {
            setupNavbarLogic();
        };
        script.onerror = () => {
            console.error('Failed to load role-utils.js');
            setupNavbarLogic(); // Fallback to localStorage-based logic
        };
        document.head.appendChild(script);
    } else {
        setupNavbarLogic();
    }
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
    const menuAdminEls = document.querySelectorAll(".nav-menu-admin");

    // Mobile Menu Toggle
    if (navToggle && navLinks) {
        navToggle.addEventListener("click", () => {
            navLinks.classList.toggle("nav-open");
        });
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
    menuAdminEls.forEach(el => el.classList.add("hidden"));

    if (navUserName) {
        navUserName.textContent = "";
    }

    // Try to fetch user from API if role-utils is available
    if (window.getCurrentUserRole) {
        getCurrentUserRole().then((userRole) => {
            if (!userRole) {
                // Not authenticated - show guest links only
                guestEls.forEach(el => el.classList.remove("hidden"));
                return;
            }

            // Authenticated - fetch user profile
            fetch('/api/users/me')
                .then(res => res.ok ? res.json() : null)
                .then(userProfile => {
                    setupAuthenticatedNav(userProfile, userRole);
                })
                .catch(() => {
                    // Fallback to just role if profile fetch fails
                    setupAuthenticatedNav({ role: userRole }, userRole);
                });
        }).catch(() => {
            // Fallback to localStorage
            setupNavbarWithLocalStorage();
        });
    } else {
        // Fallback to localStorage if role-utils not loaded
        setupNavbarWithLocalStorage();
    }

    function setupAuthenticatedNav(userProfile, userRole) {
        authEls.forEach(el => el.classList.remove("hidden"));

        const niceName = (
            [userProfile?.firstName || userProfile?.fname, userProfile?.lastName || userProfile?.lname]
                .filter(Boolean)
                .join(" ")
        ).trim();

        const fallback = userProfile?.email || userProfile?.username || userProfile?.login || "";
        if (navUserName) {
            navUserName.textContent = niceName || fallback;
            navUserName.classList.toggle("hidden", !(niceName || fallback));
        }

        // Show the user dropdown wrapper
        if (userMenuWrapper) userMenuWrapper.classList.remove("hidden");

        // Show role-based menu items
        const role = String(userRole || "").toUpperCase();

        if (role === "CUSTOMER") {
            menuCustomerEls.forEach(el => el.classList.remove("hidden"));
        } else if (role === "BACKOFFICE") {
            menuBackofficeEls.forEach(el => el.classList.remove("hidden"));
        } else if (role === "FRONTOFFICE") {
            menuFrontofficeEls.forEach(el => el.classList.remove("hidden"));
        } else if (role === "ADMIN") {
            menuAdminEls.forEach(el => el.classList.remove("hidden"));
        }

        setupMenuHandlers();
    }

    function setupNavbarWithLocalStorage() {
        let user = null;
        try {
            const raw = localStorage.getItem("simplyevents_currentUser");
            if (raw) {
                user = JSON.parse(raw);
                if (typeof user === "string") {
                    user = JSON.parse(user);
                }
            }
        } catch (_) {
            user = null;
        }

        if (!user) {
            guestEls.forEach(el => el.classList.remove("hidden"));
        } else {
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

            if (userMenuWrapper) userMenuWrapper.classList.remove("hidden");

            const role = String(user.role || "").toUpperCase();
            const roles = Array.isArray(user.roles)
                ? user.roles.map(r => String(r).toUpperCase())
                : [];

            const isCustomer = role === "CUSTOMER" || role === "USER" || roles.includes("ROLE_USER") || roles.includes("USER");
            const isBackoffice = role === "BACKOFFICE" || role === "VENDOR" || roles.includes("ROLE_VENDOR") || roles.includes("VENDOR");
            const isFrontoffice = role === "FRONTOFFICE" || roles.includes("ROLE_FRONTOFFICE") || roles.includes("FRONTOFFICE");
            const isAdmin = role === "ADMIN" || roles.includes("ROLE_ADMIN") || roles.includes("ADMIN");

            if (isCustomer) menuCustomerEls.forEach(el => el.classList.remove("hidden"));
            if (isBackoffice) menuBackofficeEls.forEach(el => el.classList.remove("hidden"));
            if (isFrontoffice) menuFrontofficeEls.forEach(el => el.classList.remove("hidden"));
            if (isAdmin) menuAdminEls.forEach(el => el.classList.remove("hidden"));

            setupMenuHandlers();
        }
    }

    function setupMenuHandlers() {
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

        // When clicking a menu link, close dropdown
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
                if (window.handleLogout) {
                    handleLogout();
                } else {
                    try {
                        localStorage.removeItem("simplyevents_currentUser");
                    } catch (_) {}
                    if (navLinks) navLinks.classList.remove("nav-open");
                    closeUserMenu();
                    window.location.href = "/login";
                }
            });
        }
    }
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


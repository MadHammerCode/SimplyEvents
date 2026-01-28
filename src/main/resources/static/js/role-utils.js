const ROLES = {
    ADMIN: 'ADMIN',
    BACKOFFICE: 'BACKOFFICE',
    FRONTOFFICE: 'FRONTOFFICE',
    CUSTOMER: 'CUSTOMER'
};

// Cache key for localStorage
const ROLE_CACHE_KEY = 'simplyevents_currentUserRole';
const ROLE_CACHE_TTL = 5 * 60 * 1000; // 5 minutes

let cachedRole = null;
let roleCacheTime = 0;


async function fetchUserRole() {
    try {
        const response = await fetch('/api/users/me');
        if (!response.ok) {
            console.warn('Failed to fetch user profile');
            return null;
        }
        const data = await response.json();
        return data.role || null;
    } catch (error) {
        console.error('Error fetching user role:', error);
        return null;
    }
}


async function getCurrentUserRole() {
    const now = Date.now();

    // Return cached role if still valid
    if (cachedRole && (now - roleCacheTime) < ROLE_CACHE_TTL) {
        return cachedRole;
    }

    const role = await fetchUserRole();
    if (role) {
        cachedRole = role;
        roleCacheTime = now;
    }
    return role;
}


function clearRoleCache() {
    cachedRole = null;
    roleCacheTime = 0;
}


async function hasRole(role) {
    const currentRole = await getCurrentUserRole();
    return currentRole === role;
}


async function hasAnyRole(roles) {
    const currentRole = await getCurrentUserRole();
    return roles.includes(currentRole);
}


function filterNavigationByRole(userRole) {
    // Hide all role-specific menu items
    document.querySelectorAll('[data-role-customer], [data-role-backoffice], [data-role-frontoffice], [data-role-admin]').forEach(el => {
        el.classList.add('hidden');
    });

    // Show items matching the user's role
    const roleKey = `data-role-${userRole.toLowerCase()}`;
    document.querySelectorAll(`[${roleKey}]`).forEach(el => {
        el.classList.remove('hidden');
    });

    // Special case: ADMIN sees all menu items (except those explicitly hidden)
    if (userRole === ROLES.ADMIN) {
        document.querySelectorAll('[data-role-admin]').forEach(el => {
            el.classList.remove('hidden');
        });
    }
}


function setupRoleBasedNavigation() {
    // For backoffice-dashboard, admin-dashboard, frontoffice-checkin
    const actionsToggle = document.getElementById('actionsToggle');
    if (!actionsToggle) return;

    actionsToggle.addEventListener('click', async () => {
        const menu = document.getElementById('actionsMenu');
        if (menu) {
            menu.classList.toggle('hidden');
        }
    });

    // Hide menu when clicking outside
    document.addEventListener('click', (e) => {
        const toggle = document.getElementById('actionsToggle');
        const menu = document.getElementById('actionsMenu');
        if (toggle && menu && !toggle.contains(e.target) && !menu.contains(e.target)) {
            menu.classList.add('hidden');
        }
    });
}


function handleLogout() {
    clearRoleCache();
    try {
        localStorage.removeItem('simplyevents_currentUser');
    } catch (_) {}
    // Use Spring Security logout endpoint, then redirect to dashboard
    window.location.href = '/logout';
}


function redirectUnauthorized() {
    window.location.href = '/dashboard';
}

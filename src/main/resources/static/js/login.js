const STORAGE_USER_KEY = "simplyevents_currentUser";

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function showError(messageOrList) {
    const box = document.getElementById("errorBox");
    const successBox = document.getElementById("successBox");
    if (!box) {
        console.error("Error box not found in DOM");
        return;
    }

    if (!messageOrList) {
        box.classList.add("hidden");
        box.innerHTML = "";
        return;
    }

    // Ensure box is visible
    if (successBox) {
        successBox.classList.add("hidden");
    }
    box.classList.remove("hidden");

    // Set the error message
    if (Array.isArray(messageOrList)) {
        const items = messageOrList.map((m) => `<li>${escapeHtml(m)}</li>`).join("");
        box.innerHTML = `<strong>Fehler:</strong><ul>${items}</ul>`;
    } else {
        box.innerHTML = `<strong>Fehler:</strong> ${escapeHtml(messageOrList)}`;
    }

    console.log("Error displayed:", messageOrList);
}

function showSuccess(messageOrList) {
    const box = document.getElementById("successBox");
    const errorBox = document.getElementById("errorBox");
    if (!box) return;

    if (!messageOrList) {
        box.classList.add("hidden");
        box.innerHTML = "";
        return;
    }

    if (errorBox) errorBox.classList.add("hidden");
    box.classList.remove("hidden");

    if (Array.isArray(messageOrList)) {
        const items = messageOrList.map((m) => `<li>${escapeHtml(m)}</li>`).join("");
        box.innerHTML = `<strong>Success:</strong><ul>${items}</ul>`;
    } else {
        box.innerHTML = escapeHtml(messageOrList);
    }
}

function switchToLogin() {
    const tabLogin = document.getElementById("tabLogin");
    const tabRegister = document.getElementById("tabRegister");
    const formLogin = document.getElementById("loginForm");
    const formRegister = document.getElementById("registerForm");

    if (!tabLogin || !tabRegister || !formLogin || !formRegister) return;

    tabLogin.classList.add("auth__tab--active");
    tabRegister.classList.remove("auth__tab--active");
    formLogin.classList.remove("hidden");
    formRegister.classList.add("hidden");
    showError(null);
    showSuccess(null);
}

function switchToRegister() {
    const tabLogin = document.getElementById("tabLogin");
    const tabRegister = document.getElementById("tabRegister");
    const formLogin = document.getElementById("loginForm");
    const formRegister = document.getElementById("registerForm");

    if (!tabLogin || !tabRegister || !formLogin || !formRegister) return;

    tabLogin.classList.remove("auth__tab--active");
    tabRegister.classList.add("auth__tab--active");
    formLogin.classList.add("hidden");
    formRegister.classList.remove("hidden");
    showError(null);
    showSuccess(null);
}

/* ---------- Login ---------- */

function validateLoginForm() {
    const emailEl = document.getElementById("loginEmail");
    const pwEl = document.getElementById("loginPassword");

    const email = emailEl?.value.trim();
    const pw = pwEl?.value;


    if (!email || !pw) {
        return null;
    }

    return { email, password: pw };
}

function doLogin(payload) {
    const remember = document.getElementById("rememberMe")?.checked;

    return fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: 'same-origin',
        body: JSON.stringify(payload)
    }).then(async (res) => {
        if (!res.ok) {
            let errorMessage = "Login failed. Please check your entries.";

            try {
                const body = await res.json();
                if (typeof body === "string" && body.trim()) {
                    errorMessage = body;
                } else if (body && typeof body === "object") {
                    errorMessage = JSON.stringify(body);
                }
            } catch {
                try {
                    errorMessage = await res.text();
                    if (!errorMessage.trim()) {
                        errorMessage = "Login failed. Please check your entries.";
                    }
                } catch {
                    //Empty
                }
            }

            console.error("Login error:", errorMessage);
            showError(errorMessage);
            throw new Error("Login failed");
        }

        showError(null);
        const user = await res.json().catch(() => ({}));


        if (user) {
            try {
                if (typeof user === "string") {
                    // If it's already JSON text, store as-is
                    window.localStorage.setItem(STORAGE_USER_KEY, user);
                } else {
                    window.localStorage.setItem(STORAGE_USER_KEY, JSON.stringify(user));
                }
            } catch {
                // Empty
            }
        }

        return user;
    });
}

/* ---------- Register ---------- */

function validateRegisterForm() {

    const firstNameEl = document.getElementById("registerFirstName");
    const lastNameEl = document.getElementById("registerLastName");

    const emailEl = document.getElementById("registerEmail");
    const pwEl = document.getElementById("registerPassword");
    const pwRepeatEl = document.getElementById("registerPasswordRepeat");
    const addressEl = document.getElementById("registerAddress");

    const errors = [];


    const firstName = firstNameEl?.value.trim();
    const lastName = lastNameEl?.value.trim();

    const email = emailEl?.value.trim();
    const pw = pwEl?.value;
    const pwRepeat = pwRepeatEl?.value;
    const address = addressEl?.value.trim();


    if (!firstName) errors.push("First name is required.");
    if (!lastName) errors.push("Last name is required.");

    if (!email) {
        errors.push("Email must not be empty.");
    } else if (!/^\S+@\S+\.\S+$/.test(email)) {
        errors.push("Please enter a valid email address.");
    }

    if (!pw) {
        errors.push("Password must not be empty.");
    } else if (pw.length < 6) {
        errors.push("The password must be at least 6 characters long.");
    }

    if (pw !== pwRepeat) {
        errors.push("The passwords do not match.");
    }

    if (errors.length) {
        showError(errors);
        return null;
    }

    showError(null);

    return {
        firstName: firstName,
        lastName: lastName,
        email: email,
        password: pw,
        address: address || null
    };
}

function doRegister(payload) {
    return fetch("/api/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    }).then(async (res) => {
        if (!res.ok) {
            let body;
            try {
                body = await res.json();
            } catch {
                body = await res.text();
            }

            if (Array.isArray(body)) {
                showError(body);
            } else if (typeof body === "string" && body.trim()) {
                showError(body);
            } else {
                showError("Registration failed. Please check your entries.");
            }
            throw new Error("Register failed");
        }

        showError(null);
        return res.json().catch(() => ({}));
    });
}

/* ---------- Init ---------- */

document.addEventListener("DOMContentLoaded", () => {
    const tabLogin = document.getElementById("tabLogin");
    const tabRegister = document.getElementById("tabRegister");
    const loginForm = document.getElementById("loginForm");
    const registerForm = document.getElementById("registerForm");

    if (tabLogin) tabLogin.addEventListener("click", switchToLogin);
    if (tabRegister) tabRegister.addEventListener("click", switchToRegister);

    if (loginForm) {
        loginForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const payload = validateLoginForm();
            if (!payload) return;

            const submitBtn = document.getElementById("loginSubmit");
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = "Logging in…";
            }

            doLogin(payload)
                .then(() => {
                    // After successful login: redirect to dashboard
                    window.location.href = "/dashboard";
                })
                .catch(() => {
                    // Error is already displayed by doLogin()
                    // This catch just prevents unhandled promise rejection
                })
                .finally(() => {
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.textContent = "Login";
                    }
                });
        });
    }

    if (registerForm) {
        registerForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const payload = validateRegisterForm();
            if (!payload) return;

            const submitBtn = document.getElementById("registerSubmit");
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = "Will be created…";
            }

            doRegister(payload)
                .then(() => {
                    // After successful registration, switch directly to login
                    switchToLogin();
                    showSuccess("Your account has been created. You can now log in.");
                })
                .finally(() => {
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.textContent = "Create account";
                    }
                });
        });
    }
});
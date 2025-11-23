
const loginTab = document.getElementById("loginTab");
const registerTab = document.getElementById("registerTab");
const loginForm = document.getElementById("loginForm");
const registerForm = document.getElementById("registerForm");

if (loginTab && registerTab) {
    loginTab.addEventListener("click", () => {
        loginTab.classList.add("active");
        registerTab.classList.remove("active");
        loginForm.classList.add("active");
        registerForm.classList.remove("active");
    });

    registerTab.addEventListener("click", () => {
        registerTab.classList.add("active");
        loginTab.classList.remove("active");
        registerForm.classList.add("active");
        loginForm.classList.remove("active");
    });
}


async function login() {
    const email = document.getElementById("loginEmail").value;
    const pw = document.getElementById("loginPassword").value;

    // keep this from the demo function
    if (!email || !pw) {
        alert("Bitte alle Felder ausfüllen");
        return;
    }

    try {
        const res = await fetch("/api/auth/login", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({ email, password: pw })
        });

        const text = await res.text();
        alert(text);

        if (res.ok) {
            window.location.href = "/dashboard";
        }

    } catch (err) {
        alert("Fehler beim Login");
        console.error(err);
    }
}


// Demo
async function register() {
    const email = document.getElementById("regEmail").value;
    const pw1 = document.getElementById("regPassword").value;
    const pw2 = document.getElementById("regPassword2").value;

    if (!email || !pw1 || !pw2) {
        alert("Bitte alle Felder ausfüllen");
        return;
    }

    if (pw1 !== pw2) {
        alert("Passwörter stimmen nicht überein");
        return;
    }

    const name = "Demo User"; // or add an input field

    try {
        const res = await fetch("/api/auth/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email, password: pw1 })
        });

        const text = await res.text();
        alert(text);

        if (res.ok) {
            window.location.href = "/dashboard";
        }

    } catch (err) {
        alert("Fehler bei der Registrierung");
        console.error(err);
    }
}

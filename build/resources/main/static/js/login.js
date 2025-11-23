
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


function login() {
    const email = document.getElementById("loginEmail").value;
    const pw = document.getElementById("loginPassword").value;

    if (!email || !pw) {
        alert("Bitte alle Felder ausfüllen");
        return;
    }

    console.log("Login:", email, pw);
    alert("Login erfolgreich (Demo)");
}

// Demo
function register() {
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

    console.log("Registrierung:", email, pw1);
    alert("Registrierung erfolgreich (Demo)");
}
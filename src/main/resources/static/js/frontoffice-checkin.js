let currentEventId = null;
let allParticipants = [];
let filteredParticipants = [];

/* -------- Helper ---------- */

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatDateTime(iso) {
    if (!iso) return "–";
    // Simply display unchanged, backend can format better later
    return iso.replace("T", " ");
}

/* -------- Event-Liste ---------- */

function loadEventsForSelect() {
    const eventSelect = document.getElementById("eventSelect");
    if (!eventSelect) return;

    fetch("/api/events")
        .then((res) => {
            if (!res.ok) throw new Error("Events could not be loaded");
            return res.json();
        })
        .then((events) => {
            if (!Array.isArray(events)) throw new Error("Unexpected format");
            // Existing options = "Please select event..."
            events.forEach((ev) => {
                const opt = document.createElement("option");
                opt.value = ev.id;
                opt.textContent = ev.title || `Event #${ev.id}`;
                opt.dataset.date = ev.date || "";
                opt.dataset.location = ev.location || "";
                eventSelect.appendChild(opt);
            });
        })
        .catch((err) => {
            console.error(err);
            // No hard UI error handling, user simply does not see any events
        });
}

/* -------- Participants ---------- */

function loadParticipants(eventId) {
    const tableBody = document.getElementById("participantTableBody");
    const empty = document.getElementById("tableEmpty");

    if (tableBody) tableBody.innerHTML = "";
    if (empty) empty.textContent = "Participants are being loaded...";

    currentEventId = eventId;
    allParticipants = [];
    filteredParticipants = [];

    if (!eventId) {
        if (empty) {
            empty.textContent = "No participants found. Choose an event or customize your filters.";
        }
        updateStats();
        return;
    }

    fetch(`/api/checkin/event/${encodeURIComponent(eventId)}/participants`)
        .then((res) => {
            if (!res.ok) throw new Error("Participants could not be loaded");
            return res.json();
        })
        .then((data) => {
            if (!Array.isArray(data)) throw new Error("Unexpected participant format");
            allParticipants = data;
            applyFilters();
        })
        .catch((err) => {
            console.error(err);
            if (empty) {
                empty.textContent =
                    "Participants could not be loaded. Please try again or contact the back office.";
            }
            updateStats();
        });
}

/* -------- Filter / Suche ---------- */

function applyFilters() {
    const onlyOpen = document.getElementById("filterOnlyOpen")?.checked;
    const searchTerm = document
        .getElementById("searchInput")
        ?.value.trim()
        .toLowerCase() || "";

    filteredParticipants = allParticipants.filter((p) => {
        const isCheckedIn = !!p.checkedIn;

        if (onlyOpen && isCheckedIn) {
            return false;
        }

        if (!searchTerm) return true;

        const haystack = [
            p.bookingNumber || "",
            p.firstName || "",
            p.lastName || "",
            p.email || ""
        ]
            .join(" ")
            .toLowerCase();

        return haystack.includes(searchTerm);
    });

    renderTable();
    updateStats();
}

/* -------- Stats ---------- */

function updateStats() {
    const statTotal = document.getElementById("statTotal");
    const statCheckedIn = document.getElementById("statCheckedIn");
    const statOpen = document.getElementById("statOpen");

    const total = allParticipants.length;
    const checked = allParticipants.filter((p) => p.checkedIn).length;
    const open = total - checked;

    if (statTotal) statTotal.textContent = String(total);
    if (statCheckedIn) statCheckedIn.textContent = String(checked);
    if (statOpen) statOpen.textContent = String(open);
}

/* -------- Tabelle ---------- */

function renderTable() {
    const tbody = document.getElementById("participantTableBody");
    const empty = document.getElementById("tableEmpty");
    if (!tbody || !empty) return;

    if (!currentEventId) {
        tbody.innerHTML = "";
        empty.textContent = "No participants found. Choose an event or customize your filters.";
        empty.classList.remove("hidden");
        return;
    }

    if (!filteredParticipants.length) {
        tbody.innerHTML = "";
        empty.textContent = "No participants match the current filters.";
        empty.classList.remove("hidden");
        return;
    }

    empty.classList.add("hidden");

    const rows = filteredParticipants.map((p) => {
        const name = `${p.firstName || ""} ${p.lastName || ""}`.trim() || "–";
        const email = p.email || "–";
        const statusText = p.checkedIn ? "Eingecheckt" : "Offen";
        const statusClass = p.checkedIn ? "status-pill--checked" : "status-pill--open";
        const checkInTime = p.checkedIn ? formatDateTime(p.checkInTime) : "–";
        const btnLabel = p.checkedIn ? "Put back" : "Check-in";

        return `
      <tr data-id="${p.id}">
        <td>${escapeHtml(p.bookingNumber || "–")}</td>
        <td>${escapeHtml(name)}</td>
        <td>${escapeHtml(email)}</td>
        <td>
          <span class="status-pill ${statusClass}">${statusText}</span>
        </td>
        <td>${escapeHtml(checkInTime)}</td>
        <td>
          <div class="action-buttons">
            <button type="button"
                    class="btn-small ${p.checkedIn ? "btn-small--reset" : "btn-small--checkin"}"
                    data-toggle="${p.id}">
              ${btnLabel}
            </button>
          </div>
        </td>
      </tr>
    `;
    });

    tbody.innerHTML = rows.join("");

    setupRowActions();
}

/* -------- Check-In Toggle ---------- */

function toggleCheckIn(participantId, newState) {
    // PUT /api/checkin/participants/{id} mit { checkedIn: true/false }
    return fetch(`/api/checkin/participants/${encodeURIComponent(participantId)}`, {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ checkedIn: newState })
    }).then((res) => {
        if (!res.ok) {
            throw new Error("Check-in could not be saved");
        }
    });
}

function setupRowActions() {
    document.querySelectorAll("[data-toggle]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-toggle");
            if (!id) return;

            const participant = allParticipants.find((p) => String(p.id) === String(id));
            if (!participant) return;

            const newState = !participant.checkedIn;

            // optimistisches UI-Update
            participant.checkedIn = newState;
            if (newState) {
                participant.checkInTime = new Date().toISOString();
            } else {
                participant.checkInTime = null;
            }
            applyFilters();

            toggleCheckIn(id, newState).catch((err) => {
                console.error(err);
                // Rollback, falls Call fehlschlägt
                participant.checkedIn = !newState;
                participant.checkInTime = newState ? null : participant.checkInTime;
                applyFilters();
                alert("Check-in status could not be saved.");
            });
        });
    });
}

/* -------- Event-Meta ---------- */

function updateEventMetaFromSelect() {
    const select = document.getElementById("eventSelect");
    const titleEl = document.getElementById("eventMetaTitle");
    const infoEl = document.getElementById("eventMetaInfo");

    if (!select || !titleEl || !infoEl) return;

    const opt = select.options[select.selectedIndex];
    if (!opt || !opt.value) {
        titleEl.textContent = "No event selected";
        infoEl.textContent = "Please select an event to display participants.";
        return;
    }

    const title = opt.textContent || "Event";
    const date = opt.dataset.date || "";
    const location = opt.dataset.location || "";

    titleEl.textContent = title;
    const parts = [];
    if (date) parts.push(date);
    if (location) parts.push(location);
    infoEl.textContent = parts.length ? parts.join(" · ") : "Details about the event";
}

/* -------- Navigation ---------- */

function setupNavigation() {
    const goToDashboard = document.getElementById("goToDashboard");
    const goToBackoffice = document.getElementById("goToBackoffice");
    const logoutBtn = document.getElementById("logoutBtn");

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

    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            try {
                localStorage.removeItem("simplyevents_currentUser");
            } catch (_) {}
            window.location.href = "/login";
        });
    }
}

/* -------- Init ---------- */

document.addEventListener("DOMContentLoaded", () => {
    setupNavigation();
    loadEventsForSelect();

    const eventSelect = document.getElementById("eventSelect");
    const filterOnlyOpen = document.getElementById("filterOnlyOpen");
    const searchInput = document.getElementById("searchInput");

    if (eventSelect) {
        eventSelect.addEventListener("change", () => {
            updateEventMetaFromSelect();
            const value = eventSelect.value || "";
            loadParticipants(value || null);
        });
    }

    if (filterOnlyOpen) {
        filterOnlyOpen.addEventListener("change", applyFilters);
    }

    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }

    updateEventMetaFromSelect();
    updateStats();
});
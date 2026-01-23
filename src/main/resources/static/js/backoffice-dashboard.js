// ------- Helpers -------

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatPrice(value) {
    if (value == null) return "–";
    const n = Number(value);
    if (isNaN(n)) return escapeHtml(String(value));
    return `${n.toFixed(2)} €`;
}

// ------- State -------

let allEvents = [];
let filteredEvents = [];
let selectedCategory = "all";

// ------- Data Loading -------

function loadEvents(statusFilter = "ALL") {
    const url = statusFilter && statusFilter !== "ALL" ? `/api/events/backoffice?status=${encodeURIComponent(statusFilter)}` : "/api/events/backoffice";
    return fetch(url)
        .then((res) => {
            if (!res.ok) throw new Error("Events could not be loaded");
            return res.json();
        })
        .then((data) => {
            if (!Array.isArray(data)) throw new Error("Unexpected data format");
            allEvents = data;
            filteredEvents = [...allEvents];
            initCategoryFilter(allEvents);
            updateStats(filteredEvents);
            renderCharts(filteredEvents);
            renderTable(filteredEvents);
            renderTopEvents(filteredEvents);
        })
        .catch((err) => {
            console.error(err);
            const empty = document.getElementById("eventsEmpty");
            if (empty) empty.classList.remove("hidden");
        });
}

// ------- Filters (Unchanged) -------

function initCategoryFilter(events) {
    const select = document.getElementById("filterCategory");
    if (!select) return;
    const categories = new Set();
    events.forEach((ev) => {
        if (ev.category && ev.category.trim() !== "") {
            categories.add(ev.category.trim());
        }
    });
    select.innerHTML = `<option value="all">All categories</option>`;
    Array.from(categories).sort().forEach((cat) => {
        const opt = document.createElement("option");
        opt.value = cat;
        opt.textContent = cat;
        select.appendChild(opt);
    });
}

function applyFilters() {
    const period = document.getElementById("filterPeriod")?.value || "all";
    const category = document.getElementById("filterCategory")?.value || "all";
    const statusFilter = document.getElementById("filterStatus")?.value || "ALL";
    const search = document.getElementById("eventSearch")?.value.trim().toLowerCase() || "";

    if (statusFilter !== "ALL") {
        loadEvents(statusFilter).then(() => filterAndRender(period, category, search));
    } else {
        filterAndRender(period, category, search);
    }
}

function filterAndRender(period, category, search) {
    filteredEvents = allEvents.filter((ev) => {
        const cat = (ev.category || "").trim();
        const matchesCategory = category === "all" || cat === category;
        const haystack = [ev.title || "", ev.location || "", ev.description || ""].join(" ").toLowerCase();
        const matchesSearch = !search || haystack.includes(search);

        const dateStr = ev.date || "";
        let matchesPeriod = true;
        if (period === "year") {
            const year = new Date().getFullYear().toString();
            matchesPeriod = dateStr.startsWith(year);
        }
        return matchesCategory && matchesSearch && matchesPeriod;
    });

    updateStats(filteredEvents);
    renderCharts(filteredEvents);
    renderTable(filteredEvents);
    renderTopEvents(filteredEvents);
}

// ------- Stats & Charts (Keep existing implementations) -------
// ... (Insert updateStats, renderCharts, buildRevenueByMonth, etc. here if needed,
// they are fine as they were in your previous upload) ...

function updateStats(events) { /* ... same as before ... */ }
function renderCharts(events) { /* ... same as before ... */ }

// ------- Table (UPDATED) -------

function renderTable(events) {
    const tbody = document.getElementById("eventsTableBody");
    const empty = document.getElementById("eventsEmpty");
    if (!tbody || !empty) return;

    if (!events.length) {
        tbody.innerHTML = "";
        empty.classList.remove("hidden");
        return;
    }

    empty.classList.add("hidden");

    const rows = events.map((ev) => {
        const id = ev.id;
        const title = escapeHtml(ev.title || "");
        const location = escapeHtml(ev.location || "");
        const date = escapeHtml(ev.date || "");
        const capacity = ev.capacity ?? "–";
        const price = formatPrice(ev.price);
        const status = escapeHtml(ev.status || "PLANNED");
        const isCancelled = ev.cancelled === true;

        // 1. Determine Edit Permission
        const isEditable = (status === "PLANNED" && !isCancelled);

        const cancelledBadge = isCancelled ? `<span class="badge badge--cancelled">Cancelled</span>` : "";
        const cancelButtonText = isCancelled ? "Uncancel" : "Cancel";

        // Publish Button: Only if PLANNED
        const publishButton = (status === "PLANNED" && !isCancelled)
            ? `<button type="button" class="action-btn action-btn--publish" data-publish="${id}">Publish</button>`
            : "";

        // Edit Button: Disabled if not planned
        const editButton = isEditable
            ? `<button type="button" class="action-btn action-btn--edit" data-edit="${id}">Edit</button>`
            : `<button type="button" class="action-btn action-btn--edit" disabled style="opacity:0.5; cursor:not-allowed;" title="Only planned events can be edited">Edit</button>`;

        const rowClass = isCancelled ? "event-row--cancelled" : "";

        return `
      <tr data-id="${id}" class="${rowClass}">
        <td>${id}</td>
        <td>${title}</td>
        <td>${location}</td>
        <td>${date}</td>
        <td>${capacity}</td>
        <td>${price}</td>
        <td>${status} ${cancelledBadge}</td>
        <td>
          <div class="event-actions">
            ${editButton}
            <button type="button" class="action-btn action-btn--cancel" data-cancel="${id}">${cancelButtonText}</button>
            <button type="button" class="action-btn action-btn--delete" data-delete="${id}">Delete</button>
            ${publishButton}
          </div>
        </td>
      </tr>
    `;
    });

    tbody.innerHTML = rows.join("");
    setupRowActions();
}

// ------- Row Actions (UPDATED) -------

function setupRowActions() {
    // 2. Fix Edit Redirect
    document.querySelectorAll("[data-edit]").forEach((btn) => {
        if (!btn.disabled) {
            btn.addEventListener("click", () => {
                const id = btn.getAttribute("data-edit");
                if (id) {
                    // Redirect to the Editor, not the Details page
                    window.location.href = `/edit-event/${encodeURIComponent(id)}`;
                }
            });
        }
    });

    document.querySelectorAll("[data-delete]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-delete");
            if (!id || !confirm("Do you really want to delete this event?")) return;

            fetch(`/api/events/${encodeURIComponent(id)}`, { method: "DELETE" })
                .then((res) => {
                    if (!res.ok) throw new Error("Deletion failed");
                    allEvents = allEvents.filter((ev) => String(ev.id) !== String(id));
                    applyFilters();
                })
                .catch((err) => {
                    console.error(err);
                    alert("Event could not be deleted.");
                });
        });
    });

    document.querySelectorAll("[data-publish]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-publish");
            if (!id || !confirm("Do you really want to publish this event?")) return;

            fetch(`/api/events/${encodeURIComponent(id)}/publish`, { method: "POST" })
                .then((res) => {
                    if (!res.ok) throw new Error("Publishing failed");
                    return res.json();
                })
                .then((updated) => {
                    const idx = allEvents.findIndex((ev) => String(ev.id) === String(id));
                    if (idx >= 0) allEvents[idx] = updated;
                    applyFilters();
                })
                .catch((err) => alert("Event could not be published."));
        });
    });

    // ... (Cancel handler remains the same) ...
}

// ------- Navigation & Init -------

function setupNavigation() {
    const navMap = {
        "goToCreateEvent": "/create-event",
        "goToInvoices": "/invoices",
        "goToFrontoffice": "/frontoffice-checkin",
        "goToDashboard": "/dashboard"
    };

    Object.keys(navMap).forEach(id => {
        const el = document.getElementById(id);
        if (el) el.addEventListener("click", () => window.location.href = navMap[id]);
    });

    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            try { localStorage.removeItem("simplyevents_currentUser"); } catch (_) {}
            window.location.href = "/login";
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    setupNavigation();
    const periodSel = document.getElementById("filterPeriod");
    const categorySel = document.getElementById("filterCategory");
    const statusSel = document.getElementById("filterStatus");
    const searchInput = document.getElementById("eventSearch");

    if (periodSel) periodSel.addEventListener("change", applyFilters);
    if (categorySel) categorySel.addEventListener("change", applyFilters);
    if (statusSel) statusSel.addEventListener("change", applyFilters);
    if (searchInput) searchInput.addEventListener("input", applyFilters);

    loadEvents();
});
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

// ------- Filters -------

function initCategoryFilter(events) {
    const select = document.getElementById("filterCategory");
    if (!select) return;

    const categories = new Set();
    events.forEach((ev) => {
        if (ev.category && ev.category.trim() !== "") {
            categories.add(ev.category.trim());
        }
    });

    // Clear except "all"
    select.innerHTML = `<option value="all">All categories</option>`;
    Array.from(categories)
        .sort()
        .forEach((cat) => {
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

    selectedCategory = category;

    if (statusFilter !== "ALL") {
        loadEvents(statusFilter).then(() => filterAndRender(period, category, search));
        return;
    }

    filterAndRender(period, category, search);
}

function filterAndRender(period, category, search) {
    filteredEvents = allEvents.filter((ev) => {
        // Category
        const cat = (ev.category || "").trim();
        const matchesCategory = category === "all" || cat === category;

        // Text search
        const haystack = [ev.title || "", ev.location || "", ev.description || ""]
            .join(" ")
            .toLowerCase();
        const matchesSearch = !search || haystack.includes(search);

        // timezone (Here only very rough: we filter by year, or let everyone in)
        const dateStr = ev.date || "";
        let matchesPeriod = true;
        if (period === "year") {
            const year = new Date().getFullYear().toString();
            matchesPeriod = dateStr.startsWith(year);
        }
        // Period "30d" / "90d" -> do not filter hard for now, can be expanded later
        // (backend may not provide time information, so we simplify this first)

        return matchesCategory && matchesSearch && matchesPeriod;
    });

    updateStats(filteredEvents);
    renderCharts(filteredEvents);
    renderTable(filteredEvents);
    renderTopEvents(filteredEvents);
}

// ------- Stats -------

function updateStats(events) {
    const statEvents = document.getElementById("statEvents");
    const statBookings = document.getElementById("statBookings");
    const statUtilization = document.getElementById("statUtilization");
    const statRevenue = document.getElementById("statRevenue");

    if (!events) events = [];

    const totalEvents = events.length;


    let totalCapacity = 0;
    let totalUsed = 0;
    let estimatedBookings = 0;
    let estimatedRevenue = 0;

    events.forEach((ev) => {
        const capacity = Number(ev.capacity ?? 0);
        const available = Number(ev.availableSlots ?? capacity);
        const used = Math.max(0, capacity - available);
        const price = Number(ev.price ?? 0);

        totalCapacity += capacity;
        totalUsed += used;
        estimatedBookings += used || 0;
        estimatedRevenue += used * (isNaN(price) ? 0 : price);
    });

    const utilization = totalCapacity > 0 ? (totalUsed / totalCapacity) * 100 : 0;

    if (statEvents) statEvents.textContent = String(totalEvents);
    if (statBookings) statBookings.textContent = String(estimatedBookings);
    if (statUtilization) statUtilization.textContent = `${utilization.toFixed(1)} %`;
    if (statRevenue) statRevenue.textContent = `${estimatedRevenue.toFixed(2)} €`;

    // Trend-Texte aktuell nur als Platzhalter
    const statEventsChange = document.getElementById("statEventsChange");
    const statBookingsChange = document.getElementById("statBookingsChange");
    const statUtilizationChange = document.getElementById("statUtilizationChange");
    const statRevenueChange = document.getElementById("statRevenueChange");

    if (statEventsChange) statEventsChange.textContent = "Vs. previous period";
    if (statBookingsChange) statBookingsChange.textContent = "Trend data follow";
    if (statUtilizationChange) statUtilizationChange.textContent = "Trend data follow";
    if (statRevenueChange) statRevenueChange.textContent = "Trend Data follow";
}

// ------- Charts -------

function buildRevenueByMonth(events) {
    const map = new Map(); // key: "YYYY-MM", value: revenue

    events.forEach((ev) => {
        const dateStr = ev.date || "";
        if (!dateStr) return;
        const key = dateStr.slice(0, 7); // "YYYY-MM"
        const capacity = Number(ev.capacity ?? 0);
        const price = Number(ev.price ?? 0);
        const revenue = capacity * (isNaN(price) ? 0 : price);

        map.set(key, (map.get(key) || 0) + revenue);
    });

    const entries = Array.from(map.entries()).sort(([a], [b]) => (a < b ? -1 : 1));
    return entries.map(([month, revenue]) => ({ month, revenue }));
}

function buildCategoryDistribution(events) {
    const map = new Map();
    events.forEach((ev) => {
        const cat = (ev.category || "Other").trim() || "Other";
        map.set(cat, (map.get(cat) || 0) + 1);
    });
    return Array.from(map.entries()).map(([category, count]) => ({ category, count }));
}

function renderCharts(events) {
    renderRevenueChart(events);
    renderCategoryChart(events);
}

function renderRevenueChart(events) {
    const container = document.getElementById("revenueChart");
    if (!container) return;
    container.innerHTML = "";

    const data = buildRevenueByMonth(events);
    if (!data.length) {
        container.textContent = "No sales data for the selected period.";
        return;
    }

    const max = Math.max(...data.map((d) => d.revenue));
    if (max <= 0) {
        container.textContent = "No sales data for the selected period.";
        return;
    }

    data.forEach((d) => {
        const bar = document.createElement("div");
        bar.className = "chart-bar";

        const fill = document.createElement("div");
        fill.className = "chart-bar__fill";
        const heightPercent = (d.revenue / max) * 100;
        fill.style.height = `${heightPercent}%`;

        const label = document.createElement("div");
        label.className = "chart-bar__label";
        // d.month is "YYYY-MM" -> show only MM
        label.textContent = d.month.slice(5);

        bar.appendChild(fill);
        bar.appendChild(label);
        container.appendChild(bar);
    });
}

function renderCategoryChart(events) {
    const container = document.getElementById("categoryChart");
    const legend = document.getElementById("categoryLegend");
    if (!container || !legend) return;
    container.innerHTML = "";
    legend.innerHTML = "";

    const data = buildCategoryDistribution(events);
    if (!data.length) {
        container.textContent = "No categories available.";
        return;
    }

    const total = data.reduce((sum, d) => sum + d.count, 0);
    if (!total) {
        container.textContent = "No categories available.";
        return;
    }

    // Simple Colors
    const colors = [
        "#4b7bec",
        "#22c55e",
        "#f97316",
        "#8b5cf6",
        "#06b6d4",
        "#ec4899",
        "#a3a3a3"
    ];

    // Build conic-gradient string
    let currentAngle = 0;
    const segments = [];
    data.forEach((d, index) => {
        const fraction = d.count / total;
        const angle = fraction * 360;
        const start = currentAngle;
        const end = currentAngle + angle;
        const color = colors[index % colors.length];
        segments.push(`${color} ${start.toFixed(1)}deg ${end.toFixed(1)}deg`);
        currentAngle = end;

        // Legend item
        const li = document.createElement("li");
        li.className = "chart-legend__item";
        const swatch = document.createElement("span");
        swatch.className = "chart-legend__swatch";
        swatch.style.backgroundColor = color;
        const text = document.createElement("span");
        const percent = ((fraction) * 100).toFixed(1);
        text.textContent = `${d.category} (${percent}%)`;
        li.appendChild(swatch);
        li.appendChild(text);
        legend.appendChild(li);
    });

    const pie = document.createElement("div");
    pie.className = "chart-pie";
    pie.style.background = `conic-gradient(${segments.join(", ")})`;
    container.appendChild(pie);
}

// ------- Table & Top Events -------

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
        const publishButton = status === "PLANNED"
            ? `<button type="button" class="action-btn action-btn--publish" data-publish="${id}">Publish</button>`
            : "";

        return `
      <tr data-id="${id}">
        <td>${id}</td>
        <td>${title}</td>
        <td>${location}</td>
        <td>${date}</td>
        <td>${capacity}</td>
        <td>${price}</td>
        <td>${status}</td>
        <td>
          <div class="event-actions">
            <button type="button" class="action-btn action-btn--edit" data-edit="${id}">Edit</button>
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

function renderTopEvents(events) {
    const container = document.getElementById("topEventsList");
    const empty = document.getElementById("topEventsEmpty");
    if (!container || !empty) return;

    if (!events.length) {
        container.innerHTML = "";
        empty.classList.remove("hidden");
        return;
    }

    empty.classList.add("hidden");

    // (capacity - availableSlots) / capacity
    const scored = events
        .map((ev) => {
            const capacity = Number(ev.capacity ?? 0);
            const available = Number(ev.availableSlots ?? capacity);
            const used = Math.max(0, capacity - available);
            const ratio = capacity > 0 ? used / capacity : 0;
            return { ev, ratio };
        })
        .sort((a, b) => b.ratio - a.ratio)
        .slice(0, 5);

    const items = scored.map(({ ev, ratio }) => {
        const title = escapeHtml(ev.title || "");
        const percent = (ratio * 100).toFixed(0);
        return `
      <div class="top-event-item" data-id="${ev.id}">
        <p class="top-event-item__title" title="${title}">${title}</p>
        <span class="top-event-item__badge">${percent}% utilized</span>
      </div>
    `;
    });

    container.innerHTML = items.join("");

    container.querySelectorAll(".top-event-item").forEach((el) => {
        el.addEventListener("click", () => {
            const id = el.getAttribute("data-id");
            if (id) {
                window.location.href = `/event-details/${encodeURIComponent(id)}`;
            }
        });
    });
}

// ------- Row Actions -------

function setupRowActions() {
    document.querySelectorAll("[data-edit]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-edit");
            if (id) {
                window.location.href = `/event-details/${encodeURIComponent(id)}`;
            }
        });
    });

    document.querySelectorAll("[data-delete]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-delete");
            if (!id) return;
            if (!confirm("Do you really want to delete this event?")) return;

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
            if (!id) return;
            if (!confirm("Do you really want to publish this event?")) return;

            fetch(`/api/events/${encodeURIComponent(id)}/publish`, { method: "POST" })
                .then((res) => {
                    if (!res.ok) throw new Error("Publishing failed");
                    return res.json();
                })
                .then((updated) => {
                    const idx = allEvents.findIndex((ev) => String(ev.id) === String(id));
                    if (idx >= 0) {
                        allEvents[idx] = updated;
                    }
                    applyFilters();
                })
                .catch((err) => {
                    console.error(err);
                    alert("Event could not be published.");
                });
        });
    });
}

// ------- Navigation -------

function setupNavigation() {
    const goToCreateEvent = document.getElementById("goToCreateEvent");
    const goToFrontoffice = document.getElementById("goToFrontoffice");
    const logoutBtn = document.getElementById("logoutBtn");

    if (goToCreateEvent) {
        goToCreateEvent.addEventListener("click", () => {
            window.location.href = "/create-event";
        });
    }

    if (goToFrontoffice) {
        goToFrontoffice.addEventListener("click", () => {
            // here e.g. Frontoffice-View (/frontoffice-dashboard)
            window.location.href = "/frontoffice-checkin";
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

// ------- Init -------

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
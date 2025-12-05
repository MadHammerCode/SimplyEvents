let allEvents = [];
let selectedCategory = "All";

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatPrice(price) {
    if (price == null) return "Free";
    const num = Number(price);
    if (isNaN(num)) return escapeHtml(String(price));
    return `${num.toFixed(2)} €`;
}

function computeCategories(events) {
    const set = new Set();
    events.forEach((ev) => {
        if (ev.category && ev.category.trim() !== "") {
            set.add(ev.category.trim());
        }
    });
    const list = Array.from(set);
    list.sort();
    return ["All", ...list];
}

/* ---------- DOM Rendering ---------- */

function renderCategories(events) {
    const container = document.getElementById("categoryChips");
    if (!container) return;
    const cats = computeCategories(events);
    container.innerHTML = "";

    cats.forEach((cat) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "chip" + (cat === selectedCategory ? " chip--active" : "");
        btn.textContent = cat;
        btn.addEventListener("click", () => {
            selectedCategory = cat;
            renderCategories(allEvents);
            renderPopularEvents();
            updateHighlightEvent();
        });
        container.appendChild(btn);
    });
}

function getFilteredEvents() {
    const searchKeyword = document.getElementById("searchKeyword")?.value.trim().toLowerCase() || "";
    const searchLocation = document.getElementById("searchLocation")?.value.trim().toLowerCase() || "";
    const searchDate = document.getElementById("searchDate")?.value || "";

    return allEvents.filter((ev) => {
        const category = (ev.category || "").trim();
        const matchesCategory = selectedCategory === "Alle" || category === selectedCategory;

        const haystack = [
            ev.title || "",
            ev.description || "",
            ev.location || ""
        ]
            .join(" ")
            .toLowerCase();

        const matchesKeyword = !searchKeyword || haystack.includes(searchKeyword);
        const matchesLocation =
            !searchLocation ||
            String(ev.location || "")
                .toLowerCase()
                .includes(searchLocation);

        const matchesDate =
            !searchDate || String(ev.date || "").startsWith(searchDate);

        return matchesCategory && matchesKeyword && matchesLocation && matchesDate;
    });
}

function renderPopularEvents() {
    const grid = document.getElementById("popularEventsGrid");
    const empty = document.getElementById("popularEmpty");
    if (!grid || !empty) return;

    const filtered = getFilteredEvents();

    if (!filtered.length) {
        grid.innerHTML = "";
        empty.classList.remove("hidden");
        return;
    }
    empty.classList.add("hidden");

    // Very simple "popularity": simply the first 6 events
    const eventsToShow = filtered.slice(0, 6);

    const cards = eventsToShow.map((ev) => {
        const id = ev.id;
        const title = escapeHtml(ev.title || "Event");
        const location = escapeHtml(ev.location || "Location follows");
        const date = ev.date || "";
        const time = ev.time || "";
        const dateTime = date ? (time ? `${date} · ${time}` : date) : "Date follows";
        const priceText = formatPrice(ev.price);
        const category = escapeHtml(ev.category || "Event");
        const imagePath = ev.imagePath ? `/${ev.imagePath}` : "/images/default-event.jpg";

        return `
      <article class="event-card" data-event-id="${id}">
        <div class="event-card__image-wrap">
          <img src="${imagePath}" alt="${title}" class="event-card__image">
        </div>
        <div class="event-card__body">
          <div class="event-card__category">${category}</div>
          <h3 class="event-card__title">${title}</h3>
          <p class="event-card__location">${location}</p>
          <div class="event-card__meta">
            <span>${escapeHtml(dateTime)}</span>
            <span class="event-card__price">${priceText}</span>
          </div>
        </div>
      </article>
    `;
    });

    grid.innerHTML = cards.join("");

    grid.querySelectorAll(".event-card").forEach((card) => {
        card.addEventListener("click", () => {
            const id = card.getAttribute("data-event-id");
            if (id) {
                window.location.href = `/event-details/${encodeURIComponent(id)}`;
            }
        });
    });
}

function updateHighlightEvent() {
    const titleEl = document.getElementById("highlightTitle");
    const metaEl = document.getElementById("highlightMeta");
    const locEl = document.getElementById("highlightLocation");
    const btn = document.getElementById("highlightButton");
    if (!titleEl || !metaEl || !locEl || !btn) return;

    const filtered = getFilteredEvents();
    if (!filtered.length) {
        titleEl.textContent = "No matching events found";
        metaEl.textContent = "Customize your search or discover all events.";
        locEl.textContent = "";
        btn.disabled = true;
        return;
    }

    // Einfach das erste Event als "Highlight"
    const ev = filtered[0];
    const date = ev.date || "";
    const time = ev.time || "";
    const dateTime = date ? (time ? `${date} · ${time}` : date) : "Date follows";
    const category = ev.category || "Event";

    titleEl.textContent = ev.title || "Event";
    metaEl.textContent = `${category} • ${dateTime}`;
    locEl.textContent = ev.location || "Location follows";
    btn.disabled = false;

    btn.onclick = () => {
        if (ev.id != null) {
            window.location.href = `/event-details/${encodeURIComponent(ev.id)}`;
        }
    };
}

function updateStats() {
    const statEvents = document.getElementById("statEvents");
    if (!statEvents) return;
    statEvents.textContent = `${allEvents.length}`;
}

/* ---------- Aktionen ---------- */

function setupActions() {
    const searchButton = document.getElementById("searchButton");
    const discoverButton = document.getElementById("discoverButton");
    const seeAllButton = document.getElementById("seeAllButton");

    const keywordInput = document.getElementById("searchKeyword");
    const locationInput = document.getElementById("searchLocation");
    const dateInput = document.getElementById("searchDate");

    if (searchButton) {
        searchButton.addEventListener("click", () => {
            renderPopularEvents();
            updateHighlightEvent();
        });
    }

    if (discoverButton) {
        discoverButton.addEventListener("click", () => {
            window.location.href = "/dashboard";
        });
    }

    if (seeAllButton) {
        seeAllButton.addEventListener("click", () => {
            window.location.href = "/dashboard";
        });
    }

    if (keywordInput) {
        keywordInput.addEventListener("input", () => {
            renderPopularEvents();
            updateHighlightEvent();
        });
    }
    if (locationInput) {
        locationInput.addEventListener("input", () => {
            renderPopularEvents();
            updateHighlightEvent();
        });
    }
    if (dateInput) {
        dateInput.addEventListener("change", () => {
            renderPopularEvents();
            updateHighlightEvent();
        });
    }
}

/* ---------- Daten laden ---------- */

function fetchEvents() {
    fetch("/api/events")
        .then((res) => {
            if (!res.ok) {
                throw new Error("Failed to load events");
            }
            return res.json();
        })
        .then((data) => {
            if (!Array.isArray(data)) {
                throw new Error("Unexpected response");
            }
            allEvents = data;
            renderCategories(allEvents);
            renderPopularEvents();
            updateHighlightEvent();
            updateStats();
        })
        .catch((err) => {
            console.error(err);
            const empty = document.getElementById("popularEmpty");
            const grid = document.getElementById("popularEventsGrid");
            if (grid) grid.innerHTML = "";
            if (empty) empty.classList.remove("hidden");
            const statEvents = document.getElementById("statEvents");
            if (statEvents) statEvents.textContent = "0";
        });
}

/* ---------- Init ---------- */

document.addEventListener("DOMContentLoaded", () => {
    setupActions();
    fetchEvents();
});
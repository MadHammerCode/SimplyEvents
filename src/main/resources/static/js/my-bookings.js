let allBookings = [];
let filteredBookings = [];
let currentFilter = "all";

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatPrice(val) {
    if (val == null) return "–";
    const n = Number(val);
    if (isNaN(n)) return escapeHtml(String(val));
    return `${n.toFixed(2)} €`;
}

/* -------- Status-Mapping (Frontend) -------- */

function classifyStatus(booking) {
    const rawStatus = (booking.status || "").toUpperCase();
    if (rawStatus === "CANCELLED") return "cancelled";

    const eventDate = booking.event?.date || null;
    if (!eventDate) {
        // If no date → everything "upcoming", except cancelled
        return "upcoming";
    }

    // Very simple past/upcoming logic: date string comparison
    const today = new Date().toISOString().slice(0, 10);
    if (eventDate < today) return "past";
    return "upcoming";
}

/* -------- Daten laden -------- */

function loadBookings() {
    fetch("/api/bookings/my/bookings")
        .then((res) => {
            if (!res.ok) throw new Error("Bookings could not be loaded");
            return res.json();
        })
        .then((data) => {
            if (!Array.isArray(data)) throw new Error("Unexpected data format");
            allBookings = data;
            applyFilters();
        })
        .catch((err) => {
            console.error(err);
            const empty = document.getElementById("bookingsEmpty");
            if (empty) {
                empty.textContent = "Bookings could not be loaded. Please try again later.";
            }
        });
}

/* -------- Filter & Suche -------- */

function applyFilters() {
    const search = document.getElementById("searchInput")?.value.trim().toLowerCase() || "";

    filteredBookings = allBookings.filter((b) => {
        const statusClass = classifyStatus(b);

        if (currentFilter === "upcoming" && statusClass !== "upcoming") return false;
        if (currentFilter === "past" && statusClass !== "past") return false;
        if (currentFilter === "cancelled" && statusClass !== "cancelled") return false;

        if (!search) return true;

        const text = [
            b.bookingNumber || "",
            b.event?.title || "",
            b.event?.location || ""
        ]
            .join(" ")
            .toLowerCase();

        return text.includes(search);
    });

    renderStats();
    renderList();
}

/* -------- Stats -------- */

function renderStats() {
    const statUpcoming = document.getElementById("statUpcoming");
    const statTotal = document.getElementById("statTotal");
    const statCancelled = document.getElementById("statCancelled");

    const total = allBookings.length;
    const upcoming = allBookings.filter((b) => classifyStatus(b) === "upcoming").length;
    const cancelled = allBookings.filter((b) => classifyStatus(b) === "cancelled").length;

    if (statTotal) statTotal.textContent = String(total);
    if (statUpcoming) statUpcoming.textContent = String(upcoming);
    if (statCancelled) statCancelled.textContent = String(cancelled);
}

/* -------- Liste rendern -------- */

function renderList() {
    const list = document.getElementById("bookingsList");
    const empty = document.getElementById("bookingsEmpty");
    if (!list || !empty) return;

    if (!filteredBookings.length) {
        list.innerHTML = "";
        empty.classList.remove("hidden");
        empty.textContent = "There are currently no bookings available for this filter.";
        return;
    }

    empty.classList.add("hidden");

    const cards = filteredBookings.map((b) => renderBookingCard(b));
    list.innerHTML = cards.join("");
    setupCardActions();
}

function renderBookingCard(booking) {
    const event = booking.event || {};
    const title = escapeHtml(event.title || "Event");
    const location = escapeHtml(event.location || "Location to be announced");
    const datetime = [
        event.date || "",
        event.time || ""
    ]
        .filter(Boolean)
        .join(" · ");

    const participants = booking.numParticipants ?? 1;
    const price = formatPrice(booking.priceTotal);
    const bookingNumber = escapeHtml(booking.bookingNumber || `#${booking.bookingId}`);

    const status = classifyStatus(booking);
    let statusText = "";
    let statusClass = "";

    if (status === "upcoming") {
        statusText = "Cooming up";
        statusClass = "status-badge--upcoming";
    } else if (status === "past") {
        statusText = "Past";
        statusClass = "status-badge--past";
    } else {
        statusText = "Cancelled";
        statusClass = "status-badge--cancelled";
    }

    const canCancel = status === "upcoming" && booking.status !== "CANCELLED";

    const bookingDate = booking.bookingDate
        ? `Gebucht am ${escapeHtml(booking.bookingDate)}`
        : "";

    return `
    <article class="mb-card" data-booking-id="${booking.bookingId}">
      <div class="mb-card__header">
        <h2 class="mb-card__event-title">${title}</h2>
        <p class="mb-card__event-meta">
          ${escapeHtml(datetime || "")}${datetime && location ? " · " : ""}${location}
        </p>
        <p class="mb-card__booking">
          Booking number. ${bookingNumber} · ${participants} Participant · ${price}
          ${bookingDate ? " · " + bookingDate : ""}
        </p>
        <span class="status-badge ${statusClass}">${statusText}</span>
      </div>

      <div class="mb-card__footer">
        <button type="button" class="mb-btn-small mb-btn-primary" data-event-id="${booking.event?.id}" data-booking-id="${booking.bookingId}">
          View details
        </button>
        ${
        canCancel
            ? `<button type="button" class="mb-btn-small mb-btn-danger" data-cancel="${booking.bookingId}">
                 Cancel booking
               </button>`
            : ""
    }
        <button type="button" class="mb-btn-small mb-btn-secondary" data-ticket="${booking.bookingId}">
          Show ticket
        </button>
      </div>
    </article>
  `;
}

/* -------- Actions on cards -------- */

function setupCardActions() {
    document.querySelectorAll("[data-event-id]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const eventId = btn.getAttribute("data-event-id");
            if (!eventId) return;
            window.location.href = `/event-details/${encodeURIComponent(eventId)}`;
        });
    });

    document.querySelectorAll("[data-ticket]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-ticket");
            if (!id) return;
            // Z. B. PDF / QR-Ticket
            window.location.href = `/booking-ticket/${encodeURIComponent(id)}`;
        });
    });

    document.querySelectorAll("[data-cancel]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-cancel");
            if (!id) return;

            // Show prompt for cancellation reason
            const reason = prompt("Do you really want to cancel this booking?\n\nPlease enter the reason for cancellation:");

            if (reason === null || reason.trim() === "") {
                if (reason !== null) {
                    alert("Please enter a reason for cancellation.");
                }
                return;
            }

            fetch(`/api/bookings/${encodeURIComponent(id)}/cancel`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ cancelReason: reason.trim() })
            })
                .then((res) => {
                    if (!res.ok) throw new Error("Cancellation failed");
                    // lokale Daten anpassen
                    const b = allBookings.find((x) => String(x.bookingId) === String(id));
                    if (b) {
                        b.status = "CANCELLED";
                    }
                    applyFilters();
                    alert("Booking cancelled successfully.");
                })
                .catch((err) => {
                    console.error(err);
                    alert("The booking could not be cancelled. Please try again.");
                });
        });
    });
}

/* -------- Filter-Tabs -------- */

function setupFilterTabs() {
    document.querySelectorAll(".filter-tab").forEach((tab) => {
        tab.addEventListener("click", () => {
            const value = tab.getAttribute("data-filter") || "all";
            currentFilter = value;

            document.querySelectorAll(".filter-tab").forEach((t) => {
                t.classList.toggle("filter-tab--active", t === tab);
            });

            applyFilters();
        });
    });
}

/* -------- Navigation -------- */

function setupNavigation() {
    const btnDash = document.getElementById("btnToDashboard");
    const btnProfile = document.getElementById("btnToProfile");
    const btnLogout = document.getElementById("btnLogout");

    if (btnDash) {
        btnDash.addEventListener("click", () => {
            window.location.href = "/dashboard";
        });
    }

    if (btnProfile) {
        btnProfile.addEventListener("click", () => {
            window.location.href = "/user-profile";
        });
    }

    if (btnLogout) {
        btnLogout.addEventListener("click", () => {
            try {
                localStorage.removeItem("simplyevents_currentUser");
            } catch (_) {}
            window.location.href = "/login";
        });
    }
}

/* -------- Init -------- */

document.addEventListener("DOMContentLoaded", () => {
    setupFilterTabs();
    setupNavigation();

    const searchInput = document.getElementById("searchInput");
    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }

    loadBookings();
});
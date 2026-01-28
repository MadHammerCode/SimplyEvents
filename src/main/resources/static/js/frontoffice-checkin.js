let currentEventId = null;
let currentEventData = null;
let allBookings = [];
let filteredBookings = [];

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

/* -------- Bookings ---------- */

function loadBookings(eventId) {
    const tableBody = document.getElementById("bookingTableBody");
    const empty = document.getElementById("tableEmpty");

    if (tableBody) tableBody.innerHTML = "";
    if (empty) empty.textContent = "Bookings are being loaded...";

    currentEventId = eventId;
    allBookings = [];
    filteredBookings = [];

    if (!eventId) {
        if (empty) {
            empty.textContent = "No bookings found. Select an event or adjust your filters.";
        }
        updateStats();
        return;
    }


    fetch(`/api/events/${encodeURIComponent(eventId)}`)
        .then((res) => {
            if (!res.ok) throw new Error("Event data could not be loaded");
            return res.json();
        })
        .then((eventData) => {
            currentEventData = eventData;
            console.log('Event data loaded:', eventData);
        })
        .catch((err) => {
            console.error("Failed to load event data:", err);
            currentEventData = null;
        });


    fetch(`/api/checkin/event/${encodeURIComponent(eventId)}/bookings`)
        .then((res) => {
            if (!res.ok) throw new Error("Bookings could not be loaded");
            return res.json();
        })
        .then((data) => {
            if (!Array.isArray(data)) throw new Error("Unexpected booking format");
            allBookings = data;
            applyFilters();
        })
        .catch((err) => {
            console.error(err);
            if (empty) {
                empty.textContent =
                    "Bookings could not be loaded. Please try again or contact the back office.";
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

    filteredBookings = allBookings.filter((b) => {
        const totalSeats = Number(b.numParticipants || 0);
        const checked = Number(b.participantsCheckedIn || 0);
        const openSeats = Math.max(0, totalSeats - checked);

        if (onlyOpen && openSeats <= 0) return false;

        if (!searchTerm) return true;

        const haystack = [
            b.bookingNumber || "",
            b.bookerFirstName || "",
            b.bookerLastName || "",
            b.bookerEmail || ""
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

    const total = allBookings.reduce((sum, b) => sum + Number(b.numParticipants || 0), 0);
    const checked = allBookings.reduce((sum, b) => sum + Number(b.participantsCheckedIn || 0), 0);
    const open = Math.max(0, total - checked);

    if (statTotal) statTotal.textContent = String(total);
    if (statCheckedIn) statCheckedIn.textContent = String(checked);
    if (statOpen) statOpen.textContent = String(open);
}

/* -------- Tabelle ---------- */

function renderTable() {
    const tbody = document.getElementById("bookingTableBody");
    const empty = document.getElementById("tableEmpty");
    if (!tbody || !empty) return;

    if (!currentEventId) {
        tbody.innerHTML = "";
        empty.textContent = "No bookings found. Select an event or adjust your filters.";
        empty.classList.remove("hidden");
        return;
    }

    if (!filteredBookings.length) {
        tbody.innerHTML = "";
        empty.textContent = "No bookings match the current filters.";
        empty.classList.remove("hidden");
        return;
    }

    empty.classList.add("hidden");

    const rows = filteredBookings.map((b) => {
        const bookerName = `${b.bookerFirstName || ""} ${b.bookerLastName || ""}`.trim() || "–";
        const email = b.bookerEmail || "–";
        const seats = Number(b.numParticipants || 0);
        const checked = Number(b.participantsCheckedIn || 0);
        const openSeats = Math.max(0, seats - checked);

        const statusClass = openSeats <= 0 ? "status-pill--checked" : "status-pill--open";
        const btnLabel = openSeats <= 0 ? "View" : "Check-in";

        // LOGIC: Show Check-Out button if at least 1 person is checked in
        const canCheckOut = checked > 0;

        return `
      <tr data-id="${b.bookingId}">
        <td>${escapeHtml(b.bookingNumber || "–")}</td>
        <td>${escapeHtml(bookerName)}</td>
        <td>${escapeHtml(email)}</td>
        <td>${escapeHtml(String(seats))}</td>
        <td>
          <span class="status-pill ${statusClass}">${escapeHtml(String(checked))}/${escapeHtml(String(seats))}</span>
        </td>
        <td>
          <div class="action-buttons">
            <button type="button"
                    class="btn-small ${openSeats <= 0 ? "btn-small--reset" : "btn-small--checkin"}"
                    data-checkin-booking="${b.bookingId}">
              ${btnLabel}
            </button>
            
            ${canCheckOut ? `
            <button type="button" 
                    class="btn-small btn-small--warning"
                    title="Check out all participants"
                    style="background-color: #f59e0b; color: white; border: none;"
                    data-checkout-booking="${b.bookingId}">
              Check-out
            </button>` : ''}
            
            <button type="button"
                    class="btn-small btn-small--invoice"
                    data-invoice-booking="${b.bookingId}"
                    title="Create invoice for this booking">
              💰 Invoice
            </button>

            <button type="button"
                    class="btn-small btn-small--delete"
                    data-delete-booking="${b.bookingId}">
              Delete
            </button>
          </div>
        </td>
      </tr>
    `;
    });

    tbody.innerHTML = rows.join("");
    setupRowActions();
}

/* -------- Check-In Booking ---------- */

const checkinModal = {
    modal: null,
    form: null,
    list: null,
    subtitle: null,
    errorBox: null,
    submitBtn: null,
    addBtn: null,
    currentBooking: null,
    remainingSeats: 0,

    init() {
        this.modal = document.getElementById("checkinModal");
        this.form = document.getElementById("checkinForm");
        this.list = document.getElementById("participantFields");
        this.subtitle = document.getElementById("checkinModalSubtitle");
        this.errorBox = document.getElementById("checkinFormError");
        this.submitBtn = document.getElementById("checkinSubmitBtn");
        this.addBtn = document.getElementById("addSeatBtn");

        if (!this.modal || !this.form || !this.list || !this.subtitle || !this.submitBtn) return;

        this.form.addEventListener("submit", (e) => {
            e.preventDefault();
            this.handleSubmit();
        });

        this.modal.querySelectorAll("[data-close-modal]").forEach((btn) =>
            btn.addEventListener("click", () => this.close())
        );

        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape" && !this.modal.classList.contains("hidden")) {
                this.close();
            }
        });

        if (this.addBtn) {
            this.addBtn.addEventListener("click", () => this.addSeat());
        }

        this.list?.addEventListener("click", (event) => {
            const removeBtn = event.target.closest("[data-seat-remove]");
            if (removeBtn) {
                this.removeSeat(removeBtn.closest(".fo-seat-row"));
            }
        });
    },

    open(booking, existingParticipants = []) {
        if (!this.modal) return;
        this.currentBooking = booking;
        confirmedSeatCount = Number(booking.numParticipants || 0);
        desiredSeatCount = confirmedSeatCount;
        this.assignExisting(existingParticipants);
        this.toggleError(null);
        this.modal.classList.remove("hidden");
        this.updateAddState();
    },

    assignExisting(existingParticipants) {
        const rowsNeeded = Math.max(existingParticipants.length, confirmedSeatCount, 1);
        this.renderSeatRows(rowsNeeded);
        if (existingParticipants.length) {
            this.fillSeats(existingParticipants);
        }
        this.updateSubtitle();
        this.syncSeatLabel();
    },

    close() {
        if (!this.modal) return;
        this.modal.classList.add("hidden");
        this.list.innerHTML = "";
        this.currentBooking = null;
        this.remainingSeats = 0;
        this.toggleError(null);
        this.setSubmitting(false);
    },

    renderSeatRows(count) {
        this.list.innerHTML = "";
        for (let i = 0; i < count; i++) {
            this.appendSeatRow();
        }
    },

    fillSeats(participants) {
        const rows = this.list.querySelectorAll(".fo-seat-row");
        participants.forEach((p, index) => {
            const row = rows[index];
            if (!row) return;
            row.querySelector("[data-seat-first]").value = p.firstName || "";
            row.querySelector("[data-seat-last]").value = p.lastName || "";
            row.querySelector("[data-seat-email]").value = p.participantEmail || "";
        });
    },

    addSeat() {
        const seats = Number(this.currentBooking?.numParticipants || 0);
        const currentCount = this.list?.querySelectorAll(".fo-seat-row").length || 0;
        if (currentCount >= seats) {
            this.toggleError("No more seats available for this booking.");
            return;
        }
        this.appendSeatRow();
        this.updateSubtitle();
        this.updateAddState();
    },

    removeSeat(row) {
        if (!row) return;
        row.remove();
        this.remainingSeats += 1;
        this.updateBadges();
        this.updateSubtitle();
        this.updateAddState();
        this.toggleError(null);
    },

    appendSeatRow() {
        const template = document.getElementById("seatRowTemplate");
        if (!template) return;
        const clone = template.content.firstElementChild.cloneNode(true);
        this.list.appendChild(clone);
        this.updateBadges();
    },

    updateBadges() {
        this.list.querySelectorAll(".fo-seat-row__badge").forEach((badge, index) => {
            badge.textContent = index + 1;
        });
    },

    updateSubtitle() {
        if (!this.currentBooking) return;
        const baseName = this.currentBooking.bookerFirstName || "Guest";
        const seatsUsed = this.list.querySelectorAll(".fo-seat-row").length;
        const openSeats = Math.max(0, desiredSeatCount - seatsUsed);
        this.subtitle.textContent = `${baseName} · ${seatsUsed} participant(s), ${openSeats} seat(s) left`;
        this.syncSeatLabel();
    },

    syncSeatLabel() {
        const seatLabel = document.getElementById("seatCount");
        if (seatLabel) {
            seatLabel.textContent = String(desiredSeatCount);
        }
    },

    updateAddState() {
        if (this.addBtn) {
            const currentCount = this.list?.querySelectorAll(".fo-seat-row").length || 0;
            this.addBtn.disabled = currentCount >= desiredSeatCount;
        }
        const minusBtn = document.getElementById("seatMinus");
        const plusBtn = document.getElementById("seatPlus");
        if (minusBtn) minusBtn.disabled = desiredSeatCount <= 1;
        if (plusBtn) plusBtn.disabled = false;
    },

    collectParticipants() {
        const rows = this.list?.querySelectorAll(".fo-seat-row") || [];
        if (!rows.length) {
            this.toggleError("Please add at least one participant.");
            return null;
        }

        if (rows.length > desiredSeatCount) {
            this.toggleError(`Too many participants. Capacity: ${desiredSeatCount}`);
            return null;
        }

        const participants = [];
        for (const row of rows) {
            const first = row.querySelector("[data-seat-first]")?.value.trim();
            const last = row.querySelector("[data-seat-last]")?.value.trim();
            const email = row.querySelector("[data-seat-email]")?.value.trim();

            if (!first || !last || !email) {
                this.toggleError("Please fill out first name, last name and email for all participants.");
                return null;
            }
            participants.push({ firstName: first, lastName: last, email });
        }
        return participants;
    },

    toggleError(msg) {
        if (!this.errorBox) return;
        if (!msg) {
            this.errorBox.classList.add("hidden");
            this.errorBox.textContent = "";
        } else {
            this.errorBox.classList.remove("hidden");
            this.errorBox.textContent = msg;
        }
    },

    setSubmitting(isSubmitting) {
        if (this.submitBtn) {
            this.submitBtn.disabled = isSubmitting;
            this.submitBtn.textContent = isSubmitting ? "Saving…" : "Save check-in";
        }
    },

    async handleSubmit() {
        if (!this.currentBooking) return;
        const participants = this.collectParticipants();
        if (!participants) return;

        this.setSubmitting(true);
        try {
            await fetch(`/api/checkin/bookings/${encodeURIComponent(this.currentBooking.bookingId)}/participants`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(participants)
            }).then((res) => {
                if (!res.ok) {
                    return res.text().then((text) => {
                        throw new Error(text || "Check-in failed");
                    });
                }
            });

            this.close();
            loadBookings(currentEventId);
        } catch (err) {
            console.error(err);
            this.toggleError(err.message || "Check-in failed. Please try again.");
            this.setSubmitting(false);
        }
    }
};

function setupRowActions() {
    document.querySelectorAll("[data-checkin-booking]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const bookingId = btn.getAttribute("data-checkin-booking");
            if (!bookingId) return;
            const booking = allBookings.find((b) => String(b.bookingId) === String(bookingId));
            if (!booking) return;

            loadBookingParticipants(booking.bookingId)
                .then((participants) => {
                    checkinModal.open(booking, participants);
                })
                .catch((err) => {
                    console.error(err);
                    alert("Participants could not be loaded. Please try again.");
                });
        });
    });

    document.querySelectorAll("[data-delete-booking]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const bookingId = btn.getAttribute("data-delete-booking");
            if (!bookingId) return;
            if (!confirm("Delete this booking and free the seats?")) return;

            fetch(`/api/checkin/bookings/${encodeURIComponent(bookingId)}`, {
                method: "DELETE"
            })
                .then((res) => {
                    if (!res.ok) {
                        return res.text().then((text) => {
                            throw new Error(text || "Deletion failed");
                        });
                    }
                })
                .then(() => {
                    allBookings = allBookings.filter((b) => String(b.bookingId) !== String(bookingId));
                    applyFilters();
                })
                .catch((err) => {
                    alert(err.message || "Booking could not be deleted.");
                });
        });
    });

    // Invoice button handlers
    document.querySelectorAll("[data-invoice-booking]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const bookingId = btn.getAttribute("data-invoice-booking");
            if (!bookingId) return;

            const booking = allBookings.find((b) => String(b.bookingId) === String(bookingId));
            if (!booking) return;

            const eventSelect = document.getElementById("eventSelect");
            const eventId = eventSelect?.value;
            if (!eventId) {
                alert("No event selected");
                return;
            }


            const eventTitle = currentEventData?.title || `Event #${eventId}`;
            const eventPrice = currentEventData?.price || 0;

            const eventData = {
                id: eventId,
                title: eventTitle,
                price: eventPrice,
                organizer_id: currentEventData?.organizerId || 1
            };

            const bookingData = {
                first_name: booking.bookerFirstName || "",
                last_name: booking.bookerLastName || "",
                email: booking.bookerEmail || "",
                seats: booking.numParticipants || 1,
                booking_number: booking.bookingNumber || ""
            };

            // Open invoice modal with prefilled data
            if (window.openInvoiceModal) {
                window.openInvoiceModal(bookingData, eventData);
            } else {
                alert("Invoice modal not initialized");
            }
        });
    });

    document.querySelectorAll("[data-checkout-booking]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const bookingId = btn.getAttribute("data-checkout-booking");
            if (!bookingId) return;

            const booking = allBookings.find((b) => String(b.bookingId) === String(bookingId));
            if (!booking) return;

            // 1. Confirm Action
            if (!confirm(`Check out ${booking.bookerFirstName} ${booking.bookerLastName} and generate invoice?`)) return;

            // 2. Perform Checkout (API Call)
            fetch(`/api/checkin/bookings/${encodeURIComponent(bookingId)}/checkout`, {
                method: "POST"
            })
                .then((res) => {
                    if (!res.ok) throw new Error("Check-out failed");

                    // 3. Prepare Data for Invoice Modal
                    const bookingData = {
                        first_name: booking.bookerFirstName || "",
                        last_name: booking.bookerLastName || "",
                        email: booking.bookerEmail || "",
                        seats: booking.numParticipants || 1,
                        booking_number: booking.bookingNumber || ""
                    };

                    const eventData = {
                        id: currentEventData.id,
                        title: currentEventData.title,
                        price: currentEventData.price,
                        organizer_id: currentEventData.organizerId || 1
                    };

                    // 4. Open the Invoice Creation Modal
                    // (Defined in frontoffice-invoice.js)
                    if (window.openInvoiceModal) {
                        window.openInvoiceModal(bookingData, eventData);
                    } else {
                        alert("Invoice system not loaded.");
                    }

                    // 5. Refresh the list to update status (green -> grey)
                    loadBookings(currentEventId);
                })
                .catch((err) => {
                    console.error(err);
                    alert("Error during checkout: " + err.message);
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
        infoEl.textContent = "Please select an event to display bookings.";
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
    const goToAdminDashboard = document.getElementById("goToAdminDashboard");
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

    if (goToAdminDashboard) {
        goToAdminDashboard.addEventListener("click", () => {
            window.location.href = "/admin-dashboard";
        });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            handleLogout();
        });
    }

    filterNavigationMenuItems();
}


function filterNavigationMenuItems() {
    getCurrentUserRole().then((userRole) => {
        if (!userRole) {
            window.location.href = "/login";
            return;
        }

        const isAdmin = userRole === "ADMIN";
        const isFrontoffice = userRole === "FRONTOFFICE";
        const isBackoffice = userRole === "BACKOFFICE";

        const showIf = (id, condition) => {
            const el = document.getElementById(id);
            if (el) {
                if (condition) {
                    el.classList.remove("hidden");
                } else {
                    el.classList.add("hidden");
                }
            }
        };



        showIf("goToDashboard", true); // All can go to main dashboard
        showIf("goToBackoffice", isAdmin || isBackoffice);
        showIf("goToAdminDashboard", isAdmin);
    }).catch((err) => {
        console.error("Error filtering navigation:", err);
        window.location.href = "/login";
    });
}

/* -------- Init ---------- */

document.addEventListener("DOMContentLoaded", () => {
    if (!window.getCurrentUserRole) {
        const script = document.createElement('script');
        script.src = '/js/role-utils.js';
        script.onload = () => {
            setupNavigation();
            loadEventsForSelect();
        };
        script.onerror = () => {
            console.error('Failed to load role-utils.js');
            window.location.href = '/login';
        };
        document.head.appendChild(script);
    } else {
        setupNavigation();
        loadEventsForSelect();
    }

    const eventSelect = document.getElementById("eventSelect");
    const filterOnlyOpen = document.getElementById("filterOnlyOpen");
    const searchInput = document.getElementById("searchInput");

    if (eventSelect) {
        eventSelect.addEventListener("change", () => {
            updateEventMetaFromSelect();
            const value = eventSelect.value || "";
            loadBookings(value || null);
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
    checkinModal.init();
    setupSeatControls();
    setupNewBookingForm();
    setupInvoiceModalDropdown();
});

function loadBookingParticipants(bookingId) {
    return fetch(`/api/checkin/bookings/${encodeURIComponent(bookingId)}/participants`).then((res) => {
        if (!res.ok) {
            throw new Error("Participants could not be loaded");
        }
        return res.json();
    });
}

let desiredSeatCount = 0;
let confirmedSeatCount = 0;
let pendingTrim = false;

async function updateCapacity(bookingId, targetSeats) {
    const res = await fetch(`/api/checkin/bookings/${encodeURIComponent(bookingId)}/capacity`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ requestedSeats: targetSeats })
    });
    if (!res.ok) {
        const message = await res.text();
        throw new Error(message || "Seat adjustment failed");
    }
    return res.json();
}

function setupSeatControls() {
    const minus = document.getElementById("seatMinus");
    const plus = document.getElementById("seatPlus");
    minus?.addEventListener("click", () => adjustSeats(-1));
    plus?.addEventListener("click", () => adjustSeats(1));
}

async function adjustSeats(delta) {
    if (!checkinModal.currentBooking) return;
    const target = desiredSeatCount + delta;
    if (target < 1) return;

    const currentRows = checkinModal.list?.querySelectorAll(".fo-seat-row") || [];
    if (delta < 0 && target < currentRows.length) {
        const confirm = window.confirm("Reducing seats will discard extra participants. Continue?");
        if (!confirm) return;
        pendingTrim = true;
    }

    try {
        const updated = await updateCapacity(checkinModal.currentBooking.bookingId, target);
        desiredSeatCount = updated.numParticipants;
        confirmedSeatCount = desiredSeatCount;
        if (pendingTrim) {
            trimRowsTo(desiredSeatCount);
            pendingTrim = false;
        } else if (delta > 0) {
            addRowsUntil(desiredSeatCount);
        }
        checkinModal.updateSubtitle();
        checkinModal.updateAddState();
        loadBookings(currentEventId);
    } catch (err) {
        alert(err.message);
    }
}

function trimRowsTo(limit) {
    const rows = [...(checkinModal.list?.querySelectorAll(".fo-seat-row") || [])];
    while (rows.length > limit) {
        const row = rows.pop();
        row?.remove();
    }
}

function addRowsUntil(limit) {
    const list = checkinModal.list;
    if (!list) return;
    while ((list.querySelectorAll(".fo-seat-row").length || 0) < limit) {
        checkinModal.appendSeatRow();
    }
}

let newBookingForm;
let newBookingError;

function setupNewBookingForm() {
    newBookingForm = document.getElementById("newBookingForm");
    newBookingError = document.getElementById("newBookingError");
    if (!newBookingForm) return;

    newBookingForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (!currentEventId) {
            showNewBookingError("Please select an event first.");
            return;
        }
        const first = document.getElementById("newBookingFirst").value.trim();
        const last = document.getElementById("newBookingLast").value.trim();
        const email = document.getElementById("newBookingEmail").value.trim();
        const seats = Number(document.getElementById("newBookingSeats").value);
        if (!first || !last || !email || !seats || seats < 1) {
            showNewBookingError("Fill all fields with valid values.");
            return;
        }
        toggleNewBookingForm(true);
        try {
            const res = await fetch(`/api/checkin/event/${encodeURIComponent(currentEventId)}/bookings`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ firstName: first, lastName: last, email, seats })
            });
            if (!res.ok) {
                const msg = await res.text();
                throw new Error(msg || "Booking creation failed");
            }
            hideNewBookingError();
            newBookingForm.reset();
            loadBookings(currentEventId);
        } catch (err) {
            showNewBookingError(err.message);
        } finally {
            toggleNewBookingForm(false);
        }
    });
}

function showNewBookingError(message) {
    if (!newBookingError) return;
    newBookingError.textContent = message;
    newBookingError.classList.remove("hidden");
}

function hideNewBookingError() {
    if (!newBookingError) return;
    newBookingError.textContent = "";
    newBookingError.classList.add("hidden");
}

function toggleNewBookingForm(disabled) {
    if (!newBookingForm) return;
    [...newBookingForm.elements].forEach((el) => (el.disabled = disabled));
}

/* -------- Invoice Modal Dropdown ---------- */

function setupInvoiceModalDropdown() {
    const invoiceActionsToggle = document.getElementById("invoiceActionsToggle");
    const invoiceActionsMenu = document.getElementById("invoiceActionsMenu");

    if (!invoiceActionsToggle || !invoiceActionsMenu) return;

    // Toggle menu on button click
    invoiceActionsToggle.addEventListener("click", (e) => {
        e.stopPropagation();
        invoiceActionsMenu.classList.toggle("hidden");
        invoiceActionsToggle.setAttribute("aria-expanded",
            invoiceActionsMenu.classList.contains("hidden") ? "false" : "true");
    });

    // Close menu when clicking outside
    document.addEventListener("click", (e) => {
        if (!invoiceActionsToggle.contains(e.target) && !invoiceActionsMenu.contains(e.target)) {
            invoiceActionsMenu.classList.add("hidden");
            invoiceActionsToggle.setAttribute("aria-expanded", "false");
        }
    });

    // Handle action items
    const printBtn = document.getElementById("invoicePrint");

    if (printBtn) {
        printBtn.addEventListener("click", () => {
            window.print();
            invoiceActionsMenu.classList.add("hidden");
            invoiceActionsToggle.setAttribute("aria-expanded", "false");
        });
    }

}


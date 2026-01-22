// Helpers

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function getQueryParams() {
    const params = {};
    const qs = window.location.search.slice(1).split("&").filter(Boolean);
    for (const part of qs) {
        const [k, v] = part.split("=");
        if (!k) continue;
        params[decodeURIComponent(k)] = decodeURIComponent(v || "");
    }
    return params;
}

function setStep(step) {
    const step1 = document.getElementById("step1");
    const step2 = document.getElementById("step2");
    const step3 = document.getElementById("step3");
    const prevBtn = document.getElementById("prevButton");
    const nextBtn = document.getElementById("nextButton");
    const cancelBtn = document.getElementById("cancelButton");
    const dashboardBtn = document.getElementById("dashboardButton");

    if (!step1 || !step2 || !step3 || !prevBtn || !nextBtn || !cancelBtn) return;

    step1.classList.toggle("hidden", step !== 1);
    step2.classList.toggle("hidden", step !== 2);
    step3.classList.toggle("hidden", step !== 3);

    prevBtn.classList.toggle("hidden", step === 1 || step === 3);
    cancelBtn.classList.toggle("hidden", step === 3);
    if (dashboardBtn) {
        dashboardBtn.classList.toggle("hidden", step !== 3);
    }

    if (step === 3) {
        nextBtn.classList.add("hidden");
    } else if (step === 1) {
        nextBtn.classList.remove("hidden");
        nextBtn.textContent = "Next";
    } else if (step === 2) {
        nextBtn.classList.remove("hidden");
        nextBtn.textContent = "Book now";
    } else {
        nextBtn.classList.add("hidden");
    }

    // Step Indicators
    document.querySelectorAll(".step-indicator").forEach((el) => {
        const s = Number(el.getAttribute("data-step"));
        const circle = el.querySelector(".step-indicator__circle");
        if (!circle) return;

        el.classList.toggle("step-indicator--done", s < step);
        circle.classList.toggle("step-indicator__circle--active", s === step);
    });

    // Save in dataset for navigation logic
    document.body.dataset.currentStep = String(step);
}

function showError(messageOrList) {
    const errorBox = document.getElementById("errorBox");
    if (!errorBox) return;
    if (!messageOrList) {
        errorBox.classList.add("hidden");
        errorBox.innerHTML = "";
        return;
    }

    if (Array.isArray(messageOrList)) {
        const items = messageOrList.map((msg) => `<li>${escapeHtml(msg)}</li>`).join("");
        errorBox.innerHTML = `<strong>Please check your entries:</strong><ul>${items}</ul>`;
    } else {
        errorBox.innerHTML = escapeHtml(messageOrList);
    }

    errorBox.classList.remove("hidden");
}

function formatPrice(value) {
    if (value == null) return "–";
    const num = Number(value);
    if (isNaN(num)) return escapeHtml(String(value));
    return `${num.toFixed(2)} €`;
}

/* ------ Event load and Summary fill ------ */

let currentEvent = null;
let pendingBooking = null;
let activeBookingId = null;
let pendingRedirect = null;
let lastBookingResponse = null;

function loadEventAndPrefill() {
    const params = getQueryParams();
    const eventId = params.eventId;
    const participantsParam = params.participants;

    if (!eventId) {
        showError("No event ID was passed. Please return to the event overview.");
        return;
    }

    const ticketInput = document.getElementById("ticketCount");
    if (ticketInput && participantsParam && !isNaN(Number(participantsParam))) {
        ticketInput.value = String(Math.max(1, Number(participantsParam)));
    }

    fetch(`/api/events/${encodeURIComponent(eventId)}`)
        .then((res) => {
            if (!res.ok) {
                throw new Error("Event could not be loaded.");
            }
            return res.json();
        })
        .then((event) => {
            currentEvent = event;
            handleYearRoundLogic(event);
            fillSummary(event);
            updateTotalPrice();
        })
        .catch((err) => {
            console.error(err);
            showError("Oops – this event just couldn't be loaded. Please try again later.");
        });

    // Back link -> back to Event
    const backBtn = document.getElementById("backToEventButton");
    if (backBtn) {
        backBtn.addEventListener("click", () => {
            window.location.href = `/event-details/${encodeURIComponent(eventId)}`;
        });
    }
}

function handleYearRoundLogic(event) {
    const dateField = document.getElementById("yearRoundField");
    const dateInput = document.getElementById("attendanceDate");
    const summaryDate = document.getElementById("summaryEventDateTime");

    if (event.yearRound) {
        // Show Date Picker
        if (dateField) dateField.classList.remove("hidden");

        // Set Min Date to Today
        if (dateInput) {
            dateInput.min = new Date().toISOString().split("T")[0];

            // Update Summary when user picks a date
            dateInput.addEventListener("change", () => {
                if (summaryDate) summaryDate.textContent = dateInput.value || "Select a date";
            });
        }

        if (summaryDate) summaryDate.textContent = "Select a date";

    } else {
        // Standard Event: Hide Picker
        if (dateField) dateField.classList.add("hidden");
    }
}

function fillSummary(event) {
    const titleEl = document.getElementById("summaryEventTitle");
    const locEl = document.getElementById("summaryEventLocation");
    const dtEl = document.getElementById("summaryEventDateTime");
    const priceEl = document.getElementById("summaryTicketPrice");
    const availEl = document.getElementById("summaryAvailability");
    const cancelEl = document.getElementById("summaryCancellation");
    const ticketHint = document.getElementById("ticketHint");

    if (titleEl) titleEl.textContent = event.title || "Event";
    if (locEl) locEl.textContent = event.location || "–";

    if (dtEl) {
        if (event.yearRound) {
            // For year-round, show the selected date (or placeholder)
            const picked = document.getElementById("attendanceDate")?.value;
            dtEl.textContent = picked || "Select a date";
        } else {
            // For standard events, show the fixed date
            const date = event.date || "";
            const time = event.time || "";
            const dt = date ? (time ? `${date} · ${time}` : date) : "–";
            dtEl.textContent = dt;
        }
    }

    if (priceEl) priceEl.textContent = formatPrice(event.price);

    const available = event.availableSlots != null ? event.availableSlots : "-";
    const capacity = event.capacity != null ? event.capacity : "-";
    const soldOut = typeof event.availableSlots === "number" && event.availableSlots <= 0;
    if (availEl) {
        availEl.textContent = soldOut ? "Sold out" : `Available places: ${available} from ${capacity}`;
    }

    if (ticketHint) {
        ticketHint.textContent = soldOut
            ? "This event is fully booked."
            : event.availableSlots != null
                ? `Maximum available: ${event.availableSlots} Tickets.`
                : "";
    }

    if (soldOut) {
        document.body.dataset.bookingDisabled = "true";
        showError("This event is already sold out – booking is no longer possible.");
        const nextBtn = document.getElementById("nextButton");
        const cancelBtn = document.getElementById("cancelButton");
        if (nextBtn) {
            nextBtn.disabled = true;
            nextBtn.textContent = "Sold out";
        }
        if (cancelBtn) {
            cancelBtn.textContent = "Back to event";
        }
    }

    if (cancelEl) {
        if (event.cancellationDeadline) {
            cancelEl.textContent = `Cancellation policy: Cancellation possible until ${event.cancellationDeadline}.`;
        } else {
            cancelEl.textContent = "Cancellation conditions: Please note the information provided by the organizer.";
        }
    }
}

function updateTotalPrice() {
    const ticketInput = document.getElementById("ticketCount");
    const ticketCountEl = document.getElementById("summaryTicketCount");
    const totalEl = document.getElementById("summaryTotalPrice");

    if (!ticketInput || !ticketCountEl || !totalEl || !currentEvent) return;

    let count = Number(ticketInput.value);
    if (!count || count < 1) count = 1;

    // optional: Limit to availableSlots
    if (currentEvent.availableSlots != null && count > currentEvent.availableSlots) {
        count = currentEvent.availableSlots;
        ticketInput.value = String(count);
    }

    ticketCountEl.textContent = String(count);

    const price = Number(currentEvent.price);
    if (!isNaN(price)) {
        const total = price * count;
        totalEl.textContent = `${total.toFixed(2)} €`;
    } else {
        totalEl.textContent = "–";
    }
}

/* ------ Validierung ------ */

function validateStep1() {
    const errors = [];
    const firstName = document.getElementById("firstName")?.value.trim();
    const lastName = document.getElementById("lastName")?.value.trim();
    const email = document.getElementById("email")?.value.trim();
    const ticketCountVal = document.getElementById("ticketCount")?.value;

    if (!firstName) errors.push("First name must not be empty.");
    if (!lastName) errors.push("Last name must not be empty.");
    if (!email) {
        errors.push("E-Mail must not be empty.");
    } else if (!/^\S+@\S+\.\S+$/.test(email)) {
        errors.push("Please enter a valid email address.");
    }

    if (currentEvent && currentEvent.yearRound) {
        const dateInput = document.getElementById("attendanceDate");
        if (!dateInput || !dateInput.value) {
            errors.push("Please select a date for your visit.");
        }
    }

    const num = Number(ticketCountVal);
    if (!num || num < 1) {
        errors.push("The number of tickets must be at least 1.");
    }

    if (currentEvent && currentEvent.availableSlots != null && num > currentEvent.availableSlots) {
        errors.push(`There are only ${currentEvent.availableSlots} Places available.`);
    }

    if (errors.length) {
        showError(errors);
        return false;
    }

    showError(null);
    return true;
}

function validateStep2() {
    const method = document.querySelector('input[name="paymentMethod"]:checked');
    if (!method) {
        showError("Please choose a payment method.");
        return false;
    }
    showError(null);
    return true;
}

/* ------ Booking API Call ------ */

function submitBooking() {
    const params = getQueryParams();
    const eventId = params.eventId;
    if (!eventId) {
        showError("Event ID is missing. Please return to the event overview.");
        return Promise.reject();
    }

    const firstName = document.getElementById("firstName")?.value.trim();
    const lastName = document.getElementById("lastName")?.value.trim();
    const email = document.getElementById("email")?.value.trim();
    const phone = document.getElementById("phone")?.value.trim();
    const ticketCountVal = document.getElementById("ticketCount")?.value;

    const numParticipants = Number(ticketCountVal) || 1;

    let attendanceDate = null;
    if (currentEvent && currentEvent.yearRound) {
        attendanceDate = document.getElementById("attendanceDate").value;
    }

    const payload = {
        eventId: Number(eventId),
        firstName,
        lastName,
        email,
        phone,
        numParticipants,
        attendanceDate
    };

    return fetch("/api/bookings", {
         method: "POST",
         headers: {
             "Content-Type": "application/json"
         },
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
            } else if (typeof body === "string") {
                showError(body);
            } else {
                showError("The booking could not be made. Please check your entries.");
            }
            throw new Error("Booking failed");
         }

        showError(null);
        return res.json();
     });
 }

 function confirmPendingBooking() {
    if (!pendingBooking) {
        return Promise.reject(new Error("No pending booking"));
    }

    const paymentMethodInput = document.querySelector('input[name="paymentMethod"]:checked');
    const paymentMethod = paymentMethodInput ? paymentMethodInput.value : null;

    if (!paymentMethod) {
        showError("Please choose a payment method.");
        return Promise.reject(new Error("Payment method missing"));
    }

    const payload = {
        pendingId: pendingBooking.pendingId,
        paymentMethod
    };

    return fetch("/api/bookings/confirm", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    }).then(async (res) => {
        if (!res.ok) {
            const body = await res.text();
            showError(body || "Booking confirmation failed.");
            throw new Error("Confirmation failed");
        }
        showError(null);
        return res.json();
    });
}

/* ------ Fill in confirmation ------ */

 function fillConfirmation(bookingResponse) {
    const bNumEl = document.getElementById("confirmBookingNumber");
    const titleEl = document.getElementById("confirmEventTitle");
    const dateEl = document.getElementById("confirmEventDate");
    const timeEl = document.getElementById("confirmEventTime");
    const locEl = document.getElementById("confirmEventLocation");
    const numEl = document.getElementById("confirmNumParticipants");
    const totalEl = document.getElementById("confirmPriceTotal");

    if (!bookingResponse) return;

    if (bNumEl) bNumEl.textContent = bookingResponse.bookingNumber || "–";
    if (titleEl) titleEl.textContent = bookingResponse.eventTitle || (currentEvent?.title || "–");
     if (dateEl) dateEl.textContent = bookingResponse.date || (bookingResponse.attendanceDate) || (currentEvent?.date || "–");    if (timeEl) timeEl.textContent = bookingResponse.time || (currentEvent?.time || "–");
    if (locEl) locEl.textContent = bookingResponse.location || (currentEvent?.location || "–");
    if (numEl) numEl.textContent = bookingResponse.numParticipants != null ? String(bookingResponse.numParticipants) : "–";

    if (totalEl) {
        if (bookingResponse.priceTotal != null) {
            const num = Number(bookingResponse.priceTotal);
            totalEl.textContent = isNaN(num) ? escapeHtml(String(bookingResponse.priceTotal)) : `${num.toFixed(2)} €`;
        } else if (currentEvent) {
            const tickets = Number(document.getElementById("ticketCount")?.value) || 1;
            const price = Number(currentEvent.price);
            if (!isNaN(price)) {
                const total = price * tickets;
                totalEl.textContent = `${total.toFixed(2)} €`;
            } else {
                totalEl.textContent = "–";
            }
        }
    }
}

function showPaymentBanner(status, paymentRef) {
    const banner = document.getElementById("paymentStatusBanner");
    if (!banner) return;

    if (!status) {
        banner.classList.add("hidden");
        banner.textContent = "";
        paymentRef && banner.removeAttribute("data-ref");
        return;
    }

    let text = "";
    let cssClass = "status-banner--info";

    switch (status.toUpperCase()) {
        case "PAID":
            text = "Payment successful. Thank you!";
            cssClass = "status-banner--success";
            break;
        case "PAYMENT_FAILED":
            text = "Payment failed. Please try again.";
            cssClass = "status-banner--error";
            break;
        case "REFUNDED":
            text = "Payment refunded. Amount has been returned.";
            cssClass = "status-banner--warning";
            break;
        case "PENDING_PAYMENT":
        default:
            text = "Payment is still pending.";
            cssClass = "status-banner--info";
            break;
    }

    banner.textContent = paymentRef ? `${text} (Ref: ${paymentRef})` : text;
    banner.className = `status-banner ${cssClass}`;
    banner.classList.remove("hidden");
}

function handleCallbackParams() {
    const params = getQueryParams();
    const bookingId = params.bookingId ? Number(params.bookingId) : null;
    const status = params.status;
    const paymentRef = params.paymentRef;

    if (!bookingId) return;

    activeBookingId = bookingId;
    showPaymentBanner(status, paymentRef);
    setStep(3);
    toggleNavButtons(3);

    fetch(`/api/bookings/${bookingId}`)
        .then((res) => (res.ok ? res.json() : null))
        .then((booking) => {
            if (!booking) return;
            lastBookingResponse = {
                bookingNumber: booking.bookingNumber,
                eventTitle: booking.eventTitle,
                date: booking.date,
                time: booking.time,
                location: booking.location,
                numParticipants: booking.numParticipants,
                priceTotal: booking.priceTotal,
                attendanceDate: booking.attendanceDate
            };
            fillConfirmation(lastBookingResponse);
        })
        .catch(() => {
            // ignore fetch failure, banner already present
        });
}

function toggleNavButtons(step) {
    const nextBtn = document.getElementById("nextButton");
    const payBtn = document.getElementById("payButton");
    if (!nextBtn || !payBtn) return;

    const showPay = step === 2 && !!pendingBooking;

    payBtn.classList.toggle("hidden", !showPay);
    nextBtn.classList.toggle("hidden", showPay);

    if (step === 2) {
        payBtn.disabled = !pendingBooking;
    }
}

async function startPayment() {
    const payBtn = document.getElementById("payButton");
    if (!payBtn || !pendingBooking) {
        return;
    }

    payBtn.disabled = true;
    payBtn.textContent = "Preparing…";

    confirmPendingBooking()
        .then((bookingResponse) => {
            activeBookingId = bookingResponse.bookingId;
            lastBookingResponse = bookingResponse;
            return fetch(`/api/payments/start/${activeBookingId}`, { method: "POST" });
        })
        .then(async (res) => {
            if (!res.ok) {
                throw new Error("Payment could not be started");
            }
            const data = await res.json();
            pendingRedirect = data.payUrl;
            window.location.href = data.payUrl;
        })
        .catch((err) => {
            alert(err.message || "Payment could not be started");
            payBtn.disabled = false;
            payBtn.textContent = "Pay now";
        });
}

/* ------ Navigation ------ */

function setupNavigation() {
    const nextBtn = document.getElementById("nextButton");
    const prevBtn = document.getElementById("prevButton");
    const cancelBtn = document.getElementById("cancelButton");
    const dashboardBtn = document.getElementById("dashboardButton");
    const ticketInput = document.getElementById("ticketCount");
    const payBtn = document.getElementById("payButton");

    if (ticketInput) {
        ticketInput.addEventListener("input", () => {
            if (Number(ticketInput.value) < 1) {
                ticketInput.value = "1";
            }
            updateTotalPrice();
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", () => {
            if (document.body.dataset.bookingDisabled === "true") {
                return;
            }
            const currentStep = Number(document.body.dataset.currentStep || "1");

            if (currentStep === 1) {
                if (!validateStep1()) return;
                setStep(2);
                toggleNavButtons(2);
                pendingBooking = null;
                activeBookingId = null;
                const payButton = document.getElementById("payButton");
                if (payButton) {
                    payButton.disabled = true;
                }
            } else if (currentStep === 2) {
                if (!validateStep2()) return;

                nextBtn.disabled = true;
                nextBtn.textContent = "Creating hold…";

                submitBooking()
                    .then((pendingResponse) => {
                        pendingBooking = pendingResponse;
                        toggleNavButtons(2);
                        const payButton = document.getElementById("payButton");
                        if (payButton) {
                            payButton.disabled = false;
                        }
                        showError(null);
                    })
                    .catch(() => {
                        // error already shown
                    })
                    .finally(() => {
                        nextBtn.disabled = false;
                        nextBtn.textContent = "Book now";
                    });
            }
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", () => {
            const currentStep = Number(document.body.dataset.currentStep || "1");
            if (currentStep === 2) {
                setStep(1);
                toggleNavButtons(1);
            } else if (currentStep === 3) {
                setStep(2);
                toggleNavButtons(2);
            }
        });
    }

    if (dashboardBtn) {
        dashboardBtn.addEventListener("click", () => {
            window.location.href = "/dashboard";
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", () => {
            // Abbrechen → zurück zur Event-Details oder Dashboard
            const params = getQueryParams();
            if (params.eventId) {
                window.location.href = `/event-details/${encodeURIComponent(params.eventId)}`;
            } else {
                window.location.href = "/dashboard";
            }
        });
    }

    if (payBtn) {
        payBtn.addEventListener("click", () => {
            startPayment();
        });
    }
}

/* ------ Init ------ */

document.addEventListener("DOMContentLoaded", () => {
    document.body.dataset.currentStep = "1";
    setStep(1);
    setupNavigation();
    loadEventAndPrefill();
    handleCallbackParams();
});

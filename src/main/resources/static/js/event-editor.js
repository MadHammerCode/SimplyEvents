let editingId = null;
let uploadedImagePath = null;
let saveMode = "planned";

function getEventIdFromUrl() {
    const url = window.location.pathname;
    if (url.includes("/edit-event/")) {
        return url.split("/edit-event/")[1];
    }
    return null;
}

function escapeHtml(str) {
    if (str == null) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function showError(msgs) {
    const box = document.getElementById("errorBox");
    if (!box) return;

    if (!msgs) {
        box.classList.add("hidden");
        box.innerHTML = "";
        return;
    }

    box.classList.remove("hidden");

    if (Array.isArray(msgs)) {
        box.innerHTML = msgs.map((m) => `<div>${escapeHtml(m)}</div>`).join("");
    } else {
        box.innerHTML = escapeHtml(msgs);
    }
}

/* ---------- UI Toggles (Year Round) ---------- */

function toggleYearRound() {
    const yearRoundCheckbox = document.getElementById("yearRound");
    const dateWrapper = document.getElementById("dateSpecificFields");
    const dateInput = document.getElementById("date");
    const timeInput = document.getElementById("time");
    const capacityLabel = document.getElementById("capacityLabel");
    const capacityHelp = document.getElementById("capacityHelp");

    // Safety check in case elements are missing
    if (!yearRoundCheckbox || !dateWrapper) return;

    if (yearRoundCheckbox.checked) {
        // CASE: Year Round Event
        dateWrapper.style.display = "none";

        // Remove browser validation
        dateInput.removeAttribute("required");
        timeInput.removeAttribute("required");

        dateInput.value = "";
        timeInput.value = "";

        // Update UI text
        if (capacityLabel) capacityLabel.innerHTML = 'Daily Capacity Limit <span class="optional-note">(required)</span>';
        if (capacityHelp) capacityHelp.innerText = "Maximum people allowed per day.";
    } else {
        // CASE: Specific Date Event
        dateWrapper.style.display = "block";

        // Restore browser validation
        dateInput.setAttribute("required", "true");
        timeInput.setAttribute("required", "true");

        // Restore UI text
        if (capacityLabel) capacityLabel.innerHTML = 'Capacity <span class="optional-note">(required for publish)</span>';
        if (capacityHelp) capacityHelp.innerText = "Total number of people allowed.";
    }
}

/* ---------- Load Event if Editing ---------- */

function loadEvent(id) {
    fetch(`/api/events/backoffice/${id}`)
        .then((res) => {
            if (!res.ok) {
                return res.text().then(text => {
                    throw new Error(text || "Event could not be loaded");
                });
            }
            return res.json();
        })
        .then((ev) => {
            document.getElementById("title").value = ev.title || "";
            document.getElementById("category").value = ev.category || "";
            document.getElementById("price").value = ev.price ?? "";
            // ...
        })
        .catch((err) => {
            console.error(err);
            showError([err.message]);
        });
}

/* ---------- Validate ---------- */

function validateForm(mode) {
    const errors = [];
    const title = document.getElementById("title").value.trim();
    const category = document.getElementById("category").value;
    const location = document.getElementById("location").value.trim();
    const yearRound = document.getElementById("yearRound")?.checked;
    const date = document.getElementById("date").value;
    const time = document.getElementById("time").value;
    const capacity = document.getElementById("capacity").value;

    if (!title) errors.push("Title is required.");

    if (mode === "publish") {
        if (!category) errors.push("Category is required to publish.");
        if (!location) errors.push("Location is required to publish.");
        if (!capacity || Number(capacity) < 1) {
            errors.push("Capacity must be at least 1.");
        }


        const dateAndTimeProvided = date && time;
        const bookingStart = document.getElementById("bookingStart")?.value;
        const bookingEnd = document.getElementById("bookingEnd")?.value;
        const bookingWindowProvided = bookingStart && bookingEnd;

        if (!dateAndTimeProvided && !bookingWindowProvided && !yearRound) {
            errors.push("Provide date & time, or booking window, or enable year-round availability.");
        }

        if (bookingWindowProvided && bookingStart > bookingEnd) {
            errors.push("Booking window is invalid.");
        }
    }

    return errors;
}

function buildEventPayload(mode) {
    const capacityInput = document.getElementById("capacity");
    const capacityValue = capacityInput ? capacityInput.value.trim() : "";
    const capacity = capacityValue === "" ? null : Number(capacityValue);

    return {
        title: document.getElementById("title").value.trim(),
        category: document.getElementById("category").value,
        price: Number(document.getElementById("price").value || 0),
        location: document.getElementById("location").value.trim(),
        date: document.getElementById("date").value || null,
        time: document.getElementById("time").value || null,
        capacity:capacity,
        maxParticipants: capacity,
        description: document.getElementById("description").value.trim(),
        bookingStart: document.getElementById("bookingStart")?.value || null,
        bookingEnd: document.getElementById("bookingEnd")?.value || null,
        yearRound: document.getElementById("yearRound")?.checked || false,
        imagePath: uploadedImagePath,
        publishNow: mode === "publish",
        status: mode === "publish" ? "PUBLISHED" : "PLANNED"
    };
}

/* ---------- Save ---------- */

function saveEvent(mode) {
    const errors = validateForm(mode);
    if (errors.length) {
        showError(errors);
        return;
    }
    showError(null);

    const eventPayload = buildEventPayload(mode);
    const formData = new FormData();
    formData.append("event", new Blob([JSON.stringify(eventPayload)], { type: "application/json" }));

    const file = document.getElementById("imageFile").files[0];
    if (file) {
        formData.append("file", file);
    }

    const method = editingId ? "PUT" : "POST";
    const url = editingId ? `/api/events/${editingId}` : "/api/events";

    fetch(url, {
        method,
        body: formData
    })
        .then(async (res) => {

            if (!res.ok) {

                const contentType = res.headers.get("content-type");
                if (contentType && contentType.includes("application/json")) {
                    const data = await res.json();

                    if (Array.isArray(data)) {
                        throw new Error(data.join("<br>"));
                    }
                    throw new Error(typeof data === "string" ? data : "Event could not be saved.");
                } else {

                    const text = await res.text();
                    throw new Error(text || "Event could not be saved (Unknown Error).");
                }
            }
            // Success case
            return res.json().catch(() => ({}));
        })
        .then(() => {
            window.location.href = "/backoffice-dashboard";
        })
        .catch((err) => {
            console.error(err);
            showError(err.message);
        });
}

/* ---------- Delete ---------- */

function deleteEvent(id) {
    if (!confirm("Do you really want to delete this event?")) return;

    fetch(`/api/events/${id}`, { method: "DELETE" })
        .then((res) => {
            if (!res.ok) throw new Error("Delete failed");
            window.location.href = "/backoffice-dashboard";
        })
        .catch(() => {
            showError("Event could not be deleted.");
        });
}

/* ---------- Init ---------- */

document.addEventListener("DOMContentLoaded", () => {
    editingId = getEventIdFromUrl();

    // Year Round toggle
    const yearRoundCheckbox = document.getElementById("yearRound");
    if (yearRoundCheckbox) {
        toggleYearRound();
        yearRoundCheckbox.addEventListener("change", toggleYearRound);
    }

    // Back
    document.getElementById("btnBack").addEventListener("click", () => {
        window.location.href = "/backoffice-dashboard";
    });

    // Delete
    document.getElementById("btnDelete").addEventListener("click", () => {
        if (editingId) deleteEvent(editingId);
    });

    // Preview image
    document.getElementById("imageFile").addEventListener("change", () => {
        const file = document.getElementById("imageFile").files[0];
        const preview = document.getElementById("imagePreview");
        const img = document.getElementById("previewImg");

        if (file) {
            img.src = URL.createObjectURL(file);
            preview.classList.remove("hidden");
        } else {
            preview.classList.add("hidden");
            img.src = "";
        }
    });

    // Save
    document.getElementById("btnSavePlanned").addEventListener("click", () => {
        saveMode = "planned";
        saveEvent("planned");
    });

    document.getElementById("btnPublish").addEventListener("click", () => {
        saveMode = "publish";
        saveEvent("publish");
    });

    // Load data if editing
    if (editingId) {
        document.getElementById("editorTitle").textContent = "Edit Event";
        loadEvent(editingId);
    }
});
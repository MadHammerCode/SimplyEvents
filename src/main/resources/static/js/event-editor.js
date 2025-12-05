let editingId = null;
let uploadedImagePath = null;

function getEventIdFromUrl() {
    // Optional: falls per Model-Attribute übergeben → window.eventId = {id}
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

/* ---------- Load Event if Editing ---------- */

function loadEvent(id) {
    fetch(`/api/events/${id}`)
        .then((res) => {
            if (!res.ok) throw new Error("Event could not be loaded");
            return res.json();
        })
        .then((ev) => {
            document.getElementById("title").value = ev.title || "";
            document.getElementById("category").value = ev.category || "";
            document.getElementById("price").value = ev.price ?? "";
            document.getElementById("location").value = ev.location || "";
            document.getElementById("date").value = ev.date || "";
            document.getElementById("time").value = ev.time || "";
            document.getElementById("capacity").value = ev.capacity ?? "";
            document.getElementById("description").value = ev.description || "";

            if (ev.imagePath) {
                uploadedImagePath = ev.imagePath;
                const preview = document.getElementById("imagePreview");
                const img = document.getElementById("previewImg");
                img.src = "/" + ev.imagePath;
                preview.classList.remove("hidden");
            }

            document.getElementById("editorTitle").textContent = "Edit event";
            document.getElementById("btnDelete").classList.remove("hidden");
        })
        .catch((err) => {
            console.error(err);
            showError(["Event could not be loaded."]);
        });
}

/* ---------- Image Upload ---------- */

function uploadImage() {
    const fileInput = document.getElementById("imageFile");
    const file = fileInput.files[0];
    if (!file) return Promise.resolve(null);

    const formData = new FormData();
    formData.append("file", file);

    return fetch("/api/uploads", {
        method: "POST",
        body: formData
    })
        .then((res) => {
            if (!res.ok) throw new Error("Image upload failed");
            return res.json();
        })
        .then((data) => {
            uploadedImagePath = data.path;
            return uploadedImagePath;
        });
}

/* ---------- Validate ---------- */

function validateForm() {
    const errors = [];
    const title = document.getElementById("title").value.trim();
    const category = document.getElementById("category").value;
    const location = document.getElementById("location").value.trim();
    const date = document.getElementById("date").value;
    const time = document.getElementById("time").value;
    const capacity = document.getElementById("capacity").value;

    if (!title) errors.push("Title is required.");
    if (!category) errors.push("Category is required.");
    if (!location) errors.push("Location is required.");
    if (!date) errors.push("Date is required.");
    if (!time) errors.push("Time is required.");
    if (!capacity || Number(capacity) < 1) errors.push("Capacity must be at least 1.");

    return errors;
}

/* ---------- Save ---------- */

function saveEvent() {
    const errors = validateForm();
    if (errors.length) {
        showError(errors);
        return;
    }
    showError(null);

    const body = {
        title: document.getElementById("title").value.trim(),
        category: document.getElementById("category").value,
        price: Number(document.getElementById("price").value || 0),
        location: document.getElementById("location").value.trim(),
        date: document.getElementById("date").value,
        time: document.getElementById("time").value,
        capacity: Number(document.getElementById("capacity").value),
        description: document.getElementById("description").value.trim(),
        imagePath: uploadedImagePath
    };

    const method = editingId ? "PUT" : "POST";
    const url = editingId ? `/api/events/${editingId}` : "/api/events";

    fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    })
        .then((res) => {
            if (!res.ok) throw new Error("Save failed");
            return res.json().catch(() => ({}));
        })
        .then(() => {
            window.location.href = "/backoffice-dashboard";
        })
        .catch((err) => {
            console.error(err);
            showError("Event could not be saved.");
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
    document.getElementById("btnSave").addEventListener("click", () => {
        uploadImage().finally(saveEvent);
    });

    // Load data if editing
    if (editingId) {
        document.getElementById("editorTitle").textContent = "Edit Event";
        loadEvent(editingId);
    }
});
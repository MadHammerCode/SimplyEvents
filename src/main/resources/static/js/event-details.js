const MOCK_REVIEWS = [];

let wishlist = new Set();
let currentUser = null;

function loadCurrentUser() {
    try {
        const raw = localStorage.getItem("simplyevents_currentUser");
        if (raw) {
            let user = JSON.parse(raw);
            if (typeof user === "string") user = JSON.parse(user);
            currentUser = user;
        }
    } catch (_) {
        currentUser = null;
    }
}

function getEventIdFromPath() {
    const parts = window.location.pathname.split("/").filter(Boolean);
    const id = parts[parts.length - 1];
    return id || null;
}

// FETCH Wishlist from Backend
async function loadWishlist(eventId) {
    if (!currentUser || !currentUser.id) return;
    try {
        const res = await fetch(`/api/wishlist?userId=${currentUser.id}`);
        if (res.ok) {
            const ids = await res.json();
            wishlist = new Set(ids);
            updateWishlistButton(eventId);
        }
    } catch (e) {
        console.error("Failed to load wishlist", e);
    }
}

// TOGGLE Wishlist via Backend
async function toggleWishlist(eventId) {
    if (!eventId) return;

    if (!currentUser || !currentUser.id) {
        alert("Please log in to use the wishlist.");
        return;
    }

    if (wishlist.has(Number(eventId))) {
        wishlist.delete(Number(eventId));
    } else {
        wishlist.add(Number(eventId));
    }
    updateWishlistButton(eventId);

    try {
        const res = await fetch(`/api/wishlist/${eventId}?userId=${currentUser.id}`, {
            method: 'POST'
        });
        if (!res.ok) {
            throw new Error("Failed");
        }
    } catch (e) {
        console.error(e);
        loadWishlist(eventId);
    }
}

function updateWishlistButton(eventId) {
    const wishlistBtn = document.getElementById("wishlistButton");
    const heart = document.getElementById("wishlistHeart");
    if (!wishlistBtn || !heart) return;

    const isInWishlist = wishlist.has(Number(eventId));
    if (isInWishlist) {
        heart.textContent = "♥";
        wishlistBtn.classList.add("icon-button--active");
    } else {
        heart.textContent = "♡";
        wishlistBtn.classList.remove("icon-button--active");
    }
}

function escapeHtml(str) {
    if (str == null) return "";
    return String(str).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

/* ---------- Rendering ---------- */

function renderEvent(event) {
    const titleEl = document.getElementById("eventTitle");
    const categoryEl = document.getElementById("eventCategory");
    const imageEl = document.getElementById("eventImage");
    const locationEl = document.getElementById("eventLocation");
    const dateEl = document.getElementById("eventDate");
    const timeEl = document.getElementById("eventTime");
    const capacityEl = document.getElementById("eventCapacity");
    const priceEl = document.getElementById("eventPrice");
    const descEl = document.getElementById("eventDescription");
    const bookingPriceEl = document.getElementById("bookingPrice");
    const bookingTotalEl = document.getElementById("bookingTotal");

    const imagePath = event.imagePath ? `/${event.imagePath}` : "/images/default-event.jpg";
    if (imageEl) {
        imageEl.src = imagePath;
        imageEl.alt = event.title || "Event Picture";
    }

    if (titleEl) titleEl.textContent = event.title || "Event";
    if (categoryEl) categoryEl.textContent = event.category || "Event";
    if (locationEl) locationEl.textContent = event.location || "–";
    if (descEl) descEl.textContent = event.description || "No description.";

    if (dateEl) dateEl.textContent = event.yearRound ? "Year round" : (event.date || "–");
    if (timeEl) timeEl.textContent = event.yearRound ? "Daily" : (event.time || "–");

    if (capacityEl) {
        const available = event.availableSlots ?? "-";
        const max = event.maxParticipants ?? "-";
        const soldOut = Number(available) <= 0;
        capacityEl.textContent = event.yearRound ? `${max} per day` : (soldOut ? "Sold out" : `${available} from ${max}`);
    }

    const priceText = event.price != null ? `${Number(event.price).toFixed(2)} €` : "–";
    if (priceEl) priceEl.textContent = priceText;
    if (bookingPriceEl) bookingPriceEl.textContent = priceText;

    if (bookingTotalEl && event.price != null) {
        const participantsInput = document.getElementById("participantsInput");
        const count = participantsInput ? Number(participantsInput.value) || 1 : 1;
        const total = Number(event.price) * count;
        if (!isNaN(total)) bookingTotalEl.textContent = `${total.toFixed(2)} €`;
    }
}

function renderReviews(reviews) {

}

function setupShareModal(eventId) {
    const shareModal = document.getElementById("shareModal");
    const shareButton = document.getElementById("shareButton");
    const shareCloseButton = document.getElementById("shareCloseButton");
    const shareInput = document.getElementById("shareLinkInput");

    if (!shareModal || !shareButton || !shareCloseButton || !shareInput) return;

    const url = `${window.location.origin}/event-details/${eventId}`;
    shareInput.value = url;

    shareButton.addEventListener("click", () => {
        shareModal.classList.remove("hidden");
    });
    shareCloseButton.addEventListener("click", () => {
        shareModal.classList.add("hidden");
    });
}

function setupBooking(event, eventId) {
    const participantsInput = document.getElementById("participantsInput");
    const bookingTotalEl = document.getElementById("bookingTotal");
    const bookNowButton = document.getElementById("bookNowButton");

    if (!participantsInput || !bookingTotalEl || !bookNowButton) return;

    const soldOut = typeof event.availableSlots === "number" && event.availableSlots <= 0;
    if (soldOut) {
        participantsInput.disabled = true;
        bookNowButton.disabled = true;
        bookNowButton.textContent = "Sold out";
        return;
    }

    participantsInput.addEventListener("input", () => {
        const count = Math.max(1, Number(participantsInput.value));
        const price = Number(event.price || 0);
        bookingTotalEl.textContent = `${(price * count).toFixed(2)} €`;
    });

    bookNowButton.addEventListener("click", () => {
        window.location.href = `/booking?eventId=${eventId}&participants=${participantsInput.value}`;
    });
}

function initEventDetailsPage() {
    const eventId = getEventIdFromPath();
    if (!eventId) return;

    loadCurrentUser();
    loadWishlist(eventId);

    const backButton = document.getElementById("backButton");
    if (backButton) backButton.addEventListener("click", () => window.history.back());

    const wishlistBtn = document.getElementById("wishlistButton");
    if (wishlistBtn) wishlistBtn.addEventListener("click", () => toggleWishlist(eventId));

    fetch(`/api/events/${encodeURIComponent(eventId)}`)
        .then(res => res.ok ? res.json() : Promise.reject())
        .then(event => {
            renderEvent(event);
            setupBooking(event, eventId);
            renderReviews(MOCK_REVIEWS);
            setupShareModal(eventId);
        })
        .catch(console.error);
}

document.addEventListener("DOMContentLoaded", initEventDetailsPage);
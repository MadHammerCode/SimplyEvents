// Einfache Mock-Bewertungen, bis das Bewertungssystem im Backend existiert
const MOCK_REVIEWS = [
    {
        id: 1,
        userName: "Anna M.",
        date: "12.10.2025",
        rating: 5,
        comment: "Super organisiert, tolle Atmosphäre und ein sehr freundliches Team!"
    },
    {
        id: 2,
        userName: "Lukas P.",
        date: "05.10.2025",
        rating: 4,
        comment: "Das Event war sehr gut, nur das Catering hätte besser sein können."
    },
    {
        id: 3,
        userName: "Sophie K.",
        date: "25.09.2025",
        rating: 5,
        comment: "Absolut empfehlenswert! Ich komme auf jeden Fall wieder."
    }
];

const WISHLIST_KEY = "simplyevents_wishlist";

let wishlist = new Set();

function loadWishlist() {
    try {
        const raw = window.localStorage.getItem(WISHLIST_KEY);
        if (!raw) return;
        const ids = JSON.parse(raw);
        if (Array.isArray(ids)) {
            wishlist = new Set(ids);
        }
    } catch (e) {
        // ignore
    }
}

function saveWishlist() {
    try {
        window.localStorage.setItem(WISHLIST_KEY, JSON.stringify(Array.from(wishlist)));
    } catch (e) {
        // ignore
    }
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

function getEventIdFromPath() {
    const parts = window.location.pathname.split("/").filter(Boolean);
    // Erwartetes Pattern: /event-details/{id}
    const id = parts[parts.length - 1];
    return id || null;
}

function formatPrice(price) {
    if (price == null) return "–";
    const num = Number(price);
    if (isNaN(num)) return escapeHtml(String(price));
    return `${num.toFixed(2)} €`;
}

function formatDate(dateStr) {
    if (!dateStr) return "–";
    // Backend liefert date vermutlich als String "2025-11-07" oder ähnlich
    return dateStr;
}

function formatTime(timeStr) {
    if (!timeStr) return "–";
    return timeStr;
}

/* ---------- Rendering-Funktionen ---------- */

function renderEvent(event) {
    // Hero
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
        imageEl.alt = event.title || "Event Bild";
    }

    if (titleEl) titleEl.textContent = event.title || "Event";
    if (categoryEl) categoryEl.textContent = event.category || "Event";
    if (locationEl) locationEl.textContent = event.location || "–";
    if (dateEl) dateEl.textContent = formatDate(event.date);
    if (timeEl) timeEl.textContent = formatTime(event.time);
    if (capacityEl) {
        const available = event.availableSlots != null ? event.availableSlots : "-";
        const max = event.maxParticipants != null ? event.maxParticipants : "-";
        capacityEl.textContent = `${available} von ${max}`;
    }

    const priceText = formatPrice(event.price);
    if (priceEl) priceEl.textContent = priceText;
    if (bookingPriceEl) bookingPriceEl.textContent = priceText;

    if (descEl) {
        descEl.textContent = event.description || "There is no description for this event yet.";
    }

    if (bookingTotalEl && event.price != null) {
        const participantsInput = document.getElementById("participantsInput");
        const count = participantsInput ? Number(participantsInput.value) || 1 : 1;
        const total = Number(event.price) * count;
        if (!isNaN(total)) {
            bookingTotalEl.textContent = `${total.toFixed(2)} €`;
        }
    }
}

function renderReviews(reviews) {
    const listEl = document.getElementById("reviewsList");
    const noReviewsEl = document.getElementById("noReviewsText");
    const avgEl = document.getElementById("reviewsAverage");
    const countEl = document.getElementById("reviewsCount");
    const heroRatingValueEl = document.getElementById("eventRatingValue");
    const heroRatingCountEl = document.getElementById("eventRatingCount");

    if (!listEl || !noReviewsEl || !avgEl || !countEl || !heroRatingValueEl || !heroRatingCountEl) {
        return;
    }

    if (!reviews.length) {
        listEl.innerHTML = "";
        noReviewsEl.classList.remove("hidden");
        avgEl.textContent = "0.0";
        countEl.textContent = "(0)";
        heroRatingValueEl.textContent = "0.0";
        heroRatingCountEl.textContent = "(0 Reviews)";
        return;
    }

    noReviewsEl.classList.add("hidden");

    let sum = 0;
    reviews.forEach(r => {
        sum += r.rating || 0;
    });
    const avg = sum / reviews.length;
    avgEl.textContent = avg.toFixed(1);
    countEl.textContent = `(${reviews.length})`;
    heroRatingValueEl.textContent = avg.toFixed(1);
    heroRatingCountEl.textContent = `(${reviews.length} Reviews)`;

    const items = reviews.map(r => {
        const stars = [];
        for (let i = 0; i < 5; i++) {
            const filled = i < r.rating;
            stars.push(
                `<span class="review__star ${filled ? "" : "review__star--empty"}">★</span>`
            );
        }

        return `
      <article class="review">
        <div class="review__header">
          <div>
            <p class="review__user">${escapeHtml(r.userName)}</p>
            <p class="review__date">${escapeHtml(r.date)}</p>
          </div>
          <div class="review__stars">
            ${stars.join("")}
          </div>
        </div>
        <p class="review__comment">${escapeHtml(r.comment)}</p>
      </article>
    `;
    });

    listEl.innerHTML = items.join("");
}

/* ---------- Wishlist & Share ---------- */

function updateWishlistButton(eventId) {
    const wishlistBtn = document.getElementById("wishlistButton");
    const heart = document.getElementById("wishlistHeart");
    if (!wishlistBtn || !heart) return;

    const isInWishlist = wishlist.has(eventId);
    if (isInWishlist) {
        heart.textContent = "♥";
        wishlistBtn.classList.add("icon-button--active");
    } else {
        heart.textContent = "♡";
        wishlistBtn.classList.remove("icon-button--active");
    }
}

function toggleWishlist(eventId) {
    if (!eventId) return;
    if (wishlist.has(eventId)) {
        wishlist.delete(eventId);
    } else {
        wishlist.add(eventId);
    }
    saveWishlist();
    updateWishlistButton(eventId);
}

function setupShareModal(eventId) {
    const shareModal = document.getElementById("shareModal");
    const shareButton = document.getElementById("shareButton");
    const shareCloseButton = document.getElementById("shareCloseButton");
    const shareInput = document.getElementById("shareLinkInput");

    if (!shareModal || !shareButton || !shareCloseButton || !shareInput) return;

    const url = `${window.location.origin}/event-details/${eventId}`;
    shareInput.value = url;

    function openModal() {
        shareModal.classList.remove("hidden");
        setTimeout(() => {
            shareInput.focus();
            shareInput.select();
        }, 50);
    }

    function closeModal() {
        shareModal.classList.add("hidden");
    }

    shareButton.addEventListener("click", openModal);
    shareCloseButton.addEventListener("click", closeModal);
    shareModal.addEventListener("click", (e) => {
        if (e.target === shareModal || e.target.classList.contains("modal__backdrop")) {
            closeModal();
        }
    });
}

/* ---------- Booking ---------- */

function setupBooking(event, eventId) {
    const participantsInput = document.getElementById("participantsInput");
    const bookingTotalEl = document.getElementById("bookingTotal");
    const bookNowButton = document.getElementById("bookNowButton");

    if (!participantsInput || !bookingTotalEl || !bookNowButton) return;

    function updateTotal() {
        const count = Number(participantsInput.value) || 1;
        const price = Number(event.price);
        if (!isNaN(price)) {
            const total = price * count;
            bookingTotalEl.textContent = `${total.toFixed(2)} €`;
        }
    }

    participantsInput.addEventListener("input", () => {
        if (Number(participantsInput.value) < 1) {
            participantsInput.value = "1";
        }
        updateTotal();
    });

    updateTotal();

    bookNowButton.addEventListener("click", () => {
        // Später kann hier der direkte Booking-API-Call hin.
        // Aktuell leiten wir zur (neu zu bauenden) Booking-Seite weiter.
        window.location.href = `/booking?eventId=${encodeURIComponent(eventId)}&participants=${encodeURIComponent(participantsInput.value)}`;
    });
}

/* ---------- Init ---------- */

function initEventDetailsPage() {
    const eventId = getEventIdFromPath();
    if (!eventId) {
        console.error("Could not read event ID from URL.");
        return;
    }

    loadWishlist();
    updateWishlistButton(eventId);

    const backButton = document.getElementById("backButton");
    if (backButton) {
        backButton.addEventListener("click", () => {
            if (window.history.length > 1) {
                window.history.back();
            } else {
                window.location.href = "/dashboard";
            }
        });
    }

    const wishlistBtn = document.getElementById("wishlistButton");
    if (wishlistBtn) {
        wishlistBtn.addEventListener("click", () => toggleWishlist(eventId));
    }

    // Event vom Backend holen
    fetch(`/api/events/${encodeURIComponent(eventId)}`)
        .then((res) => {
            if (!res.ok) {
                throw new Error("Event could not be loaded.");
            }
            return res.json();
        })
        .then((event) => {
            renderEvent(event);
            setupBooking(event, eventId);
            renderReviews(MOCK_REVIEWS);
            setupShareModal(eventId);
        })
        .catch((err) => {
            console.error(err);
            const titleEl = document.getElementById("eventTitle");
            const descEl = document.getElementById("eventDescription");
            if (titleEl) titleEl.textContent = "Event could not be loaded";
            if (descEl) {
                descEl.textContent =
                    "Oops – this event just couldn't be loaded. Please try again later or go back to the overview.";
            }
        });
}

document.addEventListener("DOMContentLoaded", initEventDetailsPage);
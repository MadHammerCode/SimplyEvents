let currentUser = null;
let wishlistIds = new Set();
let allEvents = [];

// 1. Get User
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

// 2. Escape HTML for safety
function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// 3. Main Load Logic
async function initWishlist() {
    loadCurrentUser();

    if (!currentUser || !currentUser.id) {
        window.location.href = "/login";
        return;
    }

    const grid = document.getElementById("eventsGrid");
    const empty = document.getElementById("emptyState");

    try {
        // A. Fetch Wishlist IDs
        const wishRes = await fetch(`/api/wishlist?userId=${currentUser.id}`);
        if (!wishRes.ok) throw new Error("Could not load wishlist");
        const ids = await wishRes.json();
        wishlistIds = new Set(ids);

        if (wishlistIds.size === 0) {
            grid.innerHTML = "";
            empty.classList.remove("hidden");
            return;
        }

        // B. Fetch All Events (or specific ones if you have an endpoint for that)
        // Optimization: In a real app, you'd have /api/events/batch?ids=...
        // For now, we fetch all and filter client-side to keep it simple with existing APIs
        const eventRes = await fetch("/api/events");
        if (!eventRes.ok) throw new Error("Could not load events");
        allEvents = await eventRes.json();

        // C. Filter
        const wishlistedEvents = allEvents.filter(ev => wishlistIds.has(ev.id));

        if (wishlistedEvents.length === 0) {
            grid.innerHTML = "";
            empty.classList.remove("hidden");
        } else {
            empty.classList.add("hidden");
            renderEvents(wishlistedEvents);
        }

    } catch (err) {
        console.error(err);
        grid.innerHTML = "<p>Error loading wishlist.</p>";
    }
}

// 4. Render Cards (Reused logic from Dashboard)
function renderEvents(events) {
    const grid = document.getElementById('eventsGrid');
    if (!grid) return;

    const cards = events.map(ev => {
        const id = ev.id;
        const title = escapeHtml(ev.title || '');
        const location = escapeHtml(ev.location || '');
        const description = escapeHtml((ev.description || '').slice(0, 110));
        const category = escapeHtml(ev.category || 'Other');
        const priceText = (ev.price != null) ? `${Number(ev.price).toFixed(2)} €` : 'Free';
        const imagePath = ev.imagePath ? `/${ev.imagePath}` : '/images/default-event.jpg';

        return `
      <article class="event-card" data-event-id="${id}">
        <div class="event-card__image-wrap">
          <img class="event-card__image" src="${imagePath}" alt="${title}">
          <button type="button" class="wishlist-btn is-active" aria-label="Remove from wishlist">
            <span class="wishlist-heart">♥</span>
          </button>
        </div>
        <div class="event-card__body">
          <div class="event-card__meta">
            <span class="event-card__category">${category}</span>
          </div>
          <h3 class="event-card__title">${title}</h3>
          <p class="event-card__location">${location}</p>
          <div class="event-card__footer">
            <span class="event-card__price">${priceText}</span>
            <button type="button" class="btn btn-sm view-details-btn">View details</button>
          </div>
        </div>
      </article>
    `;
    });

    grid.innerHTML = cards.join('');
}

// 5. Remove Logic
async function removeWishlist(card) {
    if (!confirm("Remove from wishlist?")) return;

    const id = card.getAttribute("data-event-id");
    card.remove(); // Optimistic remove

    try {
        await fetch(`/api/wishlist/${id}?userId=${currentUser.id}`, { method: "POST" });

        // Check if empty
        const grid = document.getElementById("eventsGrid");
        if (grid.children.length === 0) {
            document.getElementById("emptyState").classList.remove("hidden");
        }
    } catch (e) {
        console.error(e);
        alert("Could not remove item.");
    }
}

// 6. Event Listeners
document.addEventListener("DOMContentLoaded", () => {
    initWishlist();

    const grid = document.getElementById("eventsGrid");
    if (grid) {
        grid.addEventListener("click", (e) => {
            const target = e.target;

            // Remove Button
            const wishBtn = target.closest(".wishlist-btn");
            if (wishBtn) {
                const card = wishBtn.closest(".event-card");
                removeWishlist(card);
                return;
            }

            // View Details
            const detailsBtn = target.closest(".view-details-btn");
            if (detailsBtn) {
                const card = detailsBtn.closest(".event-card");
                const id = card.getAttribute("data-event-id");
                window.location.href = `/event-details/${id}`;
            }
        });
    }
});
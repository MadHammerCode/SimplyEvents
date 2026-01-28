// Simple state
let allEvents = [];
let selectedCategory = 'All';
let wishlist = new Set();
let currentUser = null;

// Helper to get logged-in user ID
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

// FETCH wishlist from Backend
async function loadWishlist() {
    if (!currentUser || !currentUser.id) {
        wishlist = new Set();
        return;
    }
    try {
        const res = await fetch(`/api/wishlist?userId=${currentUser.id}`);
        if (res.ok) {
            const ids = await res.json();
            wishlist = new Set(ids);
            renderEvents(allEvents); // Re-render to show hearts
        }
    } catch (e) {
        console.error("Failed to load wishlist", e);
    }
}

// TOGGLE via Backend
async function toggleWishlistForCard(cardEl) {
    if (!cardEl) return;
    const eventId = cardEl.getAttribute('data-event-id');
    if (!eventId) return;

    if (!currentUser || !currentUser.id) {
        alert("Please log in to use the wishlist.");
        return;
    }

    try {
        // Optimistic UI update
        if (wishlist.has(Number(eventId))) {
            wishlist.delete(Number(eventId));
        } else {
            wishlist.add(Number(eventId));
        }
        applyFilters();

        // Send to backend
        const res = await fetch(`/api/wishlist/${eventId}?userId=${currentUser.id}`, {
            method: 'POST'
        });

        if (!res.ok) {
            // Revert on failure
            console.error("Failed to save wishlist");
            loadWishlist();
        }
    } catch (e) {
        console.error(e);
        loadWishlist();
    }
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function computeCategories(events) {
    const set = new Set();
    events.forEach(ev => {
        if (ev.category && ev.category.trim() !== '') {
            set.add(ev.category.trim());
        }
    });
    return ['All', ...Array.from(set)];
}

function renderCategories(events) {
    const container = document.getElementById('categoryContainer');
    if (!container) return;

    const cats = computeCategories(events);
    container.innerHTML = '';

    cats.forEach(cat => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = cat;
        btn.className = 'chip' + (cat === selectedCategory ? ' chip--active' : '');
        btn.addEventListener('click', () => {
            selectedCategory = cat;
            renderCategories(allEvents);
            applyFilters();
        });
        container.appendChild(btn);
    });
}

function applyFilters() {
    const searchInput = document.getElementById('searchInput');
    const locationInput = document.getElementById('locationInput');
    const dateInput = document.getElementById('dateInput');

    const query = (searchInput?.value || '').toLowerCase().trim();
    const locationQuery = (locationInput?.value || '').toLowerCase().trim();
    const dateFilter = (dateInput?.value || '').trim();

    const filtered = allEvents.filter(ev => {
        const category = (ev.category || 'Other').trim();
        const matchesCategory = selectedCategory === 'All' || category === selectedCategory;

        const searchable = [
            ev.title || '',
            ev.description || '',
            ev.location || ''
        ].join(' ');

        const matchesSearch = searchable.toLowerCase().includes(query);
        const matchesLocation = !locationQuery || String(ev.location || '').toLowerCase().includes(locationQuery);
        const matchesDate = !dateFilter || String(ev.date || '').startsWith(dateFilter);

        return matchesCategory && matchesSearch && matchesLocation && matchesDate;
    });

    renderEvents(filtered);
    updateResultsHeader(filtered.length);
}

function updateResultsHeader(count) {
    const titleEl = document.getElementById('resultsTitle');
    const countEl = document.getElementById('resultsCount');

    if (titleEl) {
        titleEl.textContent = selectedCategory === 'All' ? 'All Events' : selectedCategory;
    }
    if (countEl) {
        countEl.textContent = String(count);
    }
}

function renderEvents(events) {
    const grid = document.getElementById('eventsGrid');
    const emptyState = document.getElementById('emptyState');

    if (!grid) return;

    if (!events.length) {
        grid.innerHTML = '';
        if (emptyState) emptyState.classList.remove('hidden');
        return;
    }

    if (emptyState) emptyState.classList.add('hidden');

    const cards = events.map(ev => {
        const id = ev.id;
        const title = escapeHtml(ev.title || '');
        const location = escapeHtml(ev.location || '');
        const description = escapeHtml((ev.description || '').slice(0, 110));
        const category = escapeHtml(ev.category || 'Other');
        const priceText = (ev.price != null) ? `${Number(ev.price).toFixed(2)} €` : 'Free';

        const isWishlisted = wishlist.has(id);

        const imagePath = ev.imagePath ? `/${ev.imagePath}` : '/images/default-event.jpg';
        const capacity = ev.capacity ?? ev.maxParticipants ?? null;
        const availableRaw = typeof ev.availableSlots === 'number' ? ev.availableSlots : capacity;
        const available = availableRaw != null ? Number(availableRaw) : null;
        const soldOut = available !== null && available <= 0;
        const slotsLabel = capacity != null ? (soldOut ? 'Sold out' : `${available} seats left`) : '';
        const slotClass = soldOut ? 'event-card__slots event-card__slots--empty' : 'event-card__slots';

        return `
      <article class="event-card${soldOut ? ' event-card--sold-out' : ''}" data-event-id="${id}">
        <div class="event-card__image-wrap">
          <img class="event-card__image" src="${imagePath}" alt="${title}">
          <button
            type="button"
            class="wishlist-btn ${isWishlisted ? 'is-active' : ''}"
            aria-label="Toggle wishlist"
          >
            <span class="wishlist-heart">♥</span>
          </button>
          ${soldOut ? '<span class="event-card__soldout-banner">Sold out</span>' : ''}
        </div>
        <div class="event-card__body">
          <div class="event-card__meta">
            <span class="event-card__category">${category}</span>
            ${capacity != null ? `<span class="${slotClass}">${slotsLabel}</span>` : ''}
          </div>
          <h3 class="event-card__title">${title}</h3>
          <p class="event-card__location">${location}</p>
          <p class="event-card__description">${description}</p>
          <div class="event-card__footer">
            <span class="event-card__price">${priceText}</span>
            <button type="button" class="btn btn-sm view-details-btn" ${soldOut ? 'data-soldout="true"' : ''}>
              ${soldOut ? 'View (sold out)' : 'View details'}
            </button>
          </div>
        </div>
      </article>
    `;
    });

    grid.innerHTML = cards.join('');
}

function setupEventDelegation() {
    const grid = document.getElementById('eventsGrid');
    if (!grid) return;

    grid.addEventListener('click', event => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) return;

        // Wishlist Button
        const wishlistBtn = target.closest('.wishlist-btn');
        if (wishlistBtn) {
            const card = wishlistBtn.closest('.event-card');
            toggleWishlistForCard(card);
            return;
        }

        // Details Button
        const detailsBtn = target.closest('.view-details-btn');
        if (detailsBtn) {
            const card = detailsBtn.closest('.event-card');
            if (!card) return;
            const id = card.getAttribute('data-event-id');
            if (id) {
                window.location.href = `/event-details/${id}`;
            }
        }
    });
}

function setupFilters() {
    const searchInput = document.getElementById('searchInput');
    const locationInput = document.getElementById('locationInput');
    const dateInput = document.getElementById('dateInput');

    if (searchInput) searchInput.addEventListener('input', applyFilters);
    if (locationInput) locationInput.addEventListener('input', applyFilters);
    if (dateInput) dateInput.addEventListener('change', applyFilters);
}

function fetchEvents() {
    fetch('/api/events')
        .then(res => res.ok ? res.json() : Promise.reject('Failed'))
        .then(data => {
            allEvents = data;
            renderCategories(allEvents);
            loadWishlist();
            applyFilters();
        })
        .catch(console.error);
}

document.addEventListener('DOMContentLoaded', () => {
    loadCurrentUser();
    setupEventDelegation();
    setupFilters();
    fetchEvents();
});
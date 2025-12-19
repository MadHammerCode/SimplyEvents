// Cleaned dashboard.js: fetch events and use server-side wishlist API instead of localStorage

let allEvents = [];
let selectedCategory = 'All';
let wishlist = new Set(); // stores string ids

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/'/g, '&#39;')
        .replace(/"/g, '&quot;');
}

function computeCategories(events) {
    const set = new Set();
    events.forEach(ev => {
        if (ev && ev.category) {
            try { set.add(ev.category.trim()); } catch (e) { /* ignore */ }
        }
    });
    return ['All', ...Array.from(set)];
}

function renderCategories(events) {
    const container = document.getElementById('categoryChips');
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
    const searchInput = document.getElementById('searchKeyword');
    const locationInput = document.getElementById('searchLocation');
    const dateInput = document.getElementById('searchDate');

    const query = (searchInput?.value || '').toLowerCase().trim();
    const locationQuery = (locationInput?.value || '').toLowerCase().trim();
    const dateFilter = (dateInput?.value || '').trim();

    const filtered = allEvents.filter(ev => {
        const category = (ev.category || 'Other').trim();
        const matchesCategory = selectedCategory === 'All' || category === selectedCategory;

        const searchable = [ev.title || '', ev.description || '', ev.location || ''].join(' ');
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

    if (titleEl) titleEl.textContent = selectedCategory === 'All' ? 'All Events' : selectedCategory;
    if (countEl) countEl.textContent = String(count);
}

function renderEvents(events) {
    const grid = document.getElementById('eventsGrid');
    const emptyState = document.getElementById('popularEmpty');
    if (!grid) return;

    if (!events.length) {
        grid.innerHTML = '';
        if (emptyState) emptyState.classList.remove('hidden');
        return;
    }
    if (emptyState) emptyState.classList.add('hidden');

    const cards = events.map(ev => {
        const id = ev.id || ev.eventId;
        const title = escapeHtml(ev.title || '');
        const location = escapeHtml(ev.location || '');
        const description = escapeHtml((ev.description || '').slice(0, 110));
        const date = ev.date || '';
        const time = ev.time || '';
        const dateTimeText = date ? (time ? `${date} · ${time}` : date) : '';
        const category = escapeHtml(ev.category || 'Other');

        let priceText = 'Free';
        if (ev.price !== null && ev.price !== undefined) {
            const num = Number(ev.price);
            priceText = isNaN(num) ? escapeHtml(String(ev.price)) : `${num.toFixed(2)} €`;
        }

        const isWishlisted = wishlist.has(String(id));
        const imagePath = ev.imagePath ? `/${ev.imagePath}` : '/images/default-event.jpg';

        return `
      <article class="event-card" data-event-id="${id}">
        <div class="event-card__image-wrap">
          <img class="event-card__image" src="${imagePath}" alt="${title}">
          <button
            type="button"
            class="wishlist-btn ${isWishlisted ? 'is-active' : ''}"
            aria-label="Toggle wishlist"
          >
            <span class="wishlist-heart">♥</span>
          </button>
        </div>
        <div class="event-card__body">
          <div class="event-card__meta">
            <span class="event-card__category">${category}</span>
            <span class="event-card__date">${escapeHtml(dateTimeText)}</span>
          </div>
          <h3 class="event-card__title">${title}</h3>
          <p class="event-card__location">${location}</p>
          <p class="event-card__description">${description}</p>
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

function toggleWishlistUI(btn, id) {
    if (!btn) return;
    btn.classList.toggle('is-active');
}

function setupEventDelegation() {
    const grid = document.getElementById('eventsGrid');
    if (!grid) return;

    grid.addEventListener('click', event => {
        const target = event.target;
        if (!(target instanceof HTMLElement)) return;

        const wishlistBtn = target.closest('.wishlist-btn');
        if (wishlistBtn) {
            const card = wishlistBtn.closest('.event-card');
            const id = card?.getAttribute('data-event-id');
            if (!id) return;

            // immediate UI feedback
            toggleWishlistUI(wishlistBtn, id);

            // optimistic toggle on server
            fetch('/api/wishlist/' + id, { method: 'POST' })
                .then(res => {
                    if (!res.ok) {
                        // revert UI if failed
                        toggleWishlistUI(wishlistBtn, id);
                    } else {
                        // update local set to match new state
                        if (wishlist.has(String(id))) {
                            wishlist.delete(String(id));
                        } else {
                            wishlist.add(String(id));
                        }
                    }
                })
                .catch(err => {
                    console.error('Wishlist toggle failed', err);
                    toggleWishlistUI(wishlistBtn, id);
                });
            return;
        }

        const detailsBtn = target.closest('.view-details-btn');
        if (detailsBtn) {
            const card = detailsBtn.closest('.event-card');
            const id = card?.getAttribute('data-event-id');
            if (id) {
                window.location.href = '/event-details/' + id;
            }
        }
    });
}

function fetchWishlistIds() {
    return fetch('/api/wishlist/ids')
        .then(res => {
            if (!res.ok) return [];
            return res.json();
        })
        .then(data => Array.isArray(data) ? data.map(String) : [] )
        .catch(err => {
            console.warn('Could not load wishlist ids', err);
            return [];
        });
}

function fetchEvents() {
    fetch('/api/events')
        .then(res => {
            if (!res.ok) throw new Error('Failed to load events');
            return res.json();
        })
        .then(data => {
            if (!Array.isArray(data)) throw new Error('Unexpected response format');
            allEvents = data;
            renderCategories(allEvents);
            applyFilters();
        })
        .catch(err => {
            console.error(err);
            const grid = document.getElementById('eventsGrid');
            const emptyState = document.getElementById('popularEmpty');
            if (grid) grid.innerHTML = '<p>Could not load events.</p>';
            if (emptyState) emptyState.classList.add('hidden');
        });
}

document.addEventListener('DOMContentLoaded', async () => {
    const ids = await fetchWishlistIds();
    wishlist = new Set(ids);
    setupEventDelegation();
    fetchEvents();
    // wire filters input listeners
    const searchInput = document.getElementById('searchKeyword');
    const locationInput = document.getElementById('searchLocation');
    const dateInput = document.getElementById('searchDate');
    if (searchInput) searchInput.addEventListener('input', applyFilters);
    if (locationInput) locationInput.addEventListener('input', applyFilters);
    if (dateInput) dateInput.addEventListener('change', applyFilters);
});
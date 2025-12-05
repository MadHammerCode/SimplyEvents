// Simple state
let allEvents = [];
let selectedCategory = 'All';
let wishlist = new Set();

const WISHLIST_KEY = 'simplyevents_wishlist';

function loadWishlist() {
    try {
        const raw = window.localStorage.getItem(WISHLIST_KEY);
        if (!raw) return;
        const arr = JSON.parse(raw);
        if (Array.isArray(arr)) {
            wishlist = new Set(arr);
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
        const matchesCategory =
            selectedCategory === 'All' || category === selectedCategory;

        const searchable = [
            ev.title || '',
            ev.description || '',
            ev.location || ''
        ].join(' ');

        const matchesSearch = searchable.toLowerCase().includes(query);

        const matchesLocation =
            !locationQuery ||
            String(ev.location || '').toLowerCase().includes(locationQuery);

        const matchesDate =
            !dateFilter || String(ev.date || '').startsWith(dateFilter);

        return matchesCategory && matchesSearch && matchesLocation && matchesDate;
    });

    renderEvents(filtered);
    updateResultsHeader(filtered.length);
}

function updateResultsHeader(count) {
    const titleEl = document.getElementById('resultsTitle');
    const countEl = document.getElementById('resultsCount');

    if (titleEl) {
        titleEl.textContent =
            selectedCategory === 'All' ? 'All Events' : selectedCategory;
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
        const date = ev.date || '';
        const time = ev.time || '';
        const dateTimeText = date ? (time ? `${date} · ${time}` : date) : '';
        const category = escapeHtml(ev.category || 'Other');

        let priceText = 'Free';
        if (ev.price !== null && ev.price !== undefined) {
            const num = Number(ev.price);
            priceText = isNaN(num)
                ? escapeHtml(String(ev.price))
                : `${num.toFixed(2)} €`;
        }

        const isWishlisted = wishlist.has(id);
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
            <button type="button" class="btn btn-sm view-details-btn">
              View details
            </button>
          </div>
        </div>
      </article>
    `;
    });

    grid.innerHTML = cards.join('');
}

function toggleWishlistForCard(cardEl) {
    if (!cardEl) return;
    const id = cardEl.getAttribute('data-event-id');
    if (!id) return;

    if (wishlist.has(id)) {
        wishlist.delete(id);
    } else {
        wishlist.add(id);
    }
    saveWishlist();
    applyFilters(); // re-render cards to update hearts
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

    if (searchInput) {
        searchInput.addEventListener('input', () => {
            applyFilters();
        });
    }

    if (locationInput) {
        locationInput.addEventListener('input', () => {
            applyFilters();
        });
    }

    if (dateInput) {
        dateInput.addEventListener('change', () => {
            applyFilters();
        });
    }
}

function fetchEvents() {
    fetch('/api/events')
        .then(res => {
            if (!res.ok) {
                throw new Error('Failed to load events');
            }
            return res.json();
        })
        .then(data => {
            if (!Array.isArray(data)) {
                throw new Error('Unexpected response format');
            }
            allEvents = data;
            renderCategories(allEvents);
            applyFilters();
        })
        .catch(err => {
            console.error(err);
            const grid = document.getElementById('eventsGrid');
            const emptyState = document.getElementById('emptyState');
            if (grid) {
                grid.innerHTML = '<p>Could not load events.</p>';
            }
            if (emptyState) {
                emptyState.classList.add('hidden');
            }
        });
}

document.addEventListener('DOMContentLoaded', () => {
    loadWishlist();
    setupEventDelegation();
    setupFilters();
    fetchEvents();
});
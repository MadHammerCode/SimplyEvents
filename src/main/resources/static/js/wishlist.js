// wishlist.js - renders the user's wishlist page and toggles items via the API

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/'/g, '&#39;')
        .replace(/"/g, '&quot;');
}

function renderWishlist(events) {
    const grid = document.getElementById('wishlistGrid');
    const empty = document.getElementById('wishlistEmpty');
    if (!grid) return;

    if (!events || !events.length) {
        grid.innerHTML = '';
        if (empty) empty.classList.remove('hidden');
        return;
    }
    if (empty) empty.classList.add('hidden');

    const cards = events.map(ev => {
        const id = ev.id;
        const title = escapeHtml(ev.title || '');
        const location = escapeHtml(ev.location || '');
        const description = escapeHtml((ev.description || '').slice(0, 150));
        const imagePath = ev.imagePath ? `/${ev.imagePath}` : '/images/default-event.jpg';
        const dateTime = ev.date ? ev.date + (ev.time ? ' · ' + ev.time : '') : '';

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
            <span class="event-card__date">${escapeHtml(dateTime)}</span>
          </div>
          <h3 class="event-card__title">${title}</h3>
          <p class="event-card__location">${location}</p>
          <p class="event-card__description">${description}</p>
          <div class="event-card__footer">
            <button type="button" class="btn btn-sm view-details-btn">View details</button>
          </div>
        </div>
      </article>
    `;
    });

    grid.innerHTML = cards.join('');
}

function setupDelegation() {
    const grid = document.getElementById('wishlistGrid');
    if (!grid) return;

    grid.addEventListener('click', ev => {
        const target = ev.target;
        if (!(target instanceof HTMLElement)) return;

        const wishlistBtn = target.closest('.wishlist-btn');
        if (wishlistBtn) {
            const card = wishlistBtn.closest('.event-card');
            const id = card?.getAttribute('data-event-id');
            if (!id) return;

            // optimistic remove from UI
            wishlistBtn.classList.toggle('is-active');
            // call server to toggle
            fetch('/api/wishlist/' + id, { method: 'POST' })
                .then(res => {
                    if (!res.ok) {
                        // revert UI
                        wishlistBtn.classList.toggle('is-active');
                    } else {
                        // remove card from view as it was unfavorited
                        card.remove();
                        // if grid empty show empty state
                        if (grid.children.length === 0) {
                            const empty = document.getElementById('wishlistEmpty');
                            if (empty) empty.classList.remove('hidden');
                        }
                    }
                })
                .catch(err => {
                    console.error('Failed to toggle wishlist', err);
                    wishlistBtn.classList.toggle('is-active');
                });
            return;
        }

        const detailsBtn = target.closest('.view-details-btn');
        if (detailsBtn) {
            const card = detailsBtn.closest('.event-card');
            const id = card?.getAttribute('data-event-id');
            if (id) window.location.href = '/event-details/' + id;
        }
    });
}

function loadWishlist() {
    fetch('/api/wishlist')
        .then(res => {
            if (!res.ok) throw new Error('Failed to load wishlist');
            return res.json();
        })
        .then(data => {
            if (!Array.isArray(data)) throw new Error('Unexpected response');
            renderWishlist(data);
            setupDelegation();
        })
        .catch(err => {
            console.error(err);
            const grid = document.getElementById('wishlistGrid');
            const empty = document.getElementById('wishlistEmpty');
            if (grid) grid.innerHTML = '<p>Could not load wishlist.</p>';
            if (empty) empty.classList.add('hidden');
        });
}

document.addEventListener('DOMContentLoaded', () => {
    loadWishlist();
});


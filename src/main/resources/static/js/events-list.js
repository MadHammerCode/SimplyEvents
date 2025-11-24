

(function () {

  let allEvents = [];
  let currentCategory = 'ALL';
  let currentSearchTerm = '';


  async function loadEvents() {
    const list = document.getElementById('eventList');
    const empty = document.getElementById('eventEmptyState');

    if (!list) {
      return;
    }

    try {
      const res = await fetch('/api/events');
      if (!res.ok) throw new Error('Loading failed: ' + res.status);
      const events = await res.json();

      allEvents = events || [];
      renderEvents(allEvents);
    } catch (err) {
      console.error(err);
    }
  }

  function renderEvents(eventsToShow) {
    const list = document.getElementById('eventList');
    const empty = document.getElementById('eventEmptyState');

    if (!list) return;

    list.innerHTML = '';

    if (!eventsToShow || eventsToShow.length === 0) {
      if (empty) empty.style.display = 'block';
      return;
    }
    if (empty) empty.style.display = 'none';

    for (const ev of eventsToShow) {
      const li = document.createElement('li');
      li.innerHTML = `
        <strong>${ev.title}</strong><br/>
        <small>${ev.date ?? ''} ${ev.time ?? ''} &ndash; ${ev.location ?? ''}</small><br/>
        <small>Preis: ${ev.price != null ? ev.price + ' €' : 'n/a'}, Plätze: ${ev.availableSlots ?? ev.maxParticipants ?? 'n/a'}</small>
      `;

      const eventId = ev.id ?? ev.eventId;
      console.log('Loaded event from /api/events:', ev, '-> using id:', eventId);

      li.addEventListener('click', () => {
        if (!eventId) {
          console.warn('Kein Event-ID-Feld im Event-Objekt gefunden:', ev);
          return;
        }
        if (typeof window.openEventDetail === 'function') {
          window.openEventDetail(eventId);
        } else {
          console.error('openEventDetail is not defined on window');
        }
      });

      list.appendChild(li);
    }
  }

  function applyFilters() {
    const lower = currentSearchTerm.toLowerCase();

    const filtered = allEvents.filter(ev => {
      const title = (ev.title || '').toLowerCase();
      const location = (ev.location || '').toLowerCase();
      const category = (ev.category || '').toLowerCase();
      const description = (ev.description || '').toLowerCase();

      const matchesSearch =
        !lower ||
        title.includes(lower) ||
        location.includes(lower) ||
        category.includes(lower) ||
        description.includes(lower);

      const matchesCategory =
        currentCategory === 'ALL' ||
        category === currentCategory.toLowerCase();

      return matchesSearch && matchesCategory;
    });

    renderEvents(filtered);
  }


  window.loadEvents = loadEvents;


  document.addEventListener('DOMContentLoaded', function () {
    loadEvents();

    const searchForm = document.getElementById('searchForm');
    const searchInput = document.getElementById('searchInput');

    if (searchForm && searchInput) {
      searchForm.addEventListener('submit', (e) => {
        e.preventDefault();
        currentSearchTerm = searchInput.value.trim();
        applyFilters();
      });

      searchInput.addEventListener('input', () => {
        currentSearchTerm = searchInput.value.trim();
        applyFilters();
      });
    }

    const catButtons = document.querySelectorAll('.cat-btn');
    catButtons.forEach(btn => {
      btn.addEventListener('click', () => {
        currentCategory = btn.dataset.category || 'ALL';

        catButtons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        applyFilters();
      });
    });
  });

})();
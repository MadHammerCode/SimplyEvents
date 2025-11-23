

(function () {

  let allEvents = [];


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

  function applySearch(term) {
    const lower = term.toLowerCase();

    const filtered = allEvents.filter(ev => {
      const title = (ev.title || '').toLowerCase();
      const location = (ev.location || '').toLowerCase();
      const category = (ev.category || '').toLowerCase();
      const description = (ev.description || '').toLowerCase();

      return (
        title.includes(lower) ||
        location.includes(lower) ||
        category.includes(lower) ||
        description.includes(lower)
      );
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
        const term = searchInput.value.trim();
        if (!term) {
          renderEvents(allEvents);
        } else {
          applySearch(term);
        }
      });

      searchInput.addEventListener('input', () => {
        const term = searchInput.value.trim();
        if (!term) {
          renderEvents(allEvents);
        } else {
          applySearch(term);
        }
      });
    }
  });

})();
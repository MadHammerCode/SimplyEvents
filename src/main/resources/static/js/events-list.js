
(function () {


  async function loadEvents() {
    const list = document.getElementById('eventList');
    const empty = document.getElementById('eventEmptyState');


    if (!list) {
      return;
    }

    try {
      const res = await fetch('/api/events');
      if (!res.ok) throw new Error('Laden fehlgeschlagen: ' + res.status);
      const events = await res.json();

      list.innerHTML = '';
      if (!events || events.length === 0) {
        if (empty) empty.style.display = 'block';
        return;
      }
      if (empty) empty.style.display = 'none';

      for (const ev of events) {
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
    } catch (err) {
      console.error(err);
    }
  }


  window.loadEvents = loadEvents;


  document.addEventListener('DOMContentLoaded', function () {
    loadEvents();
  });

})();
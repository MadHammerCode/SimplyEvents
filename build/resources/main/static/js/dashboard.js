document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('createEventForm');
  const modal = document.getElementById('createEventModal');
  const list = document.getElementById('eventList');
  const empty = document.getElementById('eventEmptyState');

  async function loadEvents() {
    if (!list) return;

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
          openEventDetail(eventId);
        });

        list.appendChild(li);
      }
    } catch (err) {
      console.error(err);
    }
  }

  async function openEventDetail(eventId) {
    try {
      console.log('Opening event details for id:', eventId, 'URL:', `/api/events/${eventId}`);
      const res = await fetch(`/api/events/${eventId}`);
      if (!res.ok) throw new Error("Failed to load event details");

      const ev = await res.json();


      document.getElementById("detailTitle").textContent = ev.title;
      document.getElementById("detailDate").textContent = ev.date;
      document.getElementById("detailTime").textContent = ev.time;
      document.getElementById("detailLocation").textContent = ev.location;
      document.getElementById("detailPrice").textContent = ev.price + " €";
      document.getElementById("detailCategory").textContent = ev.category;
      document.getElementById("detailDescription").textContent = ev.description;
      document.getElementById("detailMin").textContent = ev.minParticipants;
      document.getElementById("detailMax").textContent = ev.maxParticipants;
      document.getElementById("detailSlots").textContent = ev.availableSlots;
      document.getElementById("detailEquipment").textContent = ev.equipmentNeeded;
      document.getElementById("detailRequirements").textContent = ev.requirements;
      document.getElementById("detailCancel").textContent = ev.cancellationDeadline;


      const modal = document.getElementById("eventDetailModal");
      modal.classList.add("open");
      modal.setAttribute("aria-hidden", "false");
    } catch (err) {
      console.error(err);
      alert("Could not load event details.");
    }
  }

  if (form) {
    form.addEventListener('submit', async function (e) {
      e.preventDefault();

      form.classList.add('was-submitted');

      const cancelDateTime = form.cancelDeadline.value || null;

      const eventData = {
        title: form.title.value.trim(),
        date: form.date.value,
        time: form.time.value,
        location: form.location.value.trim(),
        price: form.price.value ? Number(form.price.value) : null,
        description: form.description.value.trim(),

        category: form.category.value.trim() || null,
        minParticipants: form.minParticipants.value
          ? Number(form.minParticipants.value)
          : null,
        maxParticipants: form.maxParticipants.value
          ? Number(form.maxParticipants.value)
          : null,

        durationHours: form.durationHours.value
          ? Number(form.durationHours.value)
          : null,
        equipmentNeeded: form.equipmentNeeded.value.trim() || null,
        requirements: form.requirements.value.trim() || null,

        cancellationDeadline: cancelDateTime
      };


      if (
        !eventData.title ||
        !eventData.date ||
        !eventData.time ||
        !eventData.location ||
        eventData.price === null ||
        eventData.maxParticipants === null
      ) {
        alert('Please fill in all required fields (*).');
        return;
      }

      try {
        const res = await fetch('/api/events', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(eventData)
        });

        if (!res.ok) {
          const msg = await res.text().catch(() => '');
          throw new Error('Error saving: ' + (msg || res.status));
        }

        const saved = await res.json();
        alert('Save Event: ' + saved.title);
        form.reset();

        // Modal schließen, falls vorhanden
        if (modal) {
          modal.classList.remove('open');
          modal.setAttribute('aria-hidden', 'true');
        }

        await loadEvents();
      } catch (err) {
        console.error(err);
        alert('Save failed. Please try again later.');
      }
    });
  }


  loadEvents();
});
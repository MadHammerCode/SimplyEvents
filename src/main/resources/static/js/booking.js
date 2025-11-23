// booking.js
document.addEventListener('DOMContentLoaded', async () => {
  const params = new URLSearchParams(window.location.search);
  const eventId = params.get('eventId');

  const eventInfoDiv = document.getElementById('bookingEventInfo');
  const form = document.getElementById('bookingForm');
  const bookingNumberEl = document.getElementById('bookingNumber');
  const resultBox = document.getElementById('bookingResult');

  // Event-Infos laden
  if (eventId) {
    document.getElementById('eventId').value = eventId;

    try {
      const res = await fetch(`/api/events/${eventId}`);
      if (res.ok) {
        const ev = await res.json();
        eventInfoDiv.innerHTML = `
          <h2>${ev.title}</h2>
          <p>${ev.date} · ${ev.time} · ${ev.location}</p>
          <p>Price per participant: ${ev.price != null ? ev.price + ' €' : 'Free'}</p>
        `;
         const numInput = document.getElementById('numParticipants');
              if (ev.availableSlots != null) {
                numInput.max = ev.availableSlots;
              } else if (ev.maxParticipants != null) {
                numInput.max = ev.maxParticipants;
              }
      } else {
        eventInfoDiv.textContent = 'Could not load event information.';
      }
    } catch (err) {
      console.error(err);
      eventInfoDiv.textContent = 'Error loading event information.';
    }
  } else {
    eventInfoDiv.textContent = 'No event selected.';
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const data = {
      eventId: Number(document.getElementById('eventId').value),
      firstName: document.getElementById('firstName').value.trim(),
      lastName: document.getElementById('lastName').value.trim(),
      email: document.getElementById('email').value.trim(),
      phone: document.getElementById('phone').value.trim(),
      numParticipants: Number(document.getElementById('numParticipants').value),
      paymentMethod: document.getElementById('paymentMethod').value
    };

    if (!data.eventId || !data.firstName || !data.lastName || !data.email || !data.numParticipants) {
      alert('Please fill in all required fields.');
      return;
    }
    const numInput = document.getElementById('numParticipants');
      if (numInput.max && data.numParticipants > Number(numInput.max)) {
        alert(`You can only book up to ${numInput.max} participants for this event.`);
        return;
      }

    try {
      const res = await fetch('/api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
      });

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error('Booking failed: ' + (msg || res.status));
      }

      const booking = await res.json();
      bookingNumberEl.textContent = booking.bookingNumber;
      resultBox.style.display = 'block';
    } catch (err) {
      console.error(err);
      alert('Booking failed. Please try again later.');
    }
  });
});
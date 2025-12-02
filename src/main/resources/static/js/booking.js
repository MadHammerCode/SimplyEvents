
document.addEventListener('DOMContentLoaded', async () => {
  const params = new URLSearchParams(window.location.search);
  const eventId = params.get('eventId');

  const eventInfoDiv = document.getElementById('bookingEventInfo');
  const form = document.getElementById('bookingForm');
  const bookingNumberEl = document.getElementById('bookingNumber');
  const resultBox = document.getElementById('bookingResult');


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

        if (ev.yearRound) {
          eventInfoDiv.innerHTML += `<p>Availability: all year</p>`;
        } else if (ev.bookingStart && ev.bookingEnd) {
          eventInfoDiv.innerHTML += `<p>Availability: bookable from ${ev.bookingStart} to ${ev.bookingEnd}</p>`;
        } else {
          eventInfoDiv.innerHTML += `<p>Availability: not specified</p>`;
        }


        const todayStr = new Date().toISOString().slice(0, 10);
        let blockedMessage = '';

        if (!ev.yearRound) {
          if (ev.bookingStart && todayStr < ev.bookingStart) {
            blockedMessage = `This event can only be booked from ${ev.bookingStart}.`;
          }
          if (!blockedMessage && ev.bookingEnd && todayStr > ev.bookingEnd) {
            blockedMessage = `The booking period ended on ${ev.bookingEnd}.`;
          }
        }

        if (blockedMessage) {
          form.style.display = 'none';
          eventInfoDiv.innerHTML += `<p style="color:#b91c1c; font-weight:600;">${blockedMessage}</p>`;
          return;
        }
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
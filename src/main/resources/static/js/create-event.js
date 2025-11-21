// create-event.js
document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('createEventForm');
  const modal = document.getElementById('createEventModal');

  if (!form) {
    // Seite ohne Create-Event-Formular
    return;
  }

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

    // Validierung wie bisher
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

      // Modal schließen
      if (modal) {
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');
      }

      // Liste neu laden, falls die Seite eine hat
      if (typeof window.loadEvents === 'function') {
        await window.loadEvents();
      }
    } catch (err) {
      console.error(err);
      alert('Save failed. Please try again later.');
    }
  });
});
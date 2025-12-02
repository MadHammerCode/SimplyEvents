
document.addEventListener('DOMContentLoaded', function () {
  const form = document.getElementById('createEventForm');
  const modal = document.getElementById('createEventModal');
  const imageInput = document.getElementById('image');

  if (!form) {

    return;
  }

  form.addEventListener('submit', async function (e) {
    e.preventDefault();

    form.classList.add('was-submitted');

    const cancelDateTime = form.cancelDeadline.value || null;

    const yearRound = form.yearRound.checked;

    const bookingStart = !yearRound && form.bookingStart.value
      ? form.bookingStart.value
      : null;

    const bookingEnd = !yearRound && form.bookingEnd.value
      ? form.bookingEnd.value
      : null;

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

      cancellationDeadline: cancelDateTime,

      bookingStart,
      bookingEnd,
      yearRound
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

    if (!yearRound) {
      if (!bookingStart || !bookingEnd) {
        alert('Please set both "Booking from" and "Booking until" or choose "Available all year".');
        return;
      }

      if (new Date(bookingStart) > new Date(bookingEnd)) {
        alert('"Booking from" must be before or equal to "Booking until".');
        return;
      }
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


      if (imageInput && imageInput.files && imageInput.files.length > 0) {
        try {
          const imgFormData = new FormData();
          imgFormData.append('file', imageInput.files[0]);

          const imgRes = await fetch(`/api/events/${saved.id}/image`, {
            method: 'POST',
            body: imgFormData
          });

          if (!imgRes.ok) {
            const imgMsg = await imgRes.text().catch(() => '');
            console.error('Image upload failed:', imgMsg || imgRes.status);
            alert('Event saved, but the image could not be uploaded.');
          }
        } catch (imgErr) {
          console.error('Image upload error:', imgErr);
          alert('Event saved, but an error occurred while uploading the image.');
        }
      }

      alert('Save Event: ' + saved.title);
      form.reset();

      if (modal) {
        modal.classList.remove('open');
        modal.setAttribute('aria-hidden', 'true');
      }


      if (typeof window.loadEvents === 'function') {
        await window.loadEvents();
      }
    } catch (err) {
      console.error(err);
      alert('Save failed. Please try again later.');
    }
  });
});
document.addEventListener('DOMContentLoaded', function () {
  function initCreateEventForm() {
    const form = document.getElementById('createEventForm');
    const modal = document.getElementById('createEventModal');
    const imageInput = document.getElementById('image');

    if (!form) {
      return;
    }

    // remove any previous handler to avoid double-binding
    form.replaceWith(form.cloneNode(true));
    const newForm = document.getElementById('createEventForm');
    if (!newForm) return;

    newForm.addEventListener('submit', async function (e) {
      e.preventDefault();

      const form = newForm; // use local ref
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

      // require basic required fields (note: date/time are optional if booking window or yearRound is provided)
      if (
        !eventData.title ||
        !eventData.location ||
        eventData.price === null ||
        eventData.maxParticipants === null
      ) {
        const msg = 'Please fill in all required fields (*).';
        const pageMessageEl = document.getElementById('pageMessage');
        if (pageMessageEl) {
          pageMessageEl.textContent = msg;
          pageMessageEl.className = 'msg-error';
          pageMessageEl.style.display = 'block';
        } else {
          alert(msg);
        }
        return;
      }

      // Validate that date+time OR yearRound OR booking window is provided
      const pageMessageEl = document.getElementById('pageMessage');

      const datePresent = !!eventData.date && !!eventData.time;
      const bookingWindowPresent = !!eventData.bookingStart && !!eventData.bookingEnd;
      const yearRoundFlag = !!eventData.yearRound;

      if (!datePresent && !yearRoundFlag && !bookingWindowPresent) {
        const msg = 'Please enter either date + time, or activate Available all year, or fill in both Booking from and Booking until.';
        if (pageMessageEl) {
          pageMessageEl.textContent = msg;
          pageMessageEl.className = 'msg-error';
          pageMessageEl.style.display = 'block';
        } else {
          alert(msg);
        }
        return;
      }

      if (bookingWindowPresent) {
        const bs = new Date(eventData.bookingStart);
        const be = new Date(eventData.bookingEnd);
        if (isNaN(bs.getTime()) || isNaN(be.getTime()) || bs > be) {
          const msg = 'The booking window is invalid: "Booking of" must be before or equal to "Booking until".';
          if (pageMessageEl) {
            pageMessageEl.textContent = msg;
            pageMessageEl.className = 'msg-error';
            pageMessageEl.style.display = 'block';
          } else {
            alert(msg);
          }
          return;
        }
      }

      // -- New: client-side check for past event datetime --
      try {
        if (eventData.date && eventData.time) {
          const eventDateTime = new Date(eventData.date + 'T' + eventData.time);
          const now = new Date();
          if (eventDateTime < now) {
            const confirmPast = window.confirm('The event is in the past — do you still want to create it?');
            if (!confirmPast) {
              return;
            }
            eventData.confirmPast = true;
          }
        }
      } catch (err) {
        console.warn('Could not check event date for pastness', err);
      }

      try {
        const editIdEl = document.getElementById('editEventId');
        const editId = editIdEl && editIdEl.value ? editIdEl.value.trim() : null;
        const url = editId ? `/api/events/${encodeURIComponent(editId)}` : '/api/events';
        const method = editId ? 'PUT' : 'POST';

        const res = await fetch(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(eventData)
        });

        if (!res.ok) {
          const msg = await res.text().catch(() => 'Error while saving.');
          const pageMessageEl = document.getElementById('pageMessage');
          if (pageMessageEl) {
            pageMessageEl.textContent = msg;
            pageMessageEl.className = 'msg-error';
            pageMessageEl.style.display = 'block';
          } else {
            alert(msg);
          }
          return;
        }

        const saved = await res.json();

        if (imageInput && imageInput.files && imageInput.files.length > 0) {
          try {
            const imgFormData = new FormData();
            imgFormData.append('file', imageInput.files[0]);

            const eventIdForImage = (saved && saved.id) ? saved.id : editId;
            const imgRes = await fetch(`/api/events/${eventIdForImage}/image`, {
               method: 'POST',
               body: imgFormData
             });



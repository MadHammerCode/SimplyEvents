let currentEventId = null;


async function openEventDetail(eventId) {
  currentEventId = eventId;

  try {
    console.log('Opening event details for id:', eventId, 'URL:', `/api/events/${eventId}`);
    const res = await fetch(`/api/events/${eventId}`);
    if (!res.ok) throw new Error("Failed to load event details: " + res.status);

    const ev = await res.json();


    document.getElementById("detailTitle").textContent = ev.title;
    document.getElementById("detailDate").textContent = ev.date ?? '';
    document.getElementById("detailTime").textContent = ev.time ?? '';
    document.getElementById("detailLocation").textContent = ev.location ?? '';
    document.getElementById("detailPrice").textContent = (ev.price != null ? ev.price + " €" : "Free");
    document.getElementById("detailCategory").textContent = ev.category ?? '';
    document.getElementById("detailDescription").textContent = ev.description ?? '';
    const minEl = document.getElementById("detailMin");
    if (minEl) minEl.textContent = ev.minParticipants ?? '';

    const maxEl = document.getElementById("detailMax");
    if (maxEl) maxEl.textContent = ev.maxParticipants ?? '';

    document.getElementById("detailSlots").textContent = ev.availableSlots ?? '';

    const equipEl = document.getElementById("detailEquipment");
    if (equipEl) equipEl.textContent = ev.equipmentNeeded ?? '';

    const reqEl = document.getElementById("detailRequirements");
    if (reqEl) reqEl.textContent = ev.requirements ?? '';

    const cancelEl = document.getElementById("detailCancel");
    if (cancelEl) cancelEl.textContent = ev.cancellationDeadline ?? '';

    const slotsEl = document.getElementById("detailSlots");
    if (ev.availableSlots != null) {
      slotsEl.textContent = ev.availableSlots;
    } else if (ev.maxParticipants != null) {
      slotsEl.textContent = ev.maxParticipants;
    } else {
      slotsEl.textContent = '—';
    }

    const availabilityEl = document.getElementById("detailAvailability");
    if (availabilityEl) {
      if (ev.yearRound) {
        availabilityEl.textContent = "Available all year";
      } else if (ev.bookingStart && ev.bookingEnd) {
        availabilityEl.textContent = `Bookable from ${ev.bookingStart} to ${ev.bookingEnd}`;
      } else {
        availabilityEl.textContent = "Availability not specified";
      }
    }


    const detailImage = document.getElementById("detailImage");
    if (detailImage) {
      if (ev.imagePath) {
        detailImage.src = '/' + ev.imagePath;
        detailImage.style.display = 'block';
      } else {
        detailImage.src = '';
        detailImage.style.display = 'none';
      }
    }

    // setze Link zur Vollseitenansicht (neue Seite zeigt Event nochmals detaillierter + Buchung)
    const fullPageLink = document.getElementById('openFullPageBtn');
    if (fullPageLink) {
      fullPageLink.href = '/event-details/' + encodeURIComponent(eventId);
      fullPageLink.style.display = 'inline-block';
    }

    const modal = document.getElementById("eventDetailModal");
    if (!modal) {
      console.error('eventDetailModal not found in DOM');
      return;
    }
    modal.classList.add("open");
    modal.setAttribute("aria-hidden", "false");
  } catch (err) {
    console.error(err);
    alert("Could not load event details.");
  }
}


window.openEventDetail = openEventDetail;


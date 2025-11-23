// event-detail.js
// Hält die aktuell im Detail-Modal angezeigte Event-ID
let currentEventId = null;

// Wird von events-list.js aufgerufen, wenn ein Event in der Liste angeklickt wird
async function openEventDetail(eventId) {
  currentEventId = eventId; // wichtig für den "Book Event"-Button

  try {
    console.log('Opening event details for id:', eventId, 'URL:', `/api/events/${eventId}`);
    const res = await fetch(`/api/events/${eventId}`);
    if (!res.ok) throw new Error("Failed to load event details: " + res.status);

    const ev = await res.json();

    // DOM-Felder im Modal füllen
    document.getElementById("detailTitle").textContent = ev.title;
    document.getElementById("detailDate").textContent = ev.date ?? '';
    document.getElementById("detailTime").textContent = ev.time ?? '';
    document.getElementById("detailLocation").textContent = ev.location ?? '';
    document.getElementById("detailPrice").textContent = (ev.price != null ? ev.price + " €" : "Free");
    document.getElementById("detailCategory").textContent = ev.category ?? '';
    document.getElementById("detailDescription").textContent = ev.description ?? '';
    document.getElementById("detailMin").textContent = ev.minParticipants ?? '';
    document.getElementById("detailMax").textContent = ev.maxParticipants ?? '';
    document.getElementById("detailSlots").textContent = ev.availableSlots ?? '';
    document.getElementById("detailEquipment").textContent = ev.equipmentNeeded ?? '';
    document.getElementById("detailRequirements").textContent = ev.requirements ?? '';
    document.getElementById("detailCancel").textContent = ev.cancellationDeadline ?? '';

    const slotsEl = document.getElementById("detailSlots");
    if (ev.availableSlots != null) {
      slotsEl.textContent = ev.availableSlots;
    } else if (ev.maxParticipants != null) {
      slotsEl.textContent = ev.maxParticipants;
    } else {
      slotsEl.textContent = '—';
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

// global machen, damit events-list.js es aufrufen kann
window.openEventDetail = openEventDetail;

// Buttons im Detail-Modal verdrahten
document.addEventListener('DOMContentLoaded', () => {
  const bookBtn = document.getElementById('bookEventBtn');
  const cancelBtn = document.getElementById('cancelBookingBtn');

  if (bookBtn) {
    bookBtn.addEventListener('click', () => {
      if (!currentEventId) {
        alert('No event selected.');
        return;
      }
      // weiterleiten auf die Buchungsseite mit Event-ID als Parameter
      window.location.href = `/booking?eventId=${currentEventId}`;
    });
  }

  if (cancelBtn) {
    cancelBtn.addEventListener('click', () => {
      // hier nur auf die Cancel-Seite gehen, Booking-Nummer wird dort eingegeben
      window.location.href = `/cancel-booking`;
    });
  }
});
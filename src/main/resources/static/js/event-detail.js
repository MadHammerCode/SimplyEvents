// event-detail.js
(function () {
  /**
   * Lädt ein Event per ID und füllt das Detail-Modal.
   * Wird von events-list.js aufgerufen.
   */
  async function openEventDetail(eventId) {
    try {
      console.log('Opening event details for id:', eventId, 'URL:', `/api/events/${eventId}`);
      const res = await fetch(`/api/events/${eventId}`);
      if (!res.ok) throw new Error("Failed to load event details: " + res.status);

      const ev = await res.json();

      // Felder im Modal setzen
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

      // Modal öffnen
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

  // global machen, damit andere Files (events-list.js) es nutzen können
  window.openEventDetail = openEventDetail;
})();
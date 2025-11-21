// modal.js
// Zentrale Steuerung zum Öffnen/Schließen von Modals (.modal)
// Unterstützt:
//  - Öffnen des Create-Event-Modals über #createEventBtn
//  - Schließen aller Modals über [data-close] und ESC

document.addEventListener('DOMContentLoaded', function () {
  const createOpenBtn = document.getElementById('createEventBtn');
  const createModal = document.getElementById('createEventModal');

  function openModal(modal) {
    if (!modal) return;
    console.log('Opening modal:', modal.id);
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
  }

  function closeModal(modal) {
    if (!modal) return;
    console.log('Closing modal:', modal.id);
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
  }

  // Spezifisch: Create-Event-Modal über Button öffnen
  if (createOpenBtn && createModal) {
    console.log('Modal init: createEventBtn + createEventModal gefunden');
    createOpenBtn.addEventListener('click', function () {
      console.log('CreateEventBtn clicked -> open createEventModal');
      openModal(createModal);
    });
  } else {
    console.warn('CreateEvent-Button oder Create-Modal nicht gefunden:', {
      createOpenBtn,
      createModal
    });
  }

  // Global: Klick auf [data-close] schließt das nächste .modal
  document.addEventListener('click', function (e) {
    if (e.target.matches('[data-close]')) {
      const modal = e.target.closest('.modal');
      console.log('data-close clicked, closing modal:', modal && modal.id);
      if (modal) {
        closeModal(modal);
      }
    }
  });

  // Global: ESC-Taste schließt alle offenen Modals
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      const openModals = document.querySelectorAll('.modal.open');
      openModals.forEach((m) => {
        console.log('ESC pressed, closing modal:', m.id);
        closeModal(m);
      });
    }
  });
});
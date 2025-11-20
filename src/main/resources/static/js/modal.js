const openBtn = document.getElementById('createEventBtn');
const modal = document.getElementById('createEventModal');

console.log('Modal init:', { openBtn, modal });

if (!openBtn || !modal) {
  console.warn('CreateEvent-Button oder Modal im DOM nicht gefunden.');
} else {
  function openModal() {
    console.log('Opening modal');
    modal.classList.add('open');
    modal.setAttribute('aria-hidden', 'false');
  }

  function closeModal() {
    console.log('Closing modal');
    modal.classList.remove('open');
    modal.setAttribute('aria-hidden', 'true');
  }

  openBtn.addEventListener('click', function () {
    console.log('CreateEventBtn clicked');
    openModal();
  });

  modal.addEventListener('click', function (e) {
    if (e.target.matches('[data-close]')) {
      closeModal();
    }
  });

  document.addEventListener('DOMContentLoaded', function () {
    const createOpenBtn = document.getElementById('createEventBtn');
    const createModal = document.getElementById('createEventModal');

    function openModal(modal) {
      if (!modal) return;
      modal.classList.add('open');
      modal.setAttribute('aria-hidden', 'false');
    }

    function closeModal(modal) {
      if (!modal) return;
      modal.classList.remove('open');
      modal.setAttribute('aria-hidden', 'true');
    }


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


    document.addEventListener('click', function (e) {
      if (e.target.matches('[data-close]')) {
        const modal = e.target.closest('.modal');
        console.log('data-close clicked, closing modal:', modal && modal.id);
        if (modal) {
          closeModal(modal);
        }
      }
    });


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
}
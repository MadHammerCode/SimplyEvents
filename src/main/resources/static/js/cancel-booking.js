// cancel-booking.js
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('cancelForm');
  const resultBox = document.getElementById('cancelResult');
  const resultNumber = document.getElementById('cancelResultNumber');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const bookingNumber = document.getElementById('bookingNumber').value.trim();
    const cancelReason = document.getElementById('cancelReason').value.trim();

    if (!bookingNumber || !cancelReason) {
      alert('Please fill in booking number and cancel reason.');
      return;
    }

    const payload = { bookingNumber, cancelReason };

    try {
      const res = await fetch('/api/bookings/cancel', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) {
        const msg = await res.text().catch(() => '');
        throw new Error('Cancel failed: ' + (msg || res.status));
      }

      const cancelled = await res.json();
      resultNumber.textContent = cancelled.bookingNumber;
      resultBox.style.display = 'block';
    } catch (err) {
      console.error(err);
      alert('Cancel failed. Please check the booking number and try again.');
    }
  });
});
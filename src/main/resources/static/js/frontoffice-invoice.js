// Frontoffice Invoice Handler
document.addEventListener('DOMContentLoaded', function() {
    const invoiceModal = document.getElementById('invoiceModalFO');
    const invoiceForm = document.getElementById('invoiceFormFO');
    const invoiceSubmitBtn = document.getElementById('invoiceSubmitBtn');
    const invoiceFormError = document.getElementById('invoiceFormError');
    const invoiceFormSuccess = document.getElementById('invoiceFormSuccess');
    const participantNameInput = document.getElementById('invoiceParticipantName');
    const eventNameInput = document.getElementById('invoiceEventName');
    const priceInput = document.getElementById('invoicePrice');
    const quantityInput = document.getElementById('invoiceQuantity');
    const totalInput = document.getElementById('invoiceTotal');

    let currentBooking = null;
    let currentEvent = null;


    function calculateTotal() {
        const price = parseFloat(priceInput.value) || 0;
        const quantity = parseInt(quantityInput.value) || 0;
        const total = (price * quantity).toFixed(2);
        totalInput.value = total;
    }


    priceInput.addEventListener('change', calculateTotal);
    quantityInput.addEventListener('change', calculateTotal);

    // Close modal handlers
    document.querySelectorAll('[data-close-modal-invoice]').forEach(btn => {
        btn.addEventListener('click', closeInvoiceModal);
    });

    function closeInvoiceModal() {
        invoiceModal.classList.add('hidden');
        invoiceForm.reset();
        invoiceFormError.classList.add('hidden');
        invoiceFormSuccess.classList.add('hidden');
        currentBooking = null;
        currentEvent = null;
    }


    window.openInvoiceModal = function(bookingData, eventData) {
        console.log('openInvoiceModal called with:', bookingData, eventData);
        currentBooking = bookingData;
        currentEvent = eventData;

        const fullName = bookingData.first_name + ' ' + bookingData.last_name;
        participantNameInput.value = fullName;
        eventNameInput.value = eventData.title;
        quantityInput.value = bookingData.seats;


        if (eventData.price && eventData.price > 0) {
            priceInput.value = eventData.price.toFixed(2);
            priceInput.readOnly = false;
        } else {
            priceInput.value = '';
            priceInput.readOnly = false;
        }


        calculateTotal();

        invoiceFormError.classList.add('hidden');
        invoiceFormSuccess.classList.add('hidden');
        invoiceModal.classList.remove('hidden');
    };

    // Form submission
    invoiceForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        if (!currentBooking || !currentEvent) {
            showInvoiceError('Missing booking or event data');
            return;
        }

        const price = parseFloat(priceInput.value);
        const quantity = parseInt(quantityInput.value);

        if (isNaN(price) || price <= 0) {
            showInvoiceError('Please enter a valid price');
            return;
        }

        if (isNaN(quantity) || quantity <= 0) {
            showInvoiceError('Please enter a valid quantity');
            return;
        }

        try {
            invoiceSubmitBtn.disabled = true;
            invoiceSubmitBtn.textContent = 'Creating...';

            const vendorId = currentEvent.organizer_id || 1;
            const eventId = currentEvent.id;


            const invoiceResponse = await fetch(`/api/invoices?eventId=${eventId}&vendorId=${vendorId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!invoiceResponse.ok) {
                const errorText = await invoiceResponse.text();
                console.error('Step 1 failed:', errorText);
                throw new Error(`Failed to create invoice: ${errorText}`);
            }

            const invoice = await invoiceResponse.json();
            const invoiceId = invoice.invoiceId;


            const description = `${currentBooking.first_name} ${currentBooking.last_name} - ${currentEvent.title}`;
            const lineResponse = await fetch(`/api/invoices/${invoiceId}/lines?description=${encodeURIComponent(description)}&quantity=${quantity}&unitPrice=${price}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!lineResponse.ok) {
                const errorText = await lineResponse.text();
                console.error('Step 2 failed:', errorText);
                throw new Error(`Failed to add line to invoice: ${errorText}`);
            }


            const shareResponse = await fetch(`/api/invoices/${invoiceId}/shares?userId=${vendorId}&percentage=100`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!shareResponse.ok) {
                const errorText = await shareResponse.text();
                console.error('Step 3 failed:', errorText);
                throw new Error(`Failed to add share to invoice: ${errorText}`);
            }


            const finalizeResponse = await fetch(`/api/invoices/${invoiceId}/finalize`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!finalizeResponse.ok) {
                const errorText = await finalizeResponse.text();
                console.error('Step 4 failed:', errorText);
                throw new Error(`Failed to finalize invoice: ${errorText}`);
            }

            const finalInvoice = await finalizeResponse.json();
            showInvoiceSuccess(`✓ Invoice #${invoiceId} erstellt! Nummer: ${finalInvoice.invoiceNumber}`);

            setTimeout(() => {
                closeInvoiceModal();
            }, 2000);

        } catch (error) {
            console.error('Error creating invoice:', error);
            showInvoiceError(`✗ Fehler: ${error.message}`);
        } finally {
            invoiceSubmitBtn.disabled = false;
            invoiceSubmitBtn.textContent = 'Create Invoice';
        }
    });

    function showInvoiceError(text) {
        invoiceFormError.textContent = text;
        invoiceFormError.classList.remove('hidden');
        invoiceFormSuccess.classList.add('hidden');
    }

    function showInvoiceSuccess(text) {
        invoiceFormSuccess.textContent = text;
        invoiceFormSuccess.classList.remove('hidden');
        invoiceFormError.classList.add('hidden');
    }
});

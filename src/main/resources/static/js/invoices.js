let allInvoices = [];
let filteredInvoices = [];

/* -------- Initialization -------- */

document.addEventListener('DOMContentLoaded', function() {
    setupNavigation();
    loadInvoices();
    setupFilters();
    setupModal();
});

/* -------- Navigation -------- */

function setupNavigation() {
    // Setup navbar dropdown
    const actionsToggle = document.getElementById('actionsToggle');
    const actionsMenu = document.getElementById('actionsMenu');

    if (actionsToggle && actionsMenu) {
        actionsToggle.addEventListener('click', (e) => {
            e.stopPropagation();
            actionsMenu.classList.toggle('hidden');
            actionsToggle.setAttribute('aria-expanded',
                actionsMenu.classList.contains('hidden') ? 'false' : 'true');
        });

        document.addEventListener('click', (e) => {
            if (!actionsToggle.contains(e.target) && !actionsMenu.contains(e.target)) {
                actionsMenu.classList.add('hidden');
                actionsToggle.setAttribute('aria-expanded', 'false');
            }
        });
    }

    const goToDashboard = document.getElementById('goToDashboard');
    const goToFrontoffice = document.getElementById('goToFrontoffice');
    const logoutBtn = document.getElementById('logoutBtn');

    if (goToDashboard) {
        goToDashboard.addEventListener('click', () => {
            window.location.href = '/dashboard';
        });
    }

    if (goToFrontoffice) {
        goToFrontoffice.addEventListener('click', () => {
            window.location.href = '/frontoffice-checkin';
        });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            try {
                localStorage.removeItem('simplyevents_currentUser');
            } catch (_) {}
            window.location.href = '/login';
        });
    }
}

/* -------- Load Invoices -------- */

function loadInvoices() {
    const tableBody = document.getElementById('invoiceTableBody');
    const empty = document.getElementById('tableEmpty');

    if (tableBody) tableBody.innerHTML = '';
    if (empty) empty.textContent = 'Loading invoices...';

    fetch('/api/invoices')
        .then((res) => {
            if (!res.ok) throw new Error('Failed to load invoices');
            return res.json();
        })
        .then((data) => {
            if (!Array.isArray(data)) {
                // If API returns paginated response, extract items
                allInvoices = data.content || data.items || data;
            } else {
                allInvoices = data;
            }
            applyFilters();
        })
        .catch((err) => {
            console.error('Error loading invoices:', err);
            if (empty) {
                empty.textContent = 'Failed to load invoices. Please try again.';
            }
        });
}

/* -------- Filters -------- */

function setupFilters() {
    const statusFilter = document.getElementById('filterStatus');
    const searchInput = document.getElementById('searchInput');

    if (statusFilter) {
        statusFilter.addEventListener('change', applyFilters);
    }

    if (searchInput) {
        searchInput.addEventListener('input', applyFilters);
    }
}

function applyFilters() {
    const statusFilter = document.getElementById('filterStatus')?.value || '';
    const searchTerm = document.getElementById('searchInput')?.value.trim().toLowerCase() || '';

    filteredInvoices = allInvoices.filter((inv) => {
        if (statusFilter && inv.status !== statusFilter) {
            return false;
        }

        if (searchTerm) {
            const searchable = [
                inv.invoiceNumber || '',
                inv.eventId || '',
                inv.vendorId || '',
            ].join(' ').toLowerCase();

            if (!searchable.includes(searchTerm)) {
                return false;
            }
        }

        return true;
    });

    renderTable();
}

/* -------- Render Table -------- */

function renderTable() {
    const tbody = document.getElementById('invoiceTableBody');
    const empty = document.getElementById('tableEmpty');

    if (!tbody || !empty) return;

    if (filteredInvoices.length === 0) {
        tbody.innerHTML = '';
        empty.textContent = 'No invoices found.';
        empty.classList.remove('hidden');
        return;
    }

    empty.classList.add('hidden');

    const rows = filteredInvoices.map((inv) => {
        const invoiceNo = inv.invoiceNumber || `Draft #${inv.invoiceId}`;
        const status = inv.status || 'DRAFT';
        const total = inv.total ? `€${parseFloat(inv.total).toFixed(2)}` : '€0.00';
        const created = inv.createdAt ? new Date(inv.createdAt).toLocaleDateString() : '-';

        const statusBadgeClass = status === 'FINAL' ? 'status-badge--final' : 'status-badge--draft';

        return `
            <tr>
                <td><strong>${escapeHtml(invoiceNo)}</strong></td>
                <td>Event #${escapeHtml(String(inv.eventId))}</td>
                <td>Vendor #${escapeHtml(String(inv.vendorId))}</td>
                <td>
                    <span class="status-badge ${statusBadgeClass}">
                        ${escapeHtml(status)}
                    </span>
                </td>
                <td>${total}</td>
                <td>${created}</td>
                <td>
                    <div class="action-buttons">
                        <button type="button" 
                                class="btn-small btn-small--view" 
                                data-view-invoice="${inv.invoiceId}"
                                title="View details">
                            View
                        </button>
                        ${inv.pdfPath ? `
                            <button type="button" 
                                    class="btn-small btn-small--download" 
                                    data-download-pdf="${inv.invoiceId}"
                                    title="Download PDF">
                                PDF
                            </button>
                        ` : ''}
                    </div>
                </td>
            </tr>
        `;
    }).join('');

    tbody.innerHTML = rows;
    setupRowActions();
}

function setupRowActions() {
    // View details
    document.querySelectorAll('[data-view-invoice]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const invoiceId = btn.getAttribute('data-view-invoice');
            showInvoiceDetails(invoiceId);
        });
    });

    // Download PDF
    document.querySelectorAll('[data-download-pdf]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const invoiceId = btn.getAttribute('data-download-pdf');
            downloadPdf(invoiceId);
        });
    });
}

/* -------- Modal -------- */

function setupModal() {
    const modal = document.getElementById('invoiceDetailsModal');
    const closeBtn = document.getElementById('closeDetailsModal');
    const closeFooterBtn = document.getElementById('closeDetailsBtn');
    const overlay = document.getElementById('invoiceModalOverlay');

    if (closeBtn) {
        closeBtn.addEventListener('click', closeModal);
    }

    if (closeFooterBtn) {
        closeFooterBtn.addEventListener('click', closeModal);
    }

    if (overlay) {
        overlay.addEventListener('click', closeModal);
    }

    function closeModal() {
        if (modal) {
            modal.classList.add('hidden');
        }
    }
}

function showInvoiceDetails(invoiceId) {
    const modal = document.getElementById('invoiceDetailsModal');
    const invoice = allInvoices.find((inv) => String(inv.invoiceId) === String(invoiceId));

    if (!invoice) {
        alert('Invoice not found');
        return;
    }

    // Fill in details
    document.getElementById('detailsTitle').textContent =
        `Invoice ${invoice.invoiceNumber || `Draft #${invoiceId}`}`;

    document.getElementById('detailNumber').textContent =
        invoice.invoiceNumber || `Draft #${invoiceId}`;

    document.getElementById('detailStatus').textContent = invoice.status || 'DRAFT';

    document.getElementById('detailCreated').textContent =
        invoice.createdAt ? new Date(invoice.createdAt).toLocaleDateString() : '-';

    document.getElementById('detailEventId').textContent = invoice.eventId || '-';

    // Lines
    const linesContainer = document.getElementById('detailLines');
    if (invoice.lines && invoice.lines.length > 0) {
        linesContainer.innerHTML = invoice.lines.map((line) => `
            <div class="line-item">
                <div class="line-header">
                    <strong>${escapeHtml(line.description)}</strong>
                    <span>€${parseFloat(line.lineTotal).toFixed(2)}</span>
                </div>
                <div class="line-details">
                    <div>Qty: ${line.quantity}</div>
                    <div>Unit: €${parseFloat(line.unitPrice).toFixed(2)}</div>
                </div>
            </div>
        `).join('');
    } else {
        linesContainer.innerHTML = '<p style="color: #999;">No lines</p>';
    }

    // Shares
    const sharesContainer = document.getElementById('detailShares');
    if (invoice.shares && invoice.shares.length > 0) {
        sharesContainer.innerHTML = invoice.shares.map((share) => `
            <div class="share-item">
                <div class="line-header">
                    <strong>User #${share.userId}</strong>
                    <span>€${parseFloat(share.allocatedAmount).toFixed(2)}</span>
                </div>
            </div>
        `).join('');
    } else {
        sharesContainer.innerHTML = '<p style="color: #999;">No allocations</p>';
    }

    // Total
    document.getElementById('detailTotal').textContent =
        `€${parseFloat(invoice.total || 0).toFixed(2)}`;

    // Hash
    document.getElementById('detailHash').textContent = invoice.hash || '-';

    // PDF Link
    const pdfSection = document.getElementById('pdfSection');
    if (invoice.pdfPath && invoice.status === 'FINAL') {
        pdfSection.style.display = 'block';
        const pdfBtn = document.getElementById('pdfDownloadBtn');
        pdfBtn.href = `/api/invoices/${invoiceId}/pdf`;
        pdfBtn.textContent = '📄 Download PDF';
    } else {
        pdfSection.style.display = 'none';
    }

    // Show modal
    if (modal) {
        modal.classList.remove('hidden');
    }
}

function downloadPdf(invoiceId) {
    const link = document.createElement('a');
    link.href = `/api/invoices/${invoiceId}/pdf`;
    link.download = `invoice_${invoiceId}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

/* -------- Utilities -------- */

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

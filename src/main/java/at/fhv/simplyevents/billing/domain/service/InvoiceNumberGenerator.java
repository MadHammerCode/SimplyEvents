package at.fhv.simplyevents.billing.domain.service;

import at.fhv.simplyevents.billing.domain.model.InvoiceNumber;

public interface InvoiceNumberGenerator {

    InvoiceNumber generateNext();
}

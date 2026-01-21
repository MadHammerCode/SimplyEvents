package at.fhv.simplyevents.billing.infrastructure.service;

import at.fhv.simplyevents.billing.domain.exception.InvoiceException;
import at.fhv.simplyevents.billing.domain.model.InvoiceNumber;
import at.fhv.simplyevents.billing.domain.service.InvoiceNumberGenerator;
import at.fhv.simplyevents.billing.infrastructure.persistence.model.InvoiceSequenceJpaEntity;
import at.fhv.simplyevents.billing.infrastructure.persistence.springdata.InvoiceSequenceJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
public class InvoiceNumberGeneratorImpl implements InvoiceNumberGenerator {

    private final InvoiceSequenceJpaRepository sequenceRepository;

    public InvoiceNumberGeneratorImpl(InvoiceSequenceJpaRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public InvoiceNumber generateNext() {
        int currentYear = Year.now().getValue();


        InvoiceSequenceJpaEntity sequence = sequenceRepository.findByYearWithLock(currentYear)
            .orElseGet(() -> {
                // Create new sequence if not exists
                InvoiceSequenceJpaEntity newSeq = new InvoiceSequenceJpaEntity(currentYear, 0L);
                return sequenceRepository.save(newSeq);
            });

        long nextSequence = sequence.incrementAndGet();

        if (nextSequence > 99999) {
            throw new InvoiceException(
                "Invoice sequence exceeded for year " + currentYear + ": " + nextSequence
            );
        }

        sequenceRepository.save(sequence);

        return new InvoiceNumber(currentYear, nextSequence);
    }
}

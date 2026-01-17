package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingJpaEntity, Long> {
}


package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.domain.model.EventStatus;
import at.fhv.simplyevents.persistence.model.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, Long> {
    List<EventJpaEntity> findByStatusIn(List<EventStatus> status);
    List<EventJpaEntity> findByStatus(EventStatus status);
}


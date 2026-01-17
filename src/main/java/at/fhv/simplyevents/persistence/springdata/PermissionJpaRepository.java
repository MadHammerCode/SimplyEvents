package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.PermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionJpaEntity, Long> {
}


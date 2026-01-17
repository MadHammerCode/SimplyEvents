package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.VendorProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorProfileJpaRepository extends JpaRepository<VendorProfileJpaEntity, Long> {
}


package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.VendorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
}


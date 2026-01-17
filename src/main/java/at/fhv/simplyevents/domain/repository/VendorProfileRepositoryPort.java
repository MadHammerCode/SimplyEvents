package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.VendorProfile;

import java.util.Optional;

public interface VendorProfileRepositoryPort {
    VendorProfile save(VendorProfile vendorProfile);
    Optional<VendorProfile> findById(Long id);
}

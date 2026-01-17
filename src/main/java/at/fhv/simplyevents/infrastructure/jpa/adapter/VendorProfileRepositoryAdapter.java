package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.VendorProfile;
import at.fhv.simplyevents.domain.repository.VendorProfileRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.VendorProfileMapper;
import at.fhv.simplyevents.persistence.springdata.VendorProfileJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class VendorProfileRepositoryAdapter implements VendorProfileRepositoryPort {

    private final VendorProfileJpaRepository vendorProfiles;

    public VendorProfileRepositoryAdapter(VendorProfileJpaRepository vendorProfiles) {
        this.vendorProfiles = vendorProfiles;
    }

    @Override
    public VendorProfile save(VendorProfile vendorProfile) {
        var saved = vendorProfiles.save(VendorProfileMapper.toEntity(vendorProfile));
        return VendorProfileMapper.toDomain(saved);
    }

    @Override
    public Optional<VendorProfile> findById(Long id) {
        return vendorProfiles.findById(id).map(VendorProfileMapper::toDomain);
    }
}

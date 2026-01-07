package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.VendorProfile;
import at.fhv.simplyevents.persistence.model.VendorProfileJpaEntity;

public class VendorProfileMapper {
    private VendorProfileMapper() {}

    public static VendorProfile toDomain(VendorProfileJpaEntity e) {
        if (e == null) return null;
        return VendorProfile.restore(e.getId(), e.getUserId(), e.getCompanyId(), e.getContactInfo());
    }

    public static VendorProfileJpaEntity toEntity(VendorProfile v) {
        if (v == null) return null;
        VendorProfileJpaEntity e = new VendorProfileJpaEntity();
        e.setId(v.getId());
        e.setUserId(v.getUserId());
        e.setCompanyId(v.getCompanyId());
        e.setContactInfo(v.getContactInfo());
        return e;
    }
}

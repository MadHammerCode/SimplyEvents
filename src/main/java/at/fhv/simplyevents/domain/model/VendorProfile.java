package at.fhv.simplyevents.domain.model;

import at.fhv.simplyevents.domain.DomainValidationException;
import java.util.Objects;

public class VendorProfile {
    private final Long id;
    private final Long userId;
    private final String companyId;
    private final String contactInfo;

    private VendorProfile(Long id, Long userId, String companyId, String contactInfo) {
        if (userId == null) {
            throw new DomainValidationException("userId is required");
        }
        this.id = id;
        this.userId = userId;
        this.companyId = companyId;
        this.contactInfo = contactInfo;
    }

    public static VendorProfile create(Long userId, String companyId, String contactInfo) {
        return new VendorProfile(null, userId, companyId, contactInfo);
    }

    public static VendorProfile restore(Long id, Long userId, String companyId, String contactInfo) {
        return new VendorProfile(id, userId, companyId, contactInfo);
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getCompanyId() { return companyId; }
    public String getContactInfo() { return contactInfo; }

    public VendorProfile withUpdatedContact(String companyId, String contactInfo) {
        return new VendorProfile(this.id, this.userId, companyId, contactInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VendorProfile)) return false;
        VendorProfile that = (VendorProfile) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}

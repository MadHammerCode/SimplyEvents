package at.fhv.simplyevents.domain.model;

import jakarta.persistence.*;

@Entity
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @ManyToOne
    @JoinColumn(name = "vendor_profile_id")
    private VendorProfile vendor;

    private String name;
    private String email;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public VendorProfile getVendor() { return vendor; }
    public void setVendor(VendorProfile vendor) { this.vendor = vendor; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
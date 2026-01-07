package at.fhv.simplyevents.persistence.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employee")
public class EmployeeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "vendor_profile_id")
    private Long vendorProfileId;

    private String name;

    private String email;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Long getVendorProfileId() { return vendorProfileId; }
    public void setVendorProfileId(Long vendorProfileId) { this.vendorProfileId = vendorProfileId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}


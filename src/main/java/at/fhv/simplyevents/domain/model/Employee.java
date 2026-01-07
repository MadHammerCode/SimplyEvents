package at.fhv.simplyevents.domain.model;

public class Employee {

    private Long employeeId;
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
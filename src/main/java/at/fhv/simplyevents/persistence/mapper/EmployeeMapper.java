package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.Employee;
import at.fhv.simplyevents.persistence.model.EmployeeJpaEntity;

public class EmployeeMapper {
    private EmployeeMapper() {}

    public static Employee toDomain(EmployeeJpaEntity e) {
        if (e == null) return null;
        Employee emp = new Employee();
        emp.setEmployeeId(e.getEmployeeId());
        emp.setVendorProfileId(e.getVendorProfileId());
        emp.setName(e.getName());
        emp.setEmail(e.getEmail());
        return emp;
    }

    public static EmployeeJpaEntity toEntity(Employee e) {
        if (e == null) return null;
        EmployeeJpaEntity j = new EmployeeJpaEntity();
        j.setEmployeeId(e.getEmployeeId());
        j.setVendorProfileId(e.getVendorProfileId());
        j.setName(e.getName());
        j.setEmail(e.getEmail());
        return j;
    }
}


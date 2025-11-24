package at.fhv.simplyevents.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

import java.util.List;

@Entity
public class Vendor extends Account {

    @Column(nullable = false, unique = true)
    private String companyId;

    private String contactInfo;

    @OneToMany(mappedBy = "vendor")
    private List<Employee> employees;

    @OneToMany(mappedBy = "vendor")
    private List<Event> events;
}


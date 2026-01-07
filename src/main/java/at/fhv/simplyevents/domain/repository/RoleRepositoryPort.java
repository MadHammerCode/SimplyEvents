package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.Role;

import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);
    Role save(Role role);
}

package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.RolePermissionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, Long> {
    Set<RolePermissionJpaEntity> findByRoleId(Long roleId);
    void deleteByRoleId(Long roleId);
}


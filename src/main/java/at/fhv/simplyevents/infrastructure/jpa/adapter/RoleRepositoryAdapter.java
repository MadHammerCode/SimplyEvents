package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.Role;
import at.fhv.simplyevents.domain.repository.RoleRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.RoleMapper;
import at.fhv.simplyevents.persistence.model.RoleJpaEntity;
import at.fhv.simplyevents.persistence.model.RolePermissionJpaEntity;
import at.fhv.simplyevents.persistence.springdata.RoleJpaRepository;
import at.fhv.simplyevents.persistence.springdata.RolePermissionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class RoleRepositoryAdapter implements RoleRepositoryPort {

    private final RoleJpaRepository roles;
    private final RolePermissionJpaRepository rolePermissions;

    public RoleRepositoryAdapter(RoleJpaRepository roles, RolePermissionJpaRepository rolePermissions) {
        this.roles = roles;
        this.rolePermissions = rolePermissions;
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roles.findByName(name)
                .map(e -> RoleMapper.toDomain(e, rolePermissions.findByRoleId(e.getId())));
    }

    @Override
    public Optional<Role> findById(Long id) {
        return roles.findById(id)
                .map(e -> RoleMapper.toDomain(e, rolePermissions.findByRoleId(e.getId())));
    }

    @Override
    public Role save(Role role) {
        RoleJpaEntity saved = roles.save(RoleMapper.toEntity(role));
        rolePermissions.deleteByRoleId(saved.getId());
        if (role.getPermissionIds() != null) {
            for (Long permId : role.getPermissionIds()) {
                RolePermissionJpaEntity link = new RolePermissionJpaEntity();
                link.setRoleId(saved.getId());
                link.setPermissionId(permId);
                rolePermissions.save(link);
            }
        }
        Set<RolePermissionJpaEntity> links = rolePermissions.findByRoleId(saved.getId());
        return RoleMapper.toDomain(saved, links);
    }
}

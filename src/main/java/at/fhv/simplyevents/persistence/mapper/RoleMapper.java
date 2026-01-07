package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.Role;
import at.fhv.simplyevents.persistence.model.RoleJpaEntity;
import at.fhv.simplyevents.persistence.model.RolePermissionJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class RoleMapper {
    private RoleMapper() {}

    public static Role toDomain(RoleJpaEntity e, Set<RolePermissionJpaEntity> perms) {
        if (e == null) return null;
        Role r = new Role();
        r.setId(e.getId());
        r.setName(e.getName());
        if (perms != null) {
            r.setPermissionIds(perms.stream()
                    .map(RolePermissionJpaEntity::getPermissionId)
                    .collect(Collectors.toSet()));
        }
        return r;
    }

    public static RoleJpaEntity toEntity(Role r) {
        if (r == null) return null;
        RoleJpaEntity e = new RoleJpaEntity();
        e.setId(r.getId());
        e.setName(r.getName());
        return e;
    }
}


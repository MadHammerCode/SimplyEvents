package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.Permission;
import at.fhv.simplyevents.persistence.model.PermissionJpaEntity;

public class PermissionMapper {
    private PermissionMapper() {}

    public static Permission toDomain(PermissionJpaEntity e) {
        if (e == null) return null;
        Permission p = new Permission();
        p.setId(e.getId());
        p.setName(e.getName());
        return p;
    }

    public static PermissionJpaEntity toEntity(Permission p) {
        if (p == null) return null;
        PermissionJpaEntity e = new PermissionJpaEntity();
        e.setId(p.getId());
        e.setName(p.getName());
        return e;
    }
}


package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.Permission;
import at.fhv.simplyevents.domain.repository.PermissionRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.PermissionMapper;
import at.fhv.simplyevents.persistence.springdata.PermissionJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class PermissionRepositoryAdapter implements PermissionRepositoryPort {

    private final PermissionJpaRepository permissions;

    public PermissionRepositoryAdapter(PermissionJpaRepository permissions) {
        this.permissions = permissions;
    }

    @Override
    public Permission save(Permission permission) {
        var saved = permissions.save(PermissionMapper.toEntity(permission));
        return PermissionMapper.toDomain(saved);
    }
}


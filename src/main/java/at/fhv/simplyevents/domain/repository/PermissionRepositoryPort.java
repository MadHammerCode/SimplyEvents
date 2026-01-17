package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.Permission;

public interface PermissionRepositoryPort {
    Permission save(Permission permission);
}


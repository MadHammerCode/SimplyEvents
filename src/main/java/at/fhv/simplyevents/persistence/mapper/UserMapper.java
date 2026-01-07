package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.DomainValidationException;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.persistence.model.UserJpaEntity;
import at.fhv.simplyevents.persistence.model.UserRoleJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {
    private UserMapper() {}

    public static User toDomain(UserJpaEntity e, Set<UserRoleJpaEntity> roles) {
        if (e == null) return null;
        if (roles == null || roles.isEmpty()) {
            throw new DomainValidationException("User must have at least one role");
        }
        Set<Long> roleIds = roles.stream()
                .map(UserRoleJpaEntity::getRoleId)
                .collect(Collectors.toSet());
        return User.restore(
                e.getId(),
                e.getFname(),
                e.getLname(),
                e.getEmail(),
                e.getPassword(),
                roleIds,
                e.getVendorProfileId(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    public static UserJpaEntity toEntity(User u) {
        if (u == null) return null;
        UserJpaEntity e = new UserJpaEntity();
        e.setId(u.getId());
        e.setFname(u.getFname());
        e.setLname(u.getLname());
        e.setEmail(u.getEmail());
        e.setPassword(u.getPassword());
        e.setVendorProfileId(u.getVendorProfileId());
        e.setCreatedAt(u.getCreatedAt());
        e.setUpdatedAt(u.getUpdatedAt());
        return e;
    }
}

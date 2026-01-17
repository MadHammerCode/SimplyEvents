package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.DomainValidationException;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.persistence.model.UserJpaEntity;


import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {
    private UserMapper() {}

    public static User toDomain(UserJpaEntity e) {
        if (e == null) return null;

        return User.restore(
                e.getId(),
                e.getFname(),
                e.getLname(),
                e.getEmail(),
                e.getPassword(),

                e.getRole(),

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
        e.setRole(u.getRole());
        e.setVendorProfileId(u.getVendorProfileId());
        e.setCreatedAt(u.getCreatedAt());
        e.setUpdatedAt(u.getUpdatedAt());
        return e;
    }
}

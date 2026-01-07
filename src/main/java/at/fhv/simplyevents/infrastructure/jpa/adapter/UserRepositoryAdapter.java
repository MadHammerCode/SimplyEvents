package at.fhv.simplyevents.infrastructure.jpa.adapter;

import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.repository.UserRepositoryPort;
import at.fhv.simplyevents.persistence.mapper.UserMapper;
import at.fhv.simplyevents.persistence.model.UserJpaEntity;
import at.fhv.simplyevents.persistence.model.UserRoleJpaEntity;
import at.fhv.simplyevents.persistence.springdata.UserJpaRepository;
import at.fhv.simplyevents.persistence.springdata.UserRoleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository users;
    private final UserRoleJpaRepository userRoles;

    public UserRepositoryAdapter(UserJpaRepository users, UserRoleJpaRepository userRoles) {
        this.users = users;
        this.userRoles = userRoles;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email)
                .map(e -> UserMapper.toDomain(e, userRoles.findByUserId(e.getId())));
    }

    @Override
    public User save(User user) {
        UserJpaEntity saved = users.save(UserMapper.toEntity(user));
        // reset roles for this user
        userRoles.deleteByUserId(saved.getId());
        if (user.getRoleIds() != null) {
            for (Long roleId : user.getRoleIds()) {
                UserRoleJpaEntity link = new UserRoleJpaEntity();
                link.setUserId(saved.getId());
                link.setRoleId(roleId);
                userRoles.save(link);
            }
        }
        Set<UserRoleJpaEntity> links = userRoles.findByUserId(saved.getId());
        return UserMapper.toDomain(saved, links);
    }
}


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

    public UserRepositoryAdapter(UserJpaRepository users) {
        this.users = users;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = UserMapper.toEntity(user);

        UserJpaEntity saved = users.save(entity);

        return UserMapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.findById(id)
                .map(UserMapper::toDomain);
    }
}


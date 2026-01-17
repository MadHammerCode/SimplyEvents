package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);
    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();
}


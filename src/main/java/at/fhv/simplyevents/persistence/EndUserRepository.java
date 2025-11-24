package at.fhv.simplyevents.persistence;

import at.fhv.simplyevents.domain.model.Account;
import at.fhv.simplyevents.domain.model.EndUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EndUserRepository extends JpaRepository<EndUser, Long> {
    Optional<Account> findByEmail(String email);
}


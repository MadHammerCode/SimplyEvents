package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.EndUser;
import at.fhv.simplyevents.persistence.EndUserRepository;
import at.fhv.simplyevents.rest.dto.AccountDtos;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final EndUserRepository repo;

    public AuthService(EndUserRepository repo) {
        this.repo = repo;
    }

    public EndUser register(AccountDtos.RegisterDTO dto) {

        if (repo.findByEmail(dto.email()).isPresent()) {
            throw new IllegalStateException("Email already exists");
        }

        EndUser user = new EndUser();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(dto.password()); // ⚠ plaintext (demo only)

        return repo.save(user);
    }

    public EndUser login(AccountDtos.LoginDTO dto) {

        EndUser user = (EndUser) repo.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getPassword().equals(dto.password())) {
            throw new IllegalArgumentException("Wrong password");
        }

        return user;
    }
}

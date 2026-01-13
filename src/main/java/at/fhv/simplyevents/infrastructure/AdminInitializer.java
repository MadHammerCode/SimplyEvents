package at.fhv.simplyevents.infrastructure;

import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.UserRole;
import at.fhv.simplyevents.domain.repository.UserRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@simplyevents.at";

        Optional<User> existing = userRepository.findByEmail(adminEmail);
        if (existing.isEmpty()) {
            User admin = User.create(
                    "Super",
                    "Admin",
                    adminEmail,
                    passwordEncoder.encode("admin123"),
                    UserRole.BASIC_USER,
                    null // vendorProfileId
            );

            // Promote to ADMIN immediately
            admin = admin.promoteTo(UserRole.ADMIN);

            userRepository.save(admin);
            System.out.println(">>> DEFAULT ADMIN CREATED: admin@simplyevents.at / admin123");
        }
    }
}
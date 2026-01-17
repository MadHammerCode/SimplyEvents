package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.port.in.AuthUseCase;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.UserRole;
import at.fhv.simplyevents.domain.model.VendorProfile;
import at.fhv.simplyevents.domain.repository.UserRepositoryPort;
import at.fhv.simplyevents.domain.repository.VendorProfileRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort users;
    private final VendorProfileRepositoryPort vendorProfiles;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepositoryPort users, VendorProfileRepositoryPort vendorProfiles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.vendorProfiles = vendorProfiles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerCustomer(AuthUseCase.RegisterCustomerCommand cmd) {
        users.findByEmail(cmd.email()).ifPresent(u -> { throw new IllegalStateException("Email already exists"); });

        User user = User.create(
                cmd.firstName(),
                cmd.lastName(),
                cmd.email(),
                passwordEncoder.encode(cmd.password()),
                UserRole.CUSTOMER,
                null
        );
        return users.save(user);
    }

    @Override
    public User registerVendor(AuthUseCase.RegisterVendorCommand cmd) {
        users.findByEmail(cmd.email()).ifPresent(u -> { throw new IllegalStateException("Email already exists"); });
        User user = User.create(
                cmd.firstName(),
                cmd.lastName(),
                cmd.email(),
                passwordEncoder.encode(cmd.password()),
                UserRole.CUSTOMER,
                null
        );
        User savedUser = users.save(user);

        VendorProfile vp = VendorProfile.create(savedUser.getId(), cmd.companyId(), cmd.contactInfo());
        vendorProfiles.save(vp);
        return savedUser;
    }

    @Override
    public User login(AuthUseCase.LoginCommand cmd) {
        User user = users.findByEmail(cmd.email()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(cmd.password(), user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        return user;
    }
}

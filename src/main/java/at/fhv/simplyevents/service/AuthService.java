package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.port.in.AuthUseCase;
import at.fhv.simplyevents.domain.model.Role;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.VendorProfile;
import at.fhv.simplyevents.domain.repository.RoleRepositoryPort;
import at.fhv.simplyevents.domain.repository.UserRepositoryPort;
import at.fhv.simplyevents.domain.repository.VendorProfileRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService implements AuthUseCase {

    private final UserRepositoryPort users;
    private final RoleRepositoryPort roles;
    private final VendorProfileRepositoryPort vendorProfiles;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepositoryPort users, RoleRepositoryPort roles, VendorProfileRepositoryPort vendorProfiles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.vendorProfiles = vendorProfiles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerCustomer(AuthUseCase.RegisterCustomerCommand cmd) {
        users.findByEmail(cmd.email()).ifPresent(u -> { throw new IllegalStateException("Email already exists"); });
        Role customerRole = roles.findByName("ROLE_CUSTOMER").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_CUSTOMER"); return roles.save(r);
        });
        User user = User.create(
                cmd.firstName(),
                cmd.lastName(),
                cmd.email(),
                passwordEncoder.encode(cmd.password()),
                Set.of(customerRole.getId()),
                null
        );
        return users.save(user);
    }

    @Override
    public User registerVendor(AuthUseCase.RegisterVendorCommand cmd) {
        users.findByEmail(cmd.email()).ifPresent(u -> { throw new IllegalStateException("Email already exists"); });
        Role vendorRole = roles.findByName("ROLE_VENDOR").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_VENDOR"); return roles.save(r);
        });
        User user = User.create(
                cmd.firstName(),
                cmd.lastName(),
                cmd.email(),
                passwordEncoder.encode(cmd.password()),
                Set.of(vendorRole.getId()),
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

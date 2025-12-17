package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.Role;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.VendorProfile;
import at.fhv.simplyevents.persistence.RoleRepository;
import at.fhv.simplyevents.persistence.UserRepository;
import at.fhv.simplyevents.persistence.VendorProfileRepository;
import at.fhv.simplyevents.rest.dto.AuthDtos;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthService {

    private final UserRepository users;
    private final RoleRepository roles;
    private final VendorProfileRepository vendorProfiles;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository users, RoleRepository roles, VendorProfileRepository vendorProfiles, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.vendorProfiles = vendorProfiles;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerCustomer(AuthDtos.RegisterCustomerDTO dto) {
        users.findByEmail(dto.email()).ifPresent(u -> { throw new IllegalStateException("Email already exists"); });
        Role customerRole = roles.findByName("ROLE_CUSTOMER").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_CUSTOMER"); return roles.save(r);
        });
        User user = new User();
        user.setFname(dto.firstName());
        user.setLname(dto.lastName());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(customerRole));
        return users.save(user);
    }

    public User registerVendor(AuthDtos.RegisterVendorDTO dto) {
        users.findByEmail(dto.email()).ifPresent(u -> { throw new IllegalStateException("Email already exists"); });
        Role vendorRole = roles.findByName("ROLE_VENDOR").orElseGet(() -> {
            Role r = new Role(); r.setName("ROLE_VENDOR"); return roles.save(r);
        });
        User user = new User();
        user.setFname(dto.firstName());
        user.setLname(dto.lastName());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(vendorRole));
        user = users.save(user);

        VendorProfile vp = new VendorProfile();
        vp.setUser(user);
        vp.setCompanyId(dto.companyId());
        vp.setContactInfo(dto.contactInfo());
        vendorProfiles.save(vp);
        user.setVendorProfile(vp);
        return users.save(user);
    }

    public User login(AuthDtos.LoginDTO dto) {
        User user = users.findByEmail(dto.email()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        return user;
    }
}

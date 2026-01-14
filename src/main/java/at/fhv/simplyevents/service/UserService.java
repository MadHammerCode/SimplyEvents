package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.UserUseCase;
import at.fhv.simplyevents.domain.model.Role;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.UserRole;
import at.fhv.simplyevents.domain.model.VendorProfile;
import at.fhv.simplyevents.domain.repository.RoleRepositoryPort;
import at.fhv.simplyevents.domain.repository.UserRepositoryPort;
import at.fhv.simplyevents.domain.repository.VendorProfileRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort users;
    private final VendorProfileRepositoryPort vendorProfiles;

    public UserService(UserRepositoryPort users, VendorProfileRepositoryPort vendorProfiles) {
        this.users = users;
        this.vendorProfiles = vendorProfiles;
    }

    @Override
    public UserProfileResult getUserProfile(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> NotFoundException.forEntityAndField("User", "email", email));

        String firstName = user.getFname() == null ? "" : user.getFname().trim();
        String lastName = user.getLname() == null ? "" : user.getLname().trim();

        String phone = "";
        String address = "";
        if (user.getVendorProfileId() != null) {
            Optional<VendorProfile> vp = vendorProfiles.findById(user.getVendorProfileId());
            phone = vp.map(VendorProfile::getContactInfo).orElse("");
        }

        String role = user.getRole().name();

        return new UserProfileResult(firstName, lastName, user.getEmail(), phone, address, role);
    }

    @Override
    public UserProfileResult updateUserProfile(String email, UpdateUserProfileCommand command) {
        User user = users.findByEmail(email).orElseThrow(() -> NotFoundException.forEntityAndField("User", "email", email));

        String f = command.firstName() == null ? "" : command.firstName().trim();
        String l = command.lastName() == null ? "" : command.lastName().trim();
        User updatedUser = user.withUpdatedProfile(f, l);

        if (updatedUser.getVendorProfileId() != null) {
            VendorProfile vp = vendorProfiles.findById(updatedUser.getVendorProfileId())
                    .map(existing -> existing.withUpdatedContact(existing.getCompanyId(), command.phone()))
                    .orElseGet(() -> VendorProfile.create(updatedUser.getId(), null, command.phone()));
            vendorProfiles.save(vp);
        }

        users.save(updatedUser);
        return getUserProfile(updatedUser.getEmail());
    }

    public void changeUserRole(Long userId, UserRole newRole) {
        User user = users.findById(userId)
                //NotFoundException has private constructor, so we use the static factory method
                .orElseThrow(() -> NotFoundException.forEntity("User", userId));

        User updated = user.promoteTo(newRole);
        users.save(updated);
    }

    public List<User> getAllUsers() {
        return users.findAll();
    }
}

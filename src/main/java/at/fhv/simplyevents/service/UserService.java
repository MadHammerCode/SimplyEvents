package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.Role;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.persistence.UserRepository;
import at.fhv.simplyevents.rest.dto.UserProfileDto;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    /**
     * Find user by email and map to UserProfileDto.
     */
    public UserProfileDto getUserProfile(String email) {
        User user = users.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

        String firstName = user.getFname() == null ? "" : user.getFname().trim();
        String lastName = user.getLname() == null ? "" : user.getLname().trim();

        String phone = "";
        String address = "";
        if (user.getVendorProfile() != null) {
            String contact = user.getVendorProfile().getContactInfo();
            if (contact != null) phone = contact;
        }

        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .orElse("");

        return new UserProfileDto(firstName, lastName, user.getEmail(), phone, address, role);
    }

    /**
     * Update user's profile fields (name and vendor contact info if present).
     * Email changes are not allowed here (to avoid identity complications).
     */
    public UserProfileDto updateUserProfile(String email, UserProfileDto dto) {
        User user = users.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Update first and last name directly from DTO
        String f = dto.getFirstName() == null ? "" : dto.getFirstName().trim();
        String l = dto.getLastName() == null ? "" : dto.getLastName().trim();
        user.setFname(f);
        user.setLname(l);

        // Do not allow changing email through this endpoint for safety
        // Update vendor contact info if vendor profile exists
        if (user.getVendorProfile() != null) {
            user.getVendorProfile().setContactInfo(dto.getPhone());
        }

        User saved = users.save(user);

        // Map back to DTO (reuse getUserProfile mapping)
        return getUserProfile(saved.getEmail());
    }
}

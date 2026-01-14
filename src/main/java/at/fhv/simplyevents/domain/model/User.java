package at.fhv.simplyevents.domain.model;

import at.fhv.simplyevents.domain.DomainValidationException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class User {
    private final Long id;
    private final String fname;
    private final String lname;
    private final String email;
    private final String password;
    private final UserRole role;
    private final Long vendorProfileId;
    private final Instant createdAt;
    private final Instant updatedAt;

    private User(Long id,
                 String fname,
                 String lname,
                 String email,
                 String password,
                 UserRole role,
                 Long vendorProfileId,
                 Instant createdAt,
                 Instant updatedAt) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;
        this.email = requireNotBlank(email, "email is required");
        this.password = requireNotBlank(password, "password is required");
        this.role = (role == null) ? UserRole.CUSTOMER : role;
        this.vendorProfileId = vendorProfileId;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public static User create(String fname,
                              String lname,
                              String email,
                              String password,
                              UserRole role,
                              Long vendorProfileId) {
        return new User(null, fname, lname, email, password, role, vendorProfileId, Instant.now(), Instant.now());
    }

    public static User restore(Long id,
                               String fname,
                               String lname,
                               String email,
                               String password,
                               UserRole role,
                               Long vendorProfileId,
                               Instant createdAt,
                               Instant updatedAt) {
        require(id != null, "id is required when restoring a user");
        return new User(id, fname, lname, email, password, role, vendorProfileId, createdAt, updatedAt);
    }

    public User promoteTo(UserRole newRole) {
        return new User(this.id, this.fname, this.lname, this.email, this.password,
                newRole, this.vendorProfileId, this.createdAt, Instant.now());
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new DomainValidationException(message);
        }
        return value.trim();
    }

    private static void require(boolean expression, String message) {
        if (!expression) {
            throw new DomainValidationException(message);
        }
    }

    private static Set<Long> validateRoles(Set<Long> roleIds) {
        if (roleIds == null) {
            throw new DomainValidationException("roleIds must not be null");
        }
        Set<Long> cleaned = new HashSet<>();
        for (Long r : roleIds) {
            if (r == null) {
                throw new DomainValidationException("roleIds cannot contain null");
            }
            cleaned.add(r);
        }
        if (cleaned.isEmpty()) {
            throw new DomainValidationException("at least one roleId is required");
        }
        return Collections.unmodifiableSet(cleaned);
    }

    public Long getId() { return id; }
    public String getFname() { return fname; }
    public String getLname() { return lname; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public Long getVendorProfileId() { return vendorProfileId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public User withUpdatedProfile(String fname, String lname) {
        Instant now = Instant.now();
        return new User(this.id, fname, lname, this.email, this.password, this.role, this.vendorProfileId, this.createdAt, now);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}

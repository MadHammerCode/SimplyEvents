package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.UserRole;
import at.fhv.simplyevents.rest.dto.UserSummary; // <--- Import the new DTO
import at.fhv.simplyevents.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;

    public AdminRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        // Get currently logged-in email
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        List<UserSummary> list = userService.getAllUsers().stream()
                .map(u -> new UserSummary(
                        u.getId(),
                        u.getFname(),
                        u.getLname(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.getEmail().equals(currentEmail) // <--- Check if matches
                ))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/promote")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> promoteUser(@RequestParam Long userId, @RequestParam UserRole role) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> targetUser = userService.getAllUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst();

        if (targetUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Check if the target user's email matches the logged-in admin's email
        if (targetUser.get().getEmail().equals(currentEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cannot change your own role.");
        }

        userService.changeUserRole(userId, role);
        return ResponseEntity.ok("User " + userId + " updated to " + role);
    }
}
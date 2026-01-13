package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.domain.model.UserRole;
import at.fhv.simplyevents.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;

    public AdminRestController(UserService userService) {
        this.userService = userService;
    }

    // Usage: POST /api/admin/promote?userId=5&role=BACKOFFICE
    @PostMapping("/promote")
    @PreAuthorize("hasAuthority('ADMIN')") // Extra security check
    public ResponseEntity<String> promoteUser(@RequestParam Long userId, @RequestParam UserRole role) {
        userService.changeUserRole(userId, role);
        return ResponseEntity.ok("User " + userId + " promoted to " + role);
    }
}
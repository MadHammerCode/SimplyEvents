package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.UserUseCase;
import at.fhv.simplyevents.rest.dto.UserProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getCurrentUser(Authentication authentication) {
        logger.debug("GET /api/users/me called, authentication={}", authentication);
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            logger.info("Unauthorized access to /api/users/me");
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        logger.info("Fetching profile for principal/email={}", email);
        try {
            var result = userUseCase.getUserProfile(email);
            return ResponseEntity.ok(toDto(result));
        } catch (NotFoundException e) {
            logger.warn("User not found for email={}", email);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileDto> updateCurrentUser(Authentication authentication, @RequestBody UserProfileDto dto) {
        logger.debug("PUT /api/users/me called, authentication={}, dto={}", authentication, dto);
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            logger.info("Unauthorized attempt to update /api/users/me");
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        try {
            var result = userUseCase.updateUserProfile(email, toCommand(dto));
            return ResponseEntity.ok(toDto(result));
        } catch (NotFoundException e) {
            logger.warn("User not found during update for email={}", email);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating profile for email={}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    private UserProfileDto toDto(UserUseCase.UserProfileResult r) {
        return new UserProfileDto(r.firstName(), r.lastName(), r.email(), r.phone(), r.address(), r.role());
    }

    private UserUseCase.UpdateUserProfileCommand toCommand(UserProfileDto dto) {
        return new UserUseCase.UpdateUserProfileCommand(dto.getFirstName(), dto.getLastName(), dto.getPhone(), dto.getAddress());
    }
}

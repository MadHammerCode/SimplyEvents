package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.AuthDtos;
import at.fhv.simplyevents.service.AuthService;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    // simple DTO returned to frontend (no password)
    public static record UserResponseDTO(Long id, String name, String email, java.util.Set<String> roles, String role) {}

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody AuthDtos.RegisterCustomerDTO dto) {
        User u = auth.registerCustomer(dto);
        return ResponseEntity.ok(toDto(u));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDtos.RegisterCustomerDTO dto) {
        User u = auth.registerCustomer(dto);
        return ResponseEntity.ok(toDto(u));
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<?> registerVendor(@RequestBody AuthDtos.RegisterVendorDTO dto) {
        User u = auth.registerVendor(dto);
        return ResponseEntity.ok(toDto(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDtos.LoginDTO dto, HttpServletRequest request) {
        User u = auth.login(dto);

        // build authorities from roles
        Set<SimpleGrantedAuthority> authorities = u.getRoles().stream()
                .map(Role::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(u.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // store security context in session so subsequent requests are authenticated
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return ResponseEntity.ok(toDto(u));
    }

    private UserResponseDTO toDto(User u) {
        Set<String> roleNames = u.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        String primary = null;
        if (roleNames.contains("ROLE_VENDOR")) primary = "BACKOFFICE";
        else if (roleNames.contains("ROLE_FRONTOFFICE")) primary = "FRONTOFFICE";
        else if (roleNames.contains("ROLE_CUSTOMER")) primary = "CUSTOMER";
        else if (!roleNames.isEmpty()) {
            // fallback: strip ROLE_ prefix if present
            String any = roleNames.iterator().next();
            primary = any.startsWith("ROLE_") ? any.substring(5) : any;
        }
        return new UserResponseDTO(u.getId(), u.getName(), u.getEmail(), roleNames, primary);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}

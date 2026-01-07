package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.port.in.AuthUseCase;
import at.fhv.simplyevents.application.port.in.AuthUseCase.LoginCommand;
import at.fhv.simplyevents.application.port.in.AuthUseCase.RegisterCustomerCommand;
import at.fhv.simplyevents.application.port.in.AuthUseCase.RegisterVendorCommand;
import at.fhv.simplyevents.domain.repository.RoleRepositoryPort;
import at.fhv.simplyevents.rest.dto.AuthDtos;
import at.fhv.simplyevents.domain.model.Role;
import at.fhv.simplyevents.domain.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
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

    private final AuthUseCase auth;
    private final RoleRepositoryPort roles;

    public AuthController(AuthUseCase auth, RoleRepositoryPort roles) {
        this.auth = auth;
        this.roles = roles;
    }

    // simple DTO returned to frontend (no password); expose firstName and lastName separately
    public static record UserResponseDTO(Long id, String firstName, String lastName, String email, java.util.Set<String> roles, String role) {}

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody AuthDtos.RegisterCustomerDTO dto) {
        User u = auth.registerCustomer(new RegisterCustomerCommand(dto.firstName(), dto.lastName(), dto.email(), dto.password()));
        return ResponseEntity.ok(toDto(u));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDtos.RegisterCustomerDTO dto) {
        User u = auth.registerCustomer(new RegisterCustomerCommand(dto.firstName(), dto.lastName(), dto.email(), dto.password()));
        return ResponseEntity.ok(toDto(u));
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<?> registerVendor(@RequestBody AuthDtos.RegisterVendorDTO dto) {
        User u = auth.registerVendor(new RegisterVendorCommand(dto.firstName(), dto.lastName(), dto.email(), dto.password(), dto.companyId(), dto.contactInfo()));
        return ResponseEntity.ok(toDto(u));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDtos.LoginDTO dto, HttpServletRequest request) {
        User u = auth.login(new LoginCommand(dto.email(), dto.password()));

        // build authorities from roles
        Set<SimpleGrantedAuthority> authorities = u.getRoleIds().stream()
                .map(rid -> roles.findById(rid))
                .filter(Optional::isPresent)
                .map(Optional::get)
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
        Set<String> roleNames = u.getRoleIds().stream()
                .map(rid -> roles.findById(rid))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Role::getName)
                .collect(Collectors.toSet());
        String primary = null;
        if (roleNames.contains("ROLE_VENDOR")) primary = "BACKOFFICE";
        else if (roleNames.contains("ROLE_FRONTOFFICE")) primary = "FRONTOFFICE";
        else if (roleNames.contains("ROLE_CUSTOMER")) primary = "CUSTOMER";
        else if (!roleNames.isEmpty()) {
            // fallback: strip ROLE_ prefix if present
            String any = roleNames.iterator().next();
            primary = any.startsWith("ROLE_") ? any.substring(5) : any;
        }
        return new UserResponseDTO(u.getId(), u.getFname(), u.getLname(), u.getEmail(), roleNames, primary);
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

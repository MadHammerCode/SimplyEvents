package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.AuthDtos;
import at.fhv.simplyevents.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody AuthDtos.RegisterCustomerDTO dto) {
        auth.registerCustomer(dto);
        return ResponseEntity.ok("Registered customer");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDtos.RegisterCustomerDTO dto) {
        auth.registerCustomer(dto);
        return ResponseEntity.ok("Registered!");
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<?> registerVendor(@RequestBody AuthDtos.RegisterVendorDTO dto) {
        auth.registerVendor(dto);
        return ResponseEntity.ok("Registered vendor");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDtos.LoginDTO dto) {
        auth.login(dto);
        return ResponseEntity.ok("Login successful");
    }
}

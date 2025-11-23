package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.AccountDtos;
import at.fhv.simplyevents.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AccountDtos.RegisterDTO dto) {
        auth.register(dto);
        return ResponseEntity.ok("Registered!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AccountDtos.LoginDTO dto) {
        auth.login(dto);
        return ResponseEntity.ok("Login successful!");
    }
}


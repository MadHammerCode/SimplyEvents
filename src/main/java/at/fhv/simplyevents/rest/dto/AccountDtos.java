package at.fhv.simplyevents.rest.dto;
import jakarta.validation.constraints.*;

public class AccountDtos {
    public record RegisterDTO(
            String name,
            @NotBlank(message = "Email must not be blank")
            @Email(message = "Email should be valid")
            String email,
            @NotBlank(message = "Password must not be blank")
            String password) {}

    public record LoginDTO(
            @NotBlank(message = "Email must not be blank")
            @Email(message = "Email should be valid")
            String email,
            @NotBlank(message = "Password must not be blank")
            String password) {}


}

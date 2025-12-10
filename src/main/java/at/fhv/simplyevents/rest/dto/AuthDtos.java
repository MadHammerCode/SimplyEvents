package at.fhv.simplyevents.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record RegisterCustomerDTO(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record RegisterVendorDTO(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String companyId,
            String contactInfo
    ) {}

    public record LoginDTO(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}
}


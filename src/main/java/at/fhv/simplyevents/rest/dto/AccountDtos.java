package at.fhv.simplyevents.rest.dto;

public class AccountDtos {
    public record RegisterDTO(String name, String email, String password) {}

    public record LoginDTO(String email, String password) {}


}

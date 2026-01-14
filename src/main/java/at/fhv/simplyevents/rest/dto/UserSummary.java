package at.fhv.simplyevents.rest.dto;

public record UserSummary(Long id,
                          String firstName,
                          String lastName,
                          String email,
                          String role,
                          boolean isCurrentUser) {

}

package at.fhv.simplyevents.rest.dto;

public final class CheckInRestDtos {
    private CheckInRestDtos() {}

    public record ParticipantCreateDto(String firstName, String lastName, String email) {}
    public record CheckInUpdateDto(boolean checkedIn) {}
    public record NewBookingRequestDto(String firstName, String lastName, String email, int seats) {}
}


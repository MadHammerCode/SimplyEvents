package at.fhv.simplyevents.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;


public class EventDtos {

    public record CreateEventRequest(
            @NotBlank String title,
            @NotBlank String date,
            @NotBlank String time,
            @NotBlank String location,

            BigDecimal price,
            Integer minParticipants,
            Integer maxParticipants,
            Integer durationHours,

            String category,
            String description,
            String equipmentNeeded,
            String requirements,

            String cancellationDeadline,
            String bookingStart,
            String bookingEnd,
            Boolean yearRound
    ) {}


    public record EventResponse(
            Long id,

            String title,
            String date,
            String time,
            String location,

            BigDecimal price,

            Integer minParticipants,
            Integer maxParticipants,
            Integer availableSlots,
            Integer durationHours,

            String category,
            String description,
            String equipmentNeeded,
            String requirements,

            String cancellationDeadline,
            String imagePath,
            String bookingStart,
            String bookingEnd,
            Boolean yearRound
    ) {}
}

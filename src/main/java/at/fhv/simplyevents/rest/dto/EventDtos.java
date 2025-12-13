package at.fhv.simplyevents.rest.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.lang.String;


public class EventDtos {

    public record CreateEventRequest(
            @NotBlank(message = "Title must not be blank")
            String title,
            String date,
            String time,
            @NotBlank (message = "Location must not be blank")
            String location,

            @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than 0")
            BigDecimal price,
            @Min(value = 0, message = "Minimum participants must be at least 0")
            Integer minParticipants,
            @Min(value = 1, message = "Maximum participants must be at least 1")
            Integer maxParticipants,
            Integer durationHours,

            String category,
            @Size(max = 2000, message = "Description must not exceed 2000 characters")
            String description,
            String equipmentNeeded,
            String requirements,

            String cancellationDeadline,
            String bookingStart,
            String bookingEnd,
            Boolean yearRound,
            Boolean confirmPast,
            Integer capacity,
            String imagePath
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
            Integer capacity,

            String category,
            String description,
            String equipmentNeeded,
            String requirements,

            String cancellationDeadline,
            String imagePath,
            String bookingStart,
            String bookingEnd,
            Boolean yearRound,
            String status
    ) {}
}

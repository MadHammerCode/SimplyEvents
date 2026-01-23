package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.EventStatus;
import at.fhv.simplyevents.persistence.model.EventJpaEntity;

public class EventMapper {

    private EventMapper() {}

    public static Event toDomain(EventJpaEntity entity) {
        if (entity == null) return null;
        Event event = Event.createDraft();
        event.applyDetails(
                entity.getTitle(),
                entity.getCategory(),
                entity.getPrice(),
                entity.getMinParticipants(),
                entity.getMaxParticipants(),
                entity.getRequirements(),
                entity.getEquipmentNeeded(),
                entity.getLocation(),
                entity.getDurationHours(),
                entity.getDate(),
                entity.getAvailableSlots(),
                entity.getDescription(),
                entity.getCancellationDeadline(),
                entity.getYearRound(),
                entity.getBookingStart(),
                entity.getBookingEnd(),
                entity.getImagePath(),
                entity.getStatus(),
                entity.getCancelled(),
                entity.getTime() // <--- Now accepted by Event.java
        );
        event.loadIdentifiers(entity.getEventId(), entity.getVendorProfileId(), entity.getBookingId());
        return event;
    }

    public static EventJpaEntity toEntity(Event domain) {
        if (domain == null) return null;
        EventJpaEntity entity = new EventJpaEntity();
        entity.setEventId(domain.getEventId());
        entity.setTitle(domain.getTitle());
        entity.setCategory(domain.getCategory());
        entity.setPrice(domain.getPrice());
        entity.setMinParticipants(domain.getMinParticipants());
        entity.setMaxParticipants(domain.getMaxParticipants());
        entity.setRequirements(domain.getRequirements());
        entity.setEquipmentNeeded(domain.getEquipmentNeeded());
        entity.setLocation(domain.getLocation());
        entity.setDurationHours(domain.getDurationHours());
        entity.setDate(domain.getDate());
        entity.setTime(domain.getTime()); // <--- Persist Time
        entity.setAvailableSlots(domain.getAvailableSlots());
        entity.setDescription(domain.getDescription());
        entity.setCancellationDeadline(domain.getCancellationDeadline());
        entity.setImagePath(domain.getImagePath());
        entity.setStatus(domain.getStatus() == null ? EventStatus.PLANNED : domain.getStatus());
        entity.setYearRound(domain.isYearRound());
        entity.setBookingStart(domain.getBookingStart());
        entity.setBookingEnd(domain.getBookingEnd());
        entity.setVendorProfileId(domain.getVendorProfileId());
        entity.setBookingId(domain.getBookingId());
        entity.setCancelled(domain.isCancelled());
        return entity;
    }
}
package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.Participant;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.persistence.ParticipantRepository;
import at.fhv.simplyevents.persistence.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final ParticipantRepository participants;
    private final EventRepository events;

    public CheckInService(ParticipantRepository participants, EventRepository events) {
        this.participants = participants;
        this.events = events;
    }

    public static record ParticipantDTO(Long id, String firstName, String lastName, boolean checkedIn) {}

    public List<ParticipantDTO> findParticipantsForEvent(Long eventId) {
        Event event = events.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        return participants.findByEventEventId(eventId).stream()
                .map(p -> new ParticipantDTO(p.getId(), p.getFirstName(), p.getLastName(), p.isCheckedIn()))
                .collect(Collectors.toList());
    }

    public ParticipantDTO updateCheckedIn(Long participantId, boolean checkedIn) {
        Participant p = participants.findById(participantId).orElseThrow(() -> new NoSuchElementException("Participant not found"));
        p.setCheckedIn(checkedIn);
        p.setCheckInTime(checkedIn ? Instant.now() : null);
        participants.save(p);
        return new ParticipantDTO(p.getId(), p.getFirstName(), p.getLastName(), p.isCheckedIn());
    }
}


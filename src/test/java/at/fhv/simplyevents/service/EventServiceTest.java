package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.port.in.EventUseCase.CreateEventCommand;
import at.fhv.simplyevents.application.port.out.FileStoragePort;
import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.EventStatus;
import at.fhv.simplyevents.domain.repository.EventRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepositoryPort eventRepository;

    @Mock
    private FileStoragePort fileStoragePort;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, fileStoragePort);
    }

    @Test
    void createEvent_should_create_draft_with_minimal_data() {
        // Arrange: Only Title is provided, everything else null
        CreateEventCommand cmd = new CreateEventCommand(
                "My Draft Event", null, null, null, null,
                null, null, null, "Other", null,
                null, null, null, null, null,
                false, false, null, null, false, // publishNow = false
                null
        );

        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        var result = eventService.createEvent(cmd);

        // Assert
        assertNotNull(result);
        assertEquals("My Draft Event", result.title());
        assertEquals("PLANNED", result.status());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_should_fail_publishing_without_required_fields() {
        // Arrange: Try to publish with ONLY a title (should fail cause of strict validation)
        CreateEventCommand cmd = new CreateEventCommand(
                "Incomplete Event", null, null, null, null,
                null, null, null, "Music", null,
                null, null, null, null, null,
                false, false, null, null, true,
                null
        );

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(cmd);
        });

        // Check that we get a meaningful error message
        assertTrue(exception.getMessage().contains("Location is required"), "Should complain about missing location");
    }

    @Test
    void updateEvent_should_allow_updating_draft_without_validation() {
        // Arrange: Existing event is PLANNED
        Long eventId = 1L;
        Event existingDraft = Event.createDraft();
        // applyDetails with minimal info to simulate a draft in DB
        existingDraft.applyDetails("Old Title", "Music", 10.0, 0, 100, "", "", "Loc", 2, null, 100, "Desc", null, false, null, null, null, EventStatus.PLANNED, false, null);
        existingDraft.loadIdentifiers(eventId, 1L, null);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingDraft));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArguments()[0]);

        CreateEventCommand updateCmd = new CreateEventCommand(
                "New Title", null, null, null, null,
                null, null, null, "Music", null,
                null, null, null, null, null,
                false, false, null, null, false, // publishNow = false
                EventStatus.PLANNED.name()
        );

        // Act
        var result = eventService.updateEvent(eventId, updateCmd);

        // Assert
        assertEquals("New Title", result.title());
        assertEquals("PLANNED", result.status());
    }
}
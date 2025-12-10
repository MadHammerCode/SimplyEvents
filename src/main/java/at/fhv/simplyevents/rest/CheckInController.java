package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.service.CheckInService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    @GetMapping("/event/{eventId}/participants")
    public ResponseEntity<List<CheckInService.ParticipantDTO>> participantsForEvent(@PathVariable Long eventId) {
        List<CheckInService.ParticipantDTO> list = service.findParticipantsForEvent(eventId);
        return ResponseEntity.ok(list);
    }

    public static record CheckInUpdateDTO(boolean checkedIn) {}

    @PutMapping("/participants/{id}")
    public ResponseEntity<?> updateParticipant(@PathVariable Long id, @RequestBody CheckInUpdateDTO dto) {
        var updated = service.updateCheckedIn(id, dto.checkedIn());
        return ResponseEntity.ok(updated);
    }
}


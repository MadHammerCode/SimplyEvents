package at.fhv.Authors.rest;

import at.fhv.Authors.domain.Author;
import at.fhv.Authors.domain.model.Event;
import at.fhv.Authors.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventRestController {
    private final EventService eventService;

    public EventRestController(EventService eventService){
        this.eventService = eventService;
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event){
        return eventService.createEvent(event);
    }

    @GetMapping
    public List<Event> getAllEvents(){
        return eventService.getAllEvents();
    }
}

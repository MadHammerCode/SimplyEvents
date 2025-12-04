package at.fhv.simplyevents.controller;

import at.fhv.simplyevents.service.EventService;
import at.fhv.simplyevents.rest.dto.EventDtos.EventResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    private final EventService eventService;

    public ViewController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/dashboard")
    public String authorsView() {
        return "Dashboard"; }


    @GetMapping("/events")
    public String eventList() {
        return "eventlist"; }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; }

    @GetMapping("/booking")
    public String showBookingPage() {
        return "booking";
    }

    @GetMapping("/cancel-booking")
    public String showCancelBookingPage() {
        return "cancel-booking";
    }

    @GetMapping("/event-details/{id}")
    public String showEventDetails(@PathVariable Long id, Model model) {
        try {
            EventResponse event = eventService.getEventById(id);
            model.addAttribute("event", event);
            model.addAttribute("message", "Super — hier sind die Event‑Details. 🎉 Jetzt kannst du es buchen!");
            model.addAttribute("messageType", "info");
        } catch (Exception e) {
            model.addAttribute("event", null);
            model.addAttribute("message", "Ups — das Event konnte nicht geladen werden. 😕 Versuch's später oder geh zurück zur Übersicht.");
            model.addAttribute("messageType", "error");
        }
        return "event-details";
    }

    @GetMapping("/create-event")
    public String showCreateEventPage() {
        return "create-event";
    }
}

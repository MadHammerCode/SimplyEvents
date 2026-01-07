package at.fhv.simplyevents.controller;

import at.fhv.simplyevents.application.port.in.EventUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ViewController {

    private final EventUseCase eventUseCase;

    public ViewController(EventUseCase eventUseCase) {
        this.eventUseCase = eventUseCase;
    }

    @GetMapping("/dashboard")
    public String authorsView() {
        return "Dashboard"; }


    @GetMapping("/events")
    public String eventList() {
        // There is no 'eventlist' template in src/main/resources/templates; use 'landing' which exists
        return "landing"; }

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
        // Load event and add to model so the template can render its details
        var eventResponse = eventUseCase.getEventById(id);
        model.addAttribute("event", eventResponse);
        return "event-details";
    }

    // Unified create/edit event page: use the same editor view for creating and editing events
    @GetMapping("/create-event")
    public String createEventPage() { return "event-editor"; }

    @GetMapping("/edit-event/{id}")
    public String editEventPage(@PathVariable Long id, Model model) {
        model.addAttribute("eventId", id);
        return "event-editor";
    }

    @GetMapping("/backoffice-dashboard")
    public String backofficeDashboard() {
        return "backoffice-dashboard";
    }

    @GetMapping("/frontoffice-checkin")
    public String frontofficeCheckin() {
        return "frontoffice-checkin";
    }

    @GetMapping("/user-profile")
    public String userProfile() {
        return "user-profile";}

    @GetMapping("/my-bookings")
    public String myBookings() {
        return "my-bookings";}
}

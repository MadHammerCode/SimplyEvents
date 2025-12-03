package at.fhv.simplyevents.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    
    @GetMapping("/dashboard")
    public String authorsView() {
        return "Dashboard";
    }
    
    @GetMapping("/event-details")
    public String showEventDetailsPage() {
        return "event-details";
    }
    
    @GetMapping("/booking")
    public String showBookingPage() {
        return "booking";
    }
    
    @GetMapping("/cancel-booking")
    public String showCancelBookingPage() {
        return "cancel-booking";
    }
    
    @GetMapping("/events")
    public String eventList() {
        return "EventList";
    }
    
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }
}

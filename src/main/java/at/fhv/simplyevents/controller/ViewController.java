package at.fhv.simplyevents.controller;

import at.fhv.simplyevents.application.port.in.EventUseCase;
import at.fhv.simplyevents.application.port.in.BookingUseCase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ViewController {

    private final EventUseCase eventUseCase;
    private final BookingUseCase bookingUseCase;

    public ViewController(EventUseCase eventUseCase, BookingUseCase bookingUseCase) {
        this.eventUseCase = eventUseCase;
        this.bookingUseCase = bookingUseCase;
    }

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String authorsView() {
        return "Dashboard"; }


    @GetMapping("/events")
    public String eventList() {
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
        var eventResponse = eventUseCase.getEventById(id);

        if (Boolean.TRUE.equals(eventResponse.cancelled())) {
            return "redirect:/events";
        }

        model.addAttribute("event", eventResponse);
        return "event-details";
    }

    @GetMapping("/create-event")
    public String createEventPage() { return "event-editor"; }

    @GetMapping("/edit-event/{id}")
    public String editEvent(@PathVariable Long id, org.springframework.ui.Model model) {
        return "event-editor";
    }

    @GetMapping("/backoffice-dashboard")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'BACKOFFICE')")
    public String backofficeDashboard() {
        return "backoffice-dashboard";
    }

    @GetMapping("/frontoffice-checkin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FRONTOFFICE')")
    public String frontofficeCheckin() {
        return "frontoffice-checkin";
    }

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'FRONTOFFICE', 'BACKOFFICE')")
    public String invoices() {
        return "invoices";
    }

    @GetMapping("/user-profile")
    public String userProfile() {
        return "user-profile";}

    @GetMapping("/my-bookings")
    public String myBookings() {
        return "my-bookings";}

    @GetMapping("/booking-ticket/{id}")
    public String showBookingTicket(@PathVariable Long id, Model model) {
        try {
            var booking = bookingUseCase.getBooking(id);
            var eventInfo = eventUseCase.getEventById(booking.getEventId());

            // Create a map with all booking information including event details
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("bookingNumber", booking.getBookingNumber());
            bookingData.put("eventTitle", eventInfo.title());
            bookingData.put("date", eventInfo.date());
            bookingData.put("time", eventInfo.time());
            bookingData.put("location", eventInfo.location());
            bookingData.put("numParticipants", booking.getNumParticipants());
            bookingData.put("priceTotal", booking.getPriceTotal());
            bookingData.put("eventId", booking.getEventId());

            model.addAttribute("booking", bookingData);
            return "booking-ticket";
        } catch (Exception e) {
            return "redirect:/my-bookings";
        }
    }

    @GetMapping("/booking-details/{id}")
    public String showBookingDetails(@PathVariable Long id, Model model) {
        try {
            var booking = bookingUseCase.getBooking(id);
            var eventInfo = eventUseCase.getEventById(booking.getEventId());

            // Create a map with all booking information including event details
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("bookingNumber", booking.getBookingNumber());
            bookingData.put("eventTitle", eventInfo.title());
            bookingData.put("date", eventInfo.date());
            bookingData.put("time", eventInfo.time());
            bookingData.put("location", eventInfo.location());
            bookingData.put("numParticipants", booking.getNumParticipants());
            bookingData.put("priceTotal", booking.getPriceTotal());
            bookingData.put("eventId", booking.getEventId());

            model.addAttribute("booking", bookingData);
            return "booking-details";
        } catch (Exception e) {
            return "redirect:/my-bookings";
        }
    }

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/wishlist")
    public String wishlist() {
        return "wishlist";
    }
}

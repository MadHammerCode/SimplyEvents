package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.rest.dto.EventDtos.EventResponse;
import at.fhv.simplyevents.service.EventService;
import at.fhv.simplyevents.service.WishlistService;
import at.fhv.simplyevents.domain.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private static final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    private final WishlistService wishlistService;
    private final EventService eventService;

    public WishlistController(WishlistService wishlistService, EventService eventService) {
        this.wishlistService = wishlistService;
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getWishlist(Authentication authentication) {
        logger.debug("GET /api/wishlist called, auth={}", authentication);
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        try {
            List<Event> events = wishlistService.getEvents(email);
            List<EventResponse> dtos = events.stream()
                    .map(e -> {
                        try {
                            return eventService.getEventById(e.getEventId());
                        } catch (Exception ex) {
                            // If mapping fails (e.g. booking window), skip this event
                            logger.warn("Skipping event {} while converting wishlist: {}", e.getEventId(), ex.getMessage());
                            return null;
                        }
                    })
                    .filter(r -> r != null)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getWishlistIds(Authentication authentication) {
        logger.debug("GET /api/wishlist/ids called, auth={}", authentication);
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        try {
            List<Event> events = wishlistService.getEvents(email);
            List<Long> ids = events.stream()
                    .map(Event::getEventId)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ids);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> toggleEvent(@PathVariable Long eventId, Authentication authentication) {
        logger.debug("POST /api/wishlist/{} called, auth={}", eventId, authentication);
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        try {
            wishlistService.toggleEvent(email, eventId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}


package at.fhv.simplyevents.service;

import at.fhv.simplyevents.domain.model.Event;
import at.fhv.simplyevents.domain.model.User;
import at.fhv.simplyevents.domain.model.Wishlist;
import at.fhv.simplyevents.persistence.EventRepository;
import at.fhv.simplyevents.persistence.UserRepository;
import at.fhv.simplyevents.persistence.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public WishlistService(WishlistRepository wishlistRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Wishlist getWishlist(String email) {
        Optional<Wishlist> maybe = wishlistRepository.findByEndUser_Email(email);
        if (maybe.isPresent()) return maybe.get();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));

        Wishlist wishlist = new Wishlist();
        wishlist.setEndUser(user);
        Wishlist saved = wishlistRepository.save(wishlist);
        return saved;
    }

    @Transactional
    public void toggleEvent(String email, Long eventId) {
        Wishlist wishlist = getWishlist(email);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        if (wishlist.containsEvent(event)) {
            wishlist.removeEvent(event);
        } else {
            wishlist.addEvent(event);
        }
        wishlistRepository.save(wishlist);
    }

    @Transactional(readOnly = true)
    public List<Event> getEvents(String email) {
        Wishlist wishlist = getWishlist(email);
        return List.copyOf(wishlist.getEvents());
    }
}

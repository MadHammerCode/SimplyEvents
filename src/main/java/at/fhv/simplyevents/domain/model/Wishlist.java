package at.fhv.simplyevents.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "wishlists")
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wishlistId;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User endUser;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "wishlist_events",
        joinColumns = @JoinColumn(name = "wishlist_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private Set<Event> events = new HashSet<>();

    private LocalDate createdAt = LocalDate.now();

    // Getters and Setters...
    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }
    public User getEndUser() { return endUser; }
    public void setEndUser(User endUser) { this.endUser = endUser; }
    public Set<Event> getEvents() { return events; }
    public void setEvents(Set<Event> events) { this.events = events; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    // Helper Methods
    public void addEvent(Event event) { this.events.add(event); }
    public void removeEvent(Event event) { this.events.remove(event); }

    public boolean containsEvent(Event event) {
        if (event == null || event.getEventId() == null) return false;
        return this.events.stream().anyMatch(e -> e.getEventId().equals(event.getEventId()));
    }
}
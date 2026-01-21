package at.fhv.simplyevents.persistence.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wishlists")
public class WishlistJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private LocalDate createdAt;

    // Stores the list of IDs in a separate sub-table
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wishlist_events", joinColumns = @JoinColumn(name = "wishlist_id"))
    @Column(name = "event_id")
    private List<Long> eventIds = new ArrayList<>();

    public WishlistJpaEntity() {
    }

    public WishlistJpaEntity(Long userId, List<Long> eventIds, LocalDate createdAt) {
        this.userId = userId;
        this.eventIds = eventIds;
        this.createdAt = createdAt;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
    public List<Long> getEventIds() { return eventIds; }
    public void setEventIds(List<Long> eventIds) { this.eventIds = eventIds; }
}
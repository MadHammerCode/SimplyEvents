package at.fhv.simplyevents.persistence.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "wishlist")
public class WishlistJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_id")
    private Long wishlistId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at")
    private LocalDate createdAt;

    // No join to events here; manage linking table separately if needed

    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }
}


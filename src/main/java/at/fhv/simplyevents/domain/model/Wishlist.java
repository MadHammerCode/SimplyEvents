package at.fhv.simplyevents.domain.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class Wishlist {

    private Long wishlistId;
    private Long userId;
    private List<Long> eventIds;
    private LocalDate createdAt;

    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public List<Long> getEventIds() { return eventIds; }
    public void setEventIds(List<Long> eventIds) { this.eventIds = eventIds; }
    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wishlist)) return false;
        Wishlist wishlist = (Wishlist) o;
        return Objects.equals(wishlistId, wishlist.wishlistId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wishlistId);
    }
}

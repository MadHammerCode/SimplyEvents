package at.fhv.simplyevents.application.port.in.dto;

import java.util.List;

public interface WishlistUseCase {
    List<Long> getWishlistEventIds(Long userId);
    void toggleWishlist(Long userId, Long eventId);
}
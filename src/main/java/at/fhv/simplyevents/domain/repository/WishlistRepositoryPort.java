package at.fhv.simplyevents.domain.repository;

import at.fhv.simplyevents.domain.model.Wishlist;
import java.util.Optional;

public interface WishlistRepositoryPort {
    Optional<Wishlist> findByUserId(Long userId);
    void save(Wishlist wishlist);
}
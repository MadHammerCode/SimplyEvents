package at.fhv.simplyevents.service;

import at.fhv.simplyevents.application.exception.NotFoundException;
import at.fhv.simplyevents.application.port.in.dto.WishlistUseCase;
import at.fhv.simplyevents.domain.model.Wishlist;
import at.fhv.simplyevents.domain.repository.WishlistRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WishlistService implements WishlistUseCase {

    private final WishlistRepositoryPort wishlistRepository;

    public WishlistService(WishlistRepositoryPort wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    public List<Long> getWishlistEventIds(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .map(Wishlist::getEventIds)
                .orElse(new ArrayList<>());
    }

    @Override
    @Transactional
    public void toggleWishlist(Long userId, Long eventId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyWishlist(userId));

        List<Long> ids = wishlist.getEventIds();
        if (ids == null) ids = new ArrayList<>();

        if (ids.contains(eventId)) {
            ids.remove(eventId);
        } else {
            ids.add(eventId);
        }

        wishlist.setEventIds(ids);
        wishlistRepository.save(wishlist);
    }

    private Wishlist createEmptyWishlist(Long userId) {
        Wishlist w = new Wishlist();
        w.setUserId(userId);
        w.setEventIds(new ArrayList<>());
        w.setCreatedAt(LocalDate.now());
        // Note: ID generation usually happens in persistence layer or here if UUID
        return w;
    }
}
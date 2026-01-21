package at.fhv.simplyevents.infrastructure.jpa.adapter;
import at.fhv.simplyevents.domain.model.Wishlist;
import at.fhv.simplyevents.domain.repository.WishlistRepositoryPort;
import at.fhv.simplyevents.persistence.model.WishlistJpaEntity;
import at.fhv.simplyevents.persistence.springdata.WishlistRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // <--- This annotation solves the "Could not autowire" error
public class WishlistRepositoryAdapter implements WishlistRepositoryPort {

    private final WishlistRepository jpaRepository;

    public WishlistRepositoryAdapter(WishlistRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Wishlist> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public void save(Wishlist wishlist) {
        WishlistJpaEntity entity = jpaRepository.findByUserId(wishlist.getUserId())
                .orElse(new WishlistJpaEntity());

        // Map Domain -> Entity
        entity.setUserId(wishlist.getUserId());
        entity.setCreatedAt(wishlist.getCreatedAt());
        entity.setEventIds(wishlist.getEventIds());

        WishlistJpaEntity saved = jpaRepository.save(entity);

        // Update Domain ID just in case it was new
        wishlist.setWishlistId(saved.getId());
    }

    private Wishlist toDomain(WishlistJpaEntity entity) {
        Wishlist w = new Wishlist();
        w.setWishlistId(entity.getId());
        w.setUserId(entity.getUserId());
        w.setCreatedAt(entity.getCreatedAt());
        w.setEventIds(entity.getEventIds());
        return w;
    }
}

package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.WishlistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistJpaEntity, Long> {
    Optional<WishlistJpaEntity> findByUserId(Long userId);
}
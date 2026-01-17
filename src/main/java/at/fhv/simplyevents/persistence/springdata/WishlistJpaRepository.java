package at.fhv.simplyevents.persistence.springdata;

import at.fhv.simplyevents.persistence.model.WishlistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WishlistJpaRepository extends JpaRepository<WishlistJpaEntity, Long> {
}


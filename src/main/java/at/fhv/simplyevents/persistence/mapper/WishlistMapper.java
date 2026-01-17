package at.fhv.simplyevents.persistence.mapper;

import at.fhv.simplyevents.domain.model.Wishlist;
import at.fhv.simplyevents.persistence.model.WishlistJpaEntity;

import java.util.Collections;

public class WishlistMapper {
    private WishlistMapper() {}

    public static Wishlist toDomain(WishlistJpaEntity e) {
        if (e == null) return null;
        Wishlist w = new Wishlist();
        w.setWishlistId(e.getWishlistId());
        w.setUserId(e.getUserId());
        w.setCreatedAt(e.getCreatedAt());
        // eventIds mapping skipped (separate join table not modeled here)
        w.setEventIds(Collections.emptyList());
        return w;
    }

    public static WishlistJpaEntity toEntity(Wishlist w) {
        if (w == null) return null;
        WishlistJpaEntity e = new WishlistJpaEntity();
        e.setWishlistId(w.getWishlistId());
        e.setUserId(w.getUserId());
        e.setCreatedAt(w.getCreatedAt());
        return e;
    }
}


package at.fhv.simplyevents.rest.dto;
import at.fhv.simplyevents.application.port.in.dto.WishlistUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistRestController {

    private final WishlistUseCase wishlistUseCase;

    public WishlistRestController(WishlistUseCase wishlistUseCase) {
        this.wishlistUseCase = wishlistUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Long>> getWishlist(@RequestParam Long userId) {
        return ResponseEntity.ok(wishlistUseCase.getWishlistEventIds(userId));
    }

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> toggleItem(
            @PathVariable Long eventId,
            @RequestParam Long userId
    ) {
        wishlistUseCase.toggleWishlist(userId, eventId);
        return ResponseEntity.ok().build();
    }
}
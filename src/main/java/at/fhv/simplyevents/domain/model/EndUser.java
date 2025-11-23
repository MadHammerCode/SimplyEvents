package at.fhv.simplyevents.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.util.List;

@Entity
public class EndUser extends Account {

    private String address;

    @OneToMany(mappedBy = "endUser")
    private List<Booking> bookings;

    @OneToOne(mappedBy = "endUser", cascade = CascadeType.ALL)
    private Wishlist wishlist;   // Or List<Wishlist> if 1:N
}

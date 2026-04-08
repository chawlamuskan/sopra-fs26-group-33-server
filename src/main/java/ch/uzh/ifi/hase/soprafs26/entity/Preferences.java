package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;	

@Entity
@Table(name = "preferences")
public class Preferences implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column
    private String profilePictureURL;

    @ElementCollection
    @CollectionTable(name = "visited_countries", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "country")
    private List<String> visitedCountries;

    @ElementCollection
    @CollectionTable(name = "wishlist_countries", joinColumns = @JoinColumn(name = "preferences_id"))
    @Column(name = "country")
    private List<String> wishlistCountries;
    

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getProfilePictureURL() { return profilePictureURL; }
    public void setProfilePictureURL(String profilePictureURL) { this.profilePictureURL = profilePictureURL; }

    public List<String> getVisitedCountries() { return visitedCountries; }
    public void setVisitedCountries(List<String> visitedCountries) { this.visitedCountries = visitedCountries; }

    public List<String> getWishlistCountries() { return wishlistCountries; }
    public void setWishlistCountries(List<String> wishlistCountries) { this.wishlistCountries = wishlistCountries; }
    
}

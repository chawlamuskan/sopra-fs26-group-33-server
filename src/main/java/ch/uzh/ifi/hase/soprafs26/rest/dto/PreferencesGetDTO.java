package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class PreferencesGetDTO {
    private Long id;
    private String bio;
    private String profilePicture;
    private List<String> visitedCountries;
    private List<String> wishlistCountries;
    private List<Long> friends;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio;}

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture;}

    public List<String> getVisitedCountries() { return visitedCountries;}
    public void setVisitedCountries(List<String> visitedCountries) { this.visitedCountries = visitedCountries;}

    public List<String> getWishlistCountries() { return wishlistCountries; }
    public void setWishlistCountries(List<String> wishlistCountries) { this.wishlistCountries = wishlistCountries; }

    public List<Long> getFriends() { return friends; }
    public void setFriends(List<Long> friends) { this.friends = friends; }

}

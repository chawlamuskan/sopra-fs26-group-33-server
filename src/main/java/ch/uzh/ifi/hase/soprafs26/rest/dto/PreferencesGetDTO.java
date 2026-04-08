package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class PreferencesGetDTO {
    private Long id;
    private String profilePictureURL;
    private List<String> visitedCountries;
    private List<String> wishlistCountries;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getProfilePictureURL() { return profilePictureURL; }
    public void setProfilePictureURL(String profilePictureURL) { this.profilePictureURL = profilePictureURL;}

    public List<String> getVisitedCountries() { return visitedCountries;}
    public void setVisitedCountries(List<String> visitedCountries) { this.visitedCountries = visitedCountries;}

    public List<String> getWishlistCountries() { return wishlistCountries; }
    public void setWishlistCountries(List<String> wishlistCountries) { this.wishlistCountries = wishlistCountries; }

}

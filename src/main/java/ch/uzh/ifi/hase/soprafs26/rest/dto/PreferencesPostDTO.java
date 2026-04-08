package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class PreferencesPostDTO {
    private String profilePictureURL;
    private List<String> visitedCountries;
    private List<String> wishlistCountries;

    // Getters and Setters
    public String getProfilePictureURL() { return profilePictureURL; }
    public void setProfilePictureURL(String profilePictureURL) { this.profilePictureURL = profilePictureURL;}

    public List<String> getVisitedCountries() { return visitedCountries;}
    public void setVisitedCountries(List<String> visitedCountries) { this.visitedCountries = visitedCountries;}

    public List<String> getWishlistCountries() { return wishlistCountries; }
    public void setWishlistCountries(List<String> wishlistCountries) { this.wishlistCountries = wishlistCountries; }

}

package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class SavedCountryDTO {
    private String countryName;
    private String status; // "visited" | "wishlist"

    public SavedCountryDTO(String countryName, String status) {
        this.countryName = countryName;
        this.status = status;
    }

    public String getCountryName() { 
        return countryName; 
    }
    public String getStatus() { 
        return status; 
    }

}

package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.Set;

public class SavedPlacePostDTO {

    private String externalPlaceId;
    private String name; 
    private String address;
    private Double rating;
    private String photoReference;
    private Double lat;
    private Double lng;
    private Set<String> types;

    public String getExternalPlaceId() {
        return externalPlaceId;
    }

    public void setExternalPlaceId(String externalPlaceId) {
        this.externalPlaceId = externalPlaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

      public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getPhotoReference() {
        return photoReference;
    }
    
    public void setPhotoReference(String photoReference) {
        this.photoReference = photoReference;
    }

    public Double getLat() {
        return lat;
    }
    
    public void setLat(Double lat) {
        this.lat = lat;
    }

     public Double getLng() {
        return lng;
    }
    
    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Set<String> getTypes() {
    return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    
}

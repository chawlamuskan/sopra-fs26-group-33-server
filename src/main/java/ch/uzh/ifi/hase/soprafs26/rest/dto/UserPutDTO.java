package ch.uzh.ifi.hase.soprafs26.rest.dto;
import java.util.List;

public class UserPutDTO {
    private String password;
    private String bio;
    private List<String> countries_visited;
    private List<String> countries_wishlist;

    public String getPassword() {
        return password;
    }   

    public void setPassword(String password) {
        this.password = password;
    }   

    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getCountries_visited() {
        return countries_visited;
    }

    public void setCountries_visited(List<String> countries_visited) {
        this.countries_visited = countries_visited;
    }

    public List<String> getCountries_wishlist() {
        return countries_wishlist;
    }

    public void setCountries_wishlist(List<String> countries_wishlist) {
        this.countries_wishlist = countries_wishlist;
    }
}

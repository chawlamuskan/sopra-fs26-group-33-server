package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(
    name = "travelBoardPlaces",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"externalPlaceId", "user_id"}), // we want to know which user has added the place
        @UniqueConstraint(columnNames = {"externalPlaceId", "board_id"}) // a place can only be saved once per board
    }
)

public class TravelBoardPlace implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue
	private Long id;

    @Column(nullable = false)
    private String externalPlaceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = true)
    private Double rating;

    @Column(nullable = true)
    private String photoReference;

    @Column(nullable = true)
    private Double lat;

    @Column(nullable = true)
    private Double lng;

    @ElementCollection
    @CollectionTable(name = "place_types", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "type")
    private Set<String> types;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private TravelBoard board;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TravelBoard getBoard() {
        return board;
    }

    public void setBoard(TravelBoard board) {
        this.board = board;
    }
    
}

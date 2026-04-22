package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.io.Serializable;


@Entity
@Table(name = "savedPlaces") 

public class SavedPlace implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue
	private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = true)
    private String category;

    // @Column(nullable = false) // not sure about this yet, since place is also containing name and lat/lon but we will need it for graying out
    // private String place; 

    @Column(nullable = true)
    private User user;

    @Column(nullable = true)
    private TravelBoard board;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public User getuser() {
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

package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;

import java.io.Serializable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List; 			

@Entity
@Table(name = "travelBoards")
public class TravelBoard implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = true)
	private String location;

	@Column(nullable = true)
	private LocalDate startDate;

	@Column(nullable = true)
	private LocalDate endDate;

    @ManyToOne 
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = true, unique = true)
	private String inviteCode;		

	@Column(nullable = false)
	private PrivacyLevel privacy; 			

	@Column(nullable = false)
	private LocalDate dateCreated;
    
    @ManyToMany
	private List<User> members = new ArrayList<>();
    
    @ManyToMany
	private List<Place> places = new ArrayList<>();	



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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getInviteCode() {
    return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public PrivacyLevel getPrivacy() {
		return privacy;
	}

	public void setPrivacy(PrivacyLevel privacy) {
		this.privacy = privacy;
	}

	public LocalDate getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(LocalDate dateCreated) {
		this.dateCreated = dateCreated;
	}

    public List<User> getMembers() {
		return members;
	}

	public void setMembers(List<User> members) {
		this.members = members;
	}

	public List<Place> getPlaces() {
		return places;
	}

	public void setPlaces(List<Place> places) {
		this.places = places;
	}
}

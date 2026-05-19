package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;

// this is for: the clients sends the data transfer obj when reigistering new user

public class TravelBoardPostDTO {

	private String name;

	private String location;

	private LocalDate startDate;

	private LocalDate endDate;

	private String inviteCode;

	private PrivacyLevel privacy; 	
	
	private Double latMin;
	private Double latMax;
	private Double lngMin;
	private Double lngMax;

	private String countryCode;

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

	public Double getLatMin() { return latMin; }
	public void setLatMin(Double latMin) { this.latMin = latMin; }

	public Double getLatMax() { return latMax; }
	public void setLatMax(Double latMax) { this.latMax = latMax; }

	public Double getLngMin() { return lngMin; }
	public void setLngMin(Double lngMin) { this.lngMin = lngMin; }

	public Double getLngMax() { return lngMax; }
	public void setLngMax(Double lngMax) { this.lngMax = lngMax; }

	public String getCountryCode() {
	    return countryCode;
	}
	
	public void setCountryCode(String countryCode) {
	    this.countryCode = countryCode;
	}
}

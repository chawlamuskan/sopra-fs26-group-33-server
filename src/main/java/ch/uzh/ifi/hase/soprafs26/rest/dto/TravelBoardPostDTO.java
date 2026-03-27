package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;

// this is for: the clients sends the data transfer obj when reigistering new user

public class TravelBoardPostDTO {

	private String name;

	private LocalDate startDate;

	private LocalDate endDate;

	private PrivacyLevel privacy; 			


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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


	public PrivacyLevel getPrivacy() {
		return privacy;
	}

	public void setPrivacy(PrivacyLevel privacy) {
		this.privacy = privacy;
	}

}

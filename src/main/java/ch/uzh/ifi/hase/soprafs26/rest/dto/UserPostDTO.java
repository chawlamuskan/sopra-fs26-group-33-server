package ch.uzh.ifi.hase.soprafs26.rest.dto;

// this is for: the clients sends the data transfer obj when reigistering new user

public class UserPostDTO {

	private String name;

	private String username;

	private String password;		// add password 

	private String bio;				// add bio -- no creation date here cause done automatically by server

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	// add getters and setters for password and bio 

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
}

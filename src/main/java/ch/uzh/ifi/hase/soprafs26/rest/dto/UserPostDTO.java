package ch.uzh.ifi.hase.soprafs26.rest.dto;

// this is for: the clients sends the data transfer obj when reigistering new user

public class UserPostDTO {

	private String name;
	private String username;
	private String email;
	private String password;		
	private String bio;
	// No creationDate, done automatically by server

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

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
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

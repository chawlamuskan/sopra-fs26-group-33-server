package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserLoginDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;


/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */

@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	// GET /users - Get all users
	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers(
		@RequestHeader(value = "Authorization", required = false) String token) {
		
		userService.validateToken(token); // validate token for authorization
		
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	// Register new user
	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED) // POST /users 201 CREATED based on REST specifications
	@ResponseBody
	public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
	}

	// POST /login - Login user
	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserGetDTO loginUser(@RequestBody UserLoginDTO loginDTO) {
		User user = userService.loginUser(
			loginDTO.getUsername(),
			loginDTO.getEmail(),
			loginDTO.getPassword()
		);

		// do not return raw user, that would expose sensitive data (like password)
		// use mapper to strip away sensitive data
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user); 
	}

	// Get specific user
	@GetMapping("/users/{id}")
	@ResponseStatus(HttpStatus.OK) // GET /users/{id} -> status code 200 (HttpStatus.OK)
	@ResponseBody
	public UserGetDTO getUser(@PathVariable Long id,
		@RequestHeader(value = "Authorization", required = false) String token) {
		userService.validateToken(token);
		
		User user = userService.getUserById(id);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
	}

	// POST /users/logout - Logout user
	@PostMapping("/users/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT) // POST /users/logout -> status code 204 (HttpStatus.NO_CONTENT)
	public void logoutUser(
			@RequestHeader(value = "Authorization", required = false) String token) {
		userService.validateToken(token);
		userService.logoutByToken(token);
	}

	// Update user information (user will be logged out after password change)
	@PutMapping("/users/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // PUT /users/{id} -> status code 204 (HttpStatus.NO_CONTENT)
	@ResponseBody
	public void updateUser(
		@PathVariable Long id,
		@RequestBody UserPutDTO userPutDTO) {

		// Fetch the user by id
		User user = userService.getUserById(id);
		
		// If the user is not found, throw a 404
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		userService.updatePassword(id, userPutDTO.getPassword());
	}

	// DELETE /users/{id} - Delete user account
	@DeleteMapping("/users/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT) // DELETE /users/{id} -> status code 204 (HttpStatus.NO_CONTENT)
	public void deleteUser(
		@PathVariable Long id,
		@RequestHeader(value = "Authorization", required = false) String token) {
		
		userService.validateToken(token);
		userService.deleteUser(id);
	}

}

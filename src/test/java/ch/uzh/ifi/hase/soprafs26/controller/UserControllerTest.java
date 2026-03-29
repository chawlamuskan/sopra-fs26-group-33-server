package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	@Test
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// given
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setEmail("test@example.com");
		user.setStatus(UserStatus.OFFLINE);

		List<User> allUsers = Collections.singletonList(user);

		// this mocks the UserService -> we define above what the userService should
		// return when getUsers() is called
		given(userService.getUsers()).willReturn(allUsers);
		
		Mockito.doNothing().when(userService).validateToken(Mockito.any());


		// when
		MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

		// then
		mockMvc.perform(getRequest).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is(user.getName())))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].email", is(user.getEmail())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// given
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setEmail("test@example.com");
		user.setToken("1");
		user.setStatus(UserStatus.ONLINE);

		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("Test User");
		userPostDTO.setUsername("testUsername");
		userPostDTO.setEmail("test@example.com");
		userPostDTO.setPassword("Test1234!");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// when/then -> do the request + validate the result
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPostDTO));

		// then
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.email", is(user.getEmail())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())));
	}

	@Test
	public void createUser_duplicateUsername_conflict() throws Exception {
		
		UserPostDTO duplicateUserDTO = new UserPostDTO();
		duplicateUserDTO.setName("Another User");
		duplicateUserDTO.setUsername("duplicateUsername"); // duplicate username
		duplicateUserDTO.setEmail("email@example.com");
		duplicateUserDTO.setPassword("Test1234!");

		given(userService.createUser(Mockito.any()))
			.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON) 
				.content(asJsonString(duplicateUserDTO)); 

		mockMvc.perform(postRequest)
				.andExpect(status().isConflict()); // HTTP 409
	}

	@Test
	public void createUser_duplicateEmail_conflict() throws Exception {
		
		UserPostDTO duplicateUserDTO = new UserPostDTO();
		duplicateUserDTO.setName("Another User");
		duplicateUserDTO.setUsername("anotherUsername"); 
		duplicateUserDTO.setEmail("duplicate@example.com"); // duplicate email
		duplicateUserDTO.setPassword("Test1234!");

		given(userService.createUser(Mockito.any()))
			.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists"));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON) 
				.content(asJsonString(duplicateUserDTO)); 

		mockMvc.perform(postRequest)
				.andExpect(status().isConflict()); // HTTP 409
	}

	@Test
	public void createUser_invalidPassword_badRequest() throws Exception {
		
		UserPostDTO invalidPasswordDTO = new UserPostDTO();
		invalidPasswordDTO.setName("Test User");
		invalidPasswordDTO.setUsername("testUsername"); 
		invalidPasswordDTO.setEmail("test@example.com");
		invalidPasswordDTO.setPassword("weak");

		given(userService.createUser(Mockito.any()))
			.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password format."));

		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON) 
				.content(asJsonString(invalidPasswordDTO)); 

		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest()); // HTTP 400
	}

	@Test
	public void getUser_validId_returnsUser() throws Exception {
		// Step 1: Create a dummy user to be returned by the service
		User user = new User();
		user.setId(1L);
		user.setName("John Doe");
		user.setUsername("john_doe");
		user.setEmail("john.doe@example.com");
		user.setToken("token123");
		user.setStatus(UserStatus.ONLINE);
		user.setBio("Hello, I'm John!");
		user.setCreationDate(java.time.LocalDate.of(2026, 3, 5)); // example creation date

		// Step 2: Mock the userService to return this user when getUserById() is called
		given(userService.getUserById(user.getId())).willReturn(user);
		//given(userService.getUserByToken(user.getToken())).willReturn(user); // mock token validation for authorization

		Mockito.doNothing().when(userService).validateToken(Mockito.any());

		// Step 3: Perform GET request to /users/{id}
		MockHttpServletRequestBuilder getRequest = get("/users/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON);
				//.header("Authorization", user.getToken()); // include token in header for authorization

		// Step 4: Validate the response
		mockMvc.perform(getRequest)
				.andExpect(status().isOk()) // HTTP 200
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.email", is(user.getEmail())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.bio", is(user.getBio())))
				.andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())));
	}

	@Test
	public void getUser_invalidId_returnsNotFound() throws Exception {
		// Step 1: Use an ID that does not exist
		Long nonExistentId = 999L;

		// Step 2: Mock the userService to throw ResponseStatusException with 404
		given(userService.getUserById(nonExistentId))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		        
		Mockito.doNothing().when(userService).validateToken(Mockito.any());

		// Step 3: Perform GET request to /users/{id}
		MockHttpServletRequestBuilder getRequest = get("/users/{id}", nonExistentId)
				.contentType(MediaType.APPLICATION_JSON);

		// Step 4: Validate that the response status is 404 NOT FOUND
		mockMvc.perform(getRequest)
				.andExpect(status().isNotFound());
	}

	@Test
	public void updateUser_validInput_noContent() throws Exception {
		// Create a dummy user to be updated
		User user = new User();
		user.setId(1L);
		user.setName("Test User");
		user.setUsername("testUsername");
		user.setEmail("test@example.com");
		user.setToken("token123");
		user.setStatus(UserStatus.ONLINE);

		UserPutDTO userPutDTO = new UserPutDTO();
		userPutDTO.setPassword("Test1234!");

		given(userService.getUserById(user.getId())).willReturn(user);
		Mockito.doNothing().when(userService).updatePassword(Mockito.eq(user.getId()), Mockito.any());

		// Create PUT request
		MockHttpServletRequestBuilder putRequest = put("/users/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPutDTO));

		// Perform request and expect 204 No Content
		mockMvc.perform(putRequest)
			.andExpect(status().isNoContent());
	}

	@Test
	public void updateUser_userNotFound_notFound() throws Exception {
		// Given a non-existent user ID
		Long nonExistentUserId = 999L;

		// Mock the service to return null (user not found)
		given(userService.getUserById(nonExistentUserId)).willReturn(null);

		// Create the UserPutDTO with new password
		UserPutDTO userPutDTO = new UserPutDTO();
		userPutDTO.setPassword("newSecurePassword");

		// Perform PUT request with non-existent user ID
		MockHttpServletRequestBuilder putRequest = put("/users/{id}", nonExistentUserId)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(userPutDTO));

		// Expect 404 Not Found since the user doesn't exist
		mockMvc.perform(putRequest)
				.andExpect(status().isNotFound()); // 404 Not Found
	}

	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}
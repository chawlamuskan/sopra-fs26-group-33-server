package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;


import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserLoginDTO;
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
 * - simulates HTTP requests i.e. GET/POST (no actual network calls) to test the UserController
 * - mocks the UserService (no actual service or database operations are performed)
 * - tests that the controller correctly handles HTTP requests and returns the right responses (status codes and response bodies)
 * 
 * Detects:
 * - wrong HTTP status codes returned by the controller
 * - wrong response body returned by the controller
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserService userService;

	private User createValidUser() {
		User user = new User();
		user.setId(1L);
		user.setName("testName");
		user.setUsername("testUsername");
		user.setEmail("test@example.com");
		user.setStatus(UserStatus.ONLINE);
		user.setToken("valid-token");
		user.setCreationDate(java.time.LocalDate.now());
		return user;
	}

	// ================ GET /users TESTS ================
	@Test	// test that the list of users is fetched correctly
	public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
		// GIVEN a list of users 
		User user = createValidUser();
		List<User> allUsers = Collections.singletonList(user);
		given(userService.validateToken(Mockito.any())).willReturn(user);
		given(userService.getUsers()).willReturn(allUsers);

		// WHEN performing GET request to /users 
		MockHttpServletRequestBuilder getRequest = get("/users")
			.contentType(MediaType.APPLICATION_JSON);
		// THEN return 200 OK with the list of users in the response body
		mockMvc.perform(getRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is(user.getName())))
				.andExpect(jsonPath("$[0].username", is(user.getUsername())))
				.andExpect(jsonPath("$[0].email", is(user.getEmail())))
				.andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
	}

	@Test 	// test that restricted pages are inaccessible without token
	public void getUsers_noToken_returnsUnauthorized() throws Exception {
		// GIVEN no token provided in the request header
		Mockito.when(userService.validateToken(Mockito.isNull()))
				.thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided"));

		// WHEN performing GET request to /users without Authorization header
		MockHttpServletRequestBuilder getRequest = get("/users")
			.contentType(MediaType.APPLICATION_JSON);

		// THEN return 401 UNAUTHORIZED
		mockMvc.perform(getRequest)
			.andExpect(status().isUnauthorized());
	}

	@Test 	// test that restricted pages are inaccessible with invalid token
	public void getUsers_invalidToken_returnsUnauthorized() throws Exception {
		// GIVEN invalid token provided in the request header
		Mockito.when(userService.validateToken(Mockito.anyString()))
				.thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
		
		// WHEN performing GET request to /users with invalid token in Authorization header
		MockHttpServletRequestBuilder getRequest = get("/users")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "invalid-token");

		// THEN return 401 UNAUTHORIZED
		mockMvc.perform(getRequest)
			.andExpect(status().isUnauthorized());
	}

	// ================ POST /users TESTS ================
	@Test
	public void createUser_validInput_userCreated() throws Exception {
		// GIVEN a valid user
		User user = createValidUser();

		UserPostDTO dto = new UserPostDTO();
		dto.setName("Test User");
		dto.setUsername("testUsername");
		dto.setEmail("test@example.com");
		dto.setPassword("Test1234!");

		given(userService.createUser(Mockito.any())).willReturn(user);

		// WHEN POST request is made to /users with the user details in the request body
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// THEN return 201 CREATED with the created user in the response body
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
		// GIVEN a user with a username that already exists in the database
		UserPostDTO dto = new UserPostDTO();
		dto.setName("Another User");
		dto.setUsername("duplicateUsername"); // duplicate username
		dto.setEmail("email@example.com");
		dto.setPassword("Test1234!");

		given(userService.createUser(Mockito.any()))
			.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists"));

		// WHEN POST request is made to /users with the duplicate username in the request body
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON) 
				.content(asJsonString(dto)); 

		// THEN return 409 CONFLICT
		mockMvc.perform(postRequest)
				.andExpect(status().isConflict()); // HTTP 409
	}

	@Test
	public void createUser_duplicateEmail_conflict() throws Exception {
		// GIVEN a user with an email that already exists in the database
		UserPostDTO dto = new UserPostDTO();
		dto.setName("Another User");
		dto.setUsername("anotherUsername"); 
		dto.setEmail("duplicate@example.com"); // duplicate email
		dto.setPassword("Test1234!");

		given(userService.createUser(Mockito.any()))
			.willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists"));
		
		// WHEN POST request is made to /users with the duplicate email in the request body
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON) 
				.content(asJsonString(dto)); 
		
		// THEN return 409 CONFLICT
		mockMvc.perform(postRequest)
				.andExpect(status().isConflict()); // HTTP 409
	}

	@Test
	public void createUser_invalidPassword_badRequest() throws Exception {
		// GIVEN a user with a password that does not meet the required format
		UserPostDTO dto = new UserPostDTO();
		dto.setName("Test User");
		dto.setUsername("testUsername"); 
		dto.setEmail("test@example.com");
		dto.setPassword("weak");

		given(userService.createUser(Mockito.any()))
			.willThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password format."));

		// WHEN POST request is made to /users with the invalid password in the request body
		MockHttpServletRequestBuilder postRequest = post("/users")
				.contentType(MediaType.APPLICATION_JSON) 
				.content(asJsonString(dto)); 
		
		// THEN return 400 BAD REQUEST
		mockMvc.perform(postRequest)
				.andExpect(status().isBadRequest()); // HTTP 400
	}


	// ================ POST /login TESTS ================
	@Test
	public void loginUser_validCredentials_returnsOk() throws Exception {
		// GIVEN valid login credentials
		User user = createValidUser();
		
		UserLoginDTO dto = new UserLoginDTO();
		dto.setUsername("testUsername");
		dto.setPassword("Test1234!");

		given(userService.loginUser(Mockito.any(), Mockito.any(), Mockito.any()))
			.willReturn(user);
		
		// WHEN POST request is made to /login with the valid credentials in the request body
		MockHttpServletRequestBuilder postRequest = post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// THEN return 200 OK with the logged-in user in the response body
		mockMvc.perform(postRequest)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.email", is(user.getEmail())))
				.andExpect(jsonPath("$.password").doesNotExist()) // password should not be returned in the response
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.token", is(user.getToken())));
	}

	@Test
	public void loginUser_invalidCredentials_returnsUnauthorized() throws Exception {
		// GIVEN invalid login credentials
		UserLoginDTO dto = new UserLoginDTO();
		dto.setUsername("testUsername"); 
		dto.setPassword("WrongPass1!");

		given(userService.loginUser(Mockito.any(), Mockito.any(), Mockito.any()))
			.willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		
		// WHEN POST request is made to /login with the invalid credentials in the request body
		MockHttpServletRequestBuilder postRequest = post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// THEN return 401 UNAUTHORIZED
		mockMvc.perform(postRequest)
				.andExpect(status().isUnauthorized());
	}
	
	// ================ POST /users/logout TESTS ================
	@Test	// test that a user can log out successfully with a valid token
	public void logoutUser_validToken_returnsNoContent() throws Exception {
		// GIVEN a valid token for an online user
		User user = createValidUser();
		given(userService.validateToken("valid-token"))
			.willReturn(user);
		Mockito.doNothing().when(userService).logoutByToken(Mockito.eq("valid-token"));
		
		// WHEN POST /users/logout is called with the valid token in the Authorization header
		MockHttpServletRequestBuilder postRequest = post("/users/logout")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "valid-token");
		
		// THEN return 204 NO CONTENT
		mockMvc.perform(postRequest)
			.andExpect(status().isNoContent());
	}
	
	@Test	// test that a user can log out successfully with a invalid token
	public void logoutUser_invalidToken_returnsUnauthorized() throws Exception {
		// GIVEN an invalid token
		Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"))
				.when(userService).validateToken(Mockito.eq("invalid-token"));
		
		// WHEN POST /users/logout is called with the invalid token
		MockHttpServletRequestBuilder postRequest = post("/users/logout")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "invalid-token");
		
		// THEN return 401 UNAUTHORIZED
		mockMvc.perform(postRequest)
			.andExpect(status().isUnauthorized());
	}
	
	@Test	// test that restricted pages cannot be accessed after logged out (i.e. token is invalid after logout)
	public void logoutUser_AccessRestrictedPages_returnsUnauthorized() throws Exception {
		// GIVEN user has logged out and token is now invalid
		Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"))
				.when(userService).validateToken(Mockito.eq("old-token"));
		
		// WHEN trying to access a restricted page with the old token after logout
		MockHttpServletRequestBuilder getRequest = get("/users")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "old-token");

		// THEN return 401 UNAUTHORIZED
		mockMvc.perform(getRequest)
			.andExpect(status().isUnauthorized());
	}


	// ================ GET /users/{id} TESTS ================
	@Test
	public void getUser_validId_returnsUser() throws Exception {
		// GIVEN a valid user ID
		User user = createValidUser();

		given(userService.validateToken(Mockito.any())).willReturn(user);
		given(userService.getUserById(user.getId())).willReturn(user);

		// WHEN performing GET request to /users/{id} with the valid user ID
		MockHttpServletRequestBuilder getRequest = get("/users/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON);
		
		// THEN return 200 OK with the user details in the response body
		mockMvc.perform(getRequest)
				.andExpect(status().isOk()) // HTTP 200
				.andExpect(jsonPath("$.id", is(user.getId().intValue())))
				.andExpect(jsonPath("$.name", is(user.getName())))
				.andExpect(jsonPath("$.username", is(user.getUsername())))
				.andExpect(jsonPath("$.email", is(user.getEmail())))
				.andExpect(jsonPath("$.status", is(user.getStatus().toString())))
				.andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())));
	}

	@Test
	public void getUser_invalidId_returnsNotFound() throws Exception {
		// GIVEN 
		User user = createValidUser();

		given(userService.validateToken(Mockito.any())).willReturn(user);
		given(userService.getUserById(999L))
				.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		        
		// WHEN performing GET request to /users/{id} with a non-existent user ID
		MockHttpServletRequestBuilder getRequest = get("/users/{id}", 999L)
				.contentType(MediaType.APPLICATION_JSON);

		// THEN validate that the response status is 404 NOT FOUND
		mockMvc.perform(getRequest)
				.andExpect(status().isNotFound());
	}

	// ================ PUT /users/{id} TESTS ================
	@Test
	public void updateUser_validInput_noContent() throws Exception {
		// GIVEN a valid user ID and new password
		User user = createValidUser();

		UserPutDTO dto = new UserPutDTO();
		dto.setPassword("Test1234!");

		given(userService.getUserById(user.getId())).willReturn(user);
		Mockito.doNothing().when(userService).updatePassword(user.getId(), dto.getPassword());

		// WHEN performing PUT request to /users/{id} with the new password in the request body
		MockHttpServletRequestBuilder putRequest = put("/users/{id}", user.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// THEN return 204 NO CONTENT
		mockMvc.perform(putRequest)
			.andExpect(status().isNoContent());
	}

	@Test
	public void updateUser_userNotFound_notFound() throws Exception {
		// GIVEN a valid user ID and new password
		UserPutDTO dto = new UserPutDTO();
		dto.setPassword("newSecurePassword");
		
		given(userService.getUserById(999L)).willReturn(null);

		// WHEN performing PUT request to /users/{id} with a non-existent user ID
		MockHttpServletRequestBuilder putRequest = put("/users/{id}", 999L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto));

		// THEN return 404 NOT FOUND
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
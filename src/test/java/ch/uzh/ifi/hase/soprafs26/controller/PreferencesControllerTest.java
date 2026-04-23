package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PreferencesPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.PreferencesService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

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

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * PreferencesControllerTest
 * - simulates HTTP requests i.e. GET/POST (no actual network calls) to test the PreferencesController
 * - mocks the PreferencesService (no actual service or database operations are performed)
 * - tests that the controller correctly handles HTTP requests and returns the right responses (status codes and response bodies)
 * 
 * Detects:
 * - wrong HTTP status codes returned by the controller
 * - wrong response body returned by the controller
 */
@WebMvcTest(PreferencesController.class)
public class PreferencesControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PreferencesService preferencesService;

    @MockitoBean
	private UserService userService;

    // helper method to create a logged-in, mock user
	private User mockUser(Long id) {
		User user = new User();
		user.setId(id);
        user.setUsername("testUsername");
		user.setToken("valid-token");
		return user;
	}

    // ================ POST /users/{userId}/preferences TESTS ================
	@Test   // test that valid preferences data is saved successfully
	public void savePreferences_validInput_returnsCreated() throws Exception {
		// GIVEN a logged-in user and valid preferences data
		User user = mockUser(1L);

        PreferencesPostDTO dto = new PreferencesPostDTO();
        dto.setBio("Test bio");
        dto.setProfilePicture("http://example.com/profile.jpg");
        dto.setVisitedCountries(List.of("Switzerland", "Germany"));
        dto.setWishlistCountries(List.of("Italy", "Spain"));
        dto.setFriends(List.of(2L, 3L));

        Preferences saved = new Preferences();
        saved.setBio("Test bio");
        saved.setProfilePicture("http://example.com/profile.jpg");
        saved.setVisitedCountries(List.of("Switzerland", "Germany"));
        saved.setWishlistCountries(List.of("Italy", "Spain"));
        saved.setFriends(List.of(2L, 3L));

        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(user);
		given(preferencesService.savePreferences(Mockito.eq(1L), Mockito.any()))
            .willReturn(saved);    
		
		// WHEN POST request is made to /users/{userId}/preferences with valid data
		MockHttpServletRequestBuilder postRequest = post("/users/1/preferences")
				.contentType(MediaType.APPLICATION_JSON) 
                .header("Authorization", "valid-token")
				.content(asJsonString(dto)); 

		// THEN return 201 CREATED with the saved preferences in the response body
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated()) // HTTP 201
                .andExpect(jsonPath("$.bio", is("Test bio")))
                .andExpect(jsonPath("$.visitedCountries[0]", is("Switzerland")))
                .andExpect(jsonPath("$.visitedCountries[1]", is("Germany")))
                .andExpect(jsonPath("$.wishlistCountries[0]", is("Italy")))
                .andExpect(jsonPath("$.wishlistCountries[1]", is("Spain")))
                .andExpect(jsonPath("$.friends[0]", is(2)))
                .andExpect(jsonPath("$.friends[1]", is(3)));
	}

    @Test   // test that partial preferences data is saved correctly
	public void savePreferences_partialInput_returnsCreated() throws Exception {
		// GIVEN a logged-in user and only visited countries provided in the preferences data
		User user = mockUser(1L);

        PreferencesPostDTO dto = new PreferencesPostDTO();
        dto.setVisitedCountries(List.of("Switzerland"));

        Preferences saved = new Preferences();
        saved.setVisitedCountries(List.of("Switzerland"));

        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(user);
		given(preferencesService.savePreferences(Mockito.eq(1L), Mockito.any()))
            .willReturn(saved);    
		
		// WHEN POST request is made to /users/{userId}/preferences with partial data
		MockHttpServletRequestBuilder postRequest = post("/users/1/preferences")
				.contentType(MediaType.APPLICATION_JSON) 
                .header("Authorization", "valid-token")
				.content(asJsonString(dto)); 

		// THEN return 201 CREATED with only the partial preferences in the response body
		mockMvc.perform(postRequest)
				.andExpect(status().isCreated()) // HTTP 201
                .andExpect(jsonPath("$.visitedCountries[0]", is("Switzerland")));
	}

    @Test   // test that users cannot set preferences for other users (forbidden)
	public void savePreferences_differentUserId_returnsForbidden() throws Exception {
		// GIVEN logged-in user with ID 1 trying to set preferences for user with ID 2
		User user = mockUser(1L);

        PreferencesPostDTO dto = new PreferencesPostDTO();
        dto.setBio("Test bio");

        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(user);  
		
		// WHEN POST request is made to /users/2/preferences from user with ID 1
		MockHttpServletRequestBuilder postRequest = post("/users/2/preferences")
				.contentType(MediaType.APPLICATION_JSON) 
                .header("Authorization", "valid-token")
				.content(asJsonString(dto)); 

		// THEN return 403 FORBIDDEN when trying to set preferences for another user
		mockMvc.perform(postRequest)
				.andExpect(status().isForbidden());
	}

    @Test   // test that saving preferences without a token throws error
	public void savePreferences_noToken_returnsUnauthorized() throws Exception {
		// GIVEN no token is provided in the request header
        PreferencesPostDTO dto = new PreferencesPostDTO();
        dto.setBio("Test bio");

        given(userService.validateToken(Mockito.isNull()))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided"));
		User user = mockUser(1L);

		// WHEN POST request is made to /users/{userId}/preferences from user with ID 1
		MockHttpServletRequestBuilder postRequest = post("/users/1/preferences")
				.contentType(MediaType.APPLICATION_JSON)
				.content(asJsonString(dto)); 

		// THEN return 401 UNAUTHORIZED when no token is provided
		mockMvc.perform(postRequest)
				.andExpect(status().isUnauthorized());
	}

    // ================ GET /users/{userId}/preferences TESTS ================

    // ================ PUT /users/{userId}/preferences TESTS ================


    // ================ helper methods ================
    private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}
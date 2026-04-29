package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.service.SavedPlaceService;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasItems;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

/**
 * PreferencesControllerTest
 * - simulates HTTP requests i.e. GET/POST/DELETE (no actual network calls) to test the SavedPlaceController
 * - mocks the SavedPlaceService (no actual service or database operations are performed)
 * - tests that the controller correctly handles HTTP requests and returns the right responses (status codes and response bodies)
 * 
 * Detects:
 * - wrong HTTP status codes returned by the controller
 * - wrong response body returned by the controller
 */



@WebMvcTest(SavedPlaceController.class)
public class SavedPlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavedPlaceService savedPlaceService;

    @MockitoBean
    private UserService userService;

    // Helper method to mock a logged-in user
    private User mockUser(Long id) {
		User user = new User();
		user.setId(id);
        user.setUsername("testUsername");
		user.setToken("valid-token");
		return user;
	}

    // Helper method to mock a saved place
    private SavedPlace mockSavedPlace(String externalPlaceId, String name, String address, Double rating, String photoReference, Double lat, Double lng, Set<String> types) {
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId(externalPlaceId);
        savedPlace.setName(name);
        savedPlace.setAddress(address);
        savedPlace.setRating(rating);
        savedPlace.setPhotoReference(photoReference);
        savedPlace.setLat(lat);
        savedPlace.setLng(lng);
        savedPlace.setTypes(types);
        return savedPlace;
    }


    // ================ POST /users/{userId}/savedplaces TESTS ================

    @Test // test that a place is saved successfully to saved places
    public void createSavedPlace_validInput_returnsCreated() throws Exception{
        User mockUser = mockUser(1L);
        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(mockUser);

        SavedPlacePostDTO dto = new SavedPlacePostDTO();
        dto.setExternalPlaceId("9876");
        dto.setName("Eiffel Tower");
        dto.setAddress("Rue de Eiffel, 3000 Paris");
        dto.setRating(4.3);
        dto.setPhotoReference("abcde");
        dto.setLat(321.321);
        dto.setLng(123.123);
        dto.setTypes(Set.of("Attraction", "Building"));

        SavedPlace created = mockSavedPlace("9876", "Eiffel Tower", "Rue de Eiffel, 3000 Paris", 4.3, "abcde", 321.321, 123.123, Set.of("Attraction", "Building"));

        given(savedPlaceService.saveToUser(Mockito.eq(1L), Mockito.any()))
            .willReturn(created);

        MockHttpServletRequestBuilder postRequest = post("/users/1/savedplaces")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "valid-token")
            .content(asJsonString(dto));

        mockMvc.perform(postRequest)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.externalPlaceId", is("9876")))
            .andExpect(jsonPath("$.name", is("Eiffel Tower")))
            .andExpect(jsonPath("$.address", is("Rue de Eiffel, 3000 Paris")))
            .andExpect(jsonPath("$.rating", is(4.3)))
            .andExpect(jsonPath("$.photoReference", is("abcde")))
            .andExpect(jsonPath("$.lat", is(321.321)))
            .andExpect(jsonPath("$.lng", is(123.123)))
            .andExpect(jsonPath("$.types", hasItems("Attraction", "Building")));
    }

    @Test
    public void createSavedPlace_differentUserId_returnsForbidden() throws Exception {
        User user = mockUser(1L);

        SavedPlacePostDTO dto = new SavedPlacePostDTO();
        dto.setExternalPlaceId("9876");
        dto.setName("Eiffel Tower");
        dto.setAddress("Rue de Eiffel, 3000 Paris");
        dto.setRating(4.3);
        dto.setPhotoReference("abcde");
        dto.setLat(321.321);
        dto.setLng(123.123);
        dto.setTypes(Set.of("Attraction", "Building"));

        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(user);

        // When POST request is made to /users/2/savedplaces from user with Id 1
        MockHttpServletRequestBuilder postRequest = post("/users/2/savedplaces")
            .contentType(MediaType.APPLICATION_JSON) 
            .header("Authorization", "valid-token")
			.content(asJsonString(dto));

        // Then return 403 FORBIDDEN
        mockMvc.perform(postRequest)
            .andExpect(status().isForbidden());
    }

    @Test
    public void createSavedPlace_noToken_returnsUnauthorized() throws Exception {
        SavedPlacePostDTO dto = new SavedPlacePostDTO();
        dto.setExternalPlaceId("9876");
        dto.setName("Eiffel Tower");
        dto.setAddress("Rue de Eiffel, 3000 Paris");
        dto.setRating(4.3);
        dto.setPhotoReference("abcde");
        dto.setLat(321.321);
        dto.setLng(123.123);
        dto.setTypes(Set.of("Attraction", "Building"));

    given(userService.validateToken(Mockito.isNull()))
        .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided"));

    // When POST request is made to /users/{userId}/savedplaces from user with Id 1
    MockHttpServletRequestBuilder postRequest = post("/users/1/savedplaces")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(dto));

    // then return 401 UNAUTHORIZED when no token is provided
    mockMvc.perform(postRequest)
        .andExpect(status().isUnauthorized());
    }


    // ================ GET /users/{userId}/savedplaces TESTS ================
  

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



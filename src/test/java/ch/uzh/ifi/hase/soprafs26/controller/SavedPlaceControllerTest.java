package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.service.SavedPlaceService;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import java.util.ArrayList;
import java.util.List;

/**
 * SavedPlaceControllerTest
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
    private SavedPlace mockSavedPlace(String externalPlaceId, String name, String address, Double rating, String photoReference, Double lat, Double lng, Set<String> types, User user, String city) {
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId(externalPlaceId);
        savedPlace.setName(name);
        savedPlace.setAddress(address);
        savedPlace.setRating(rating);
        savedPlace.setPhotoReference(photoReference);
        savedPlace.setLat(lat);
        savedPlace.setLng(lng);
        savedPlace.setTypes(types);
        savedPlace.setUser(user);
        savedPlace.setCity(city);
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
        dto.setTypes(Set.of("toursit_attraction", "establishment"));
        dto.setCity("Paris");
       

        SavedPlace created = mockSavedPlace("9876", "Eiffel Tower", "Rue de Eiffel, 3000 Paris", 4.3, "abcde", 321.321, 123.123, Set.of("tourist_attraction", "establishment"), mockUser, "Paris");

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
            .andExpect(jsonPath("$.types", hasItems("tourist_attraction", "establishment")))
            .andExpect(jsonPath("$.city", is("Paris")));
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
        dto.setTypes(Set.of("Attraction", "Establishment"));
        dto.setCity("Paris");

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
        dto.setTypes(Set.of("Attraction", "Establishment"));
        dto.setCity("Paris");

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
  
    @Test // Test getting all saved places for a user returns 200
    public void getSavedPlacesByUser_validToken_returnsOk() throws Exception {
        String token = "valid-token";

        User mockUser = mockUser(1L);

        SavedPlace mockPlace = mockSavedPlace("9876", "Eiffel Tower", "Rue de Eiffel, 3000 Paris", 4.3, "abcde", 321.321, 123.123, Set.of("tourist_attraction", "establishment"), mockUser, "Paris");
        List<SavedPlace> savedPlaces = new ArrayList<>();
        savedPlaces.add(mockPlace);
        
        Mockito.when(userService.validateToken(token)).thenReturn(mockUser);
        Mockito.when(savedPlaceService.getSavedPlacesByUser(1L))
            .thenReturn(savedPlaces);


        MockHttpServletRequestBuilder getRequest = get("/users/1/savedplaces")
            .header("Authorization", token)
            .contentType(MediaType.APPLICATION_JSON);
            

        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].externalPlaceId", is("9876")))
            .andExpect(jsonPath("$[0].name", is("Eiffel Tower")))
            .andExpect(jsonPath("$[0].address", is("Rue de Eiffel, 3000 Paris")))
            .andExpect(jsonPath("$[0].rating", is(4.3)))
            .andExpect(jsonPath("$[0].photoReference", is("abcde")))
            .andExpect(jsonPath("$[0].lat", is(321.321)))
            .andExpect(jsonPath("$[0].lng", is(123.123)))
            .andExpect(jsonPath("$[0].types", hasItems("tourist_attraction", "establishment")))
            .andExpect(jsonPath("$[0].city", is("Paris")));

    }

    @Test // Test that you can only get your own saved places
    public void getSavedPlacesByUser_differentUserId_returnsForbidden() throws Exception {
        User mockUser = mockUser(1L);
        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(mockUser);

        MockHttpServletRequestBuilder getRequest = get("/users/2/savedplaces")
            .header("Authorization", "valid-token")
            .contentType(MediaType.APPLICATION_JSON);

        // Then return 403 FORBIDDEN
        mockMvc.perform(getRequest)
            .andExpect(status().isForbidden());

    }


    @Test // Test that if no token is provided, you cannot get saved places
    public void getSavedPlacesByUser_noToken_returnsUnauthorized() throws Exception {
        given(userService.validateToken(Mockito.isNull()))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided"));

        MockHttpServletRequestBuilder getRequest = get("/users/1/savedplaces")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
            .andExpect(status().isUnauthorized());


    }


    // ================ DELETE /users/{userId}/savedplaces/{savedPlaceId} TESTS ================

    @Test // Test that deleting a place is returning no content
    public void deleteSavedPlace_validInput_returnsNoContent() throws Exception {
        Long savedPlaceId = 1L;

        User mockUser = mockUser(1L);
        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(mockUser);
        doNothing().when(savedPlaceService).deleteSavedPlace(1L, "valid-token");

        MockHttpServletRequestBuilder deleteRequest = delete("/users/1/savedplaces/{savedPlaceId}", savedPlaceId)
            .header("Authorization", "valid-token")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
            .andExpect(status().isNoContent());

    }


    @Test // Test that you can only delete your own saved places
    public void deleteSavedPlace_differentUserId_returnsForbidden() throws Exception {

        User mockUser = mockUser(1L);
        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(mockUser);

        MockHttpServletRequestBuilder deleteRequest = delete("/users/2/savedplaces/1")
            .header("Authorization", "valid-token")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
            .andExpect(status().isForbidden());

    }


    @Test // Test that if no token is provided, it is not possible to delete a saved place
    public void deleteSavedPlace_noToken_returnsUnauthorized() throws Exception {
        given(userService.validateToken(Mockito.isNull()))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided"));

        MockHttpServletRequestBuilder deleteRequest = delete("/users/1/savedplaces/1")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
            .andExpect(status().isUnauthorized());

    }

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



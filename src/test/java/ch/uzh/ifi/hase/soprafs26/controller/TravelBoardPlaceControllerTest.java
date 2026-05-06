package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.service.SavedPlaceService;
import ch.uzh.ifi.hase.soprafs26.service.TravelBoardPlaceService;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;

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
 * TravelBoardPlaceControllerTest
 * - simulates HTTP requests i.e. GET/POST/DELETE (no actual network calls) to test the SavedPlaceController
 * - mocks the TravelBoardPlaceService (no actual service or database operations are performed)
 * - tests that the controller correctly handles HTTP requests and returns the right responses (status codes and response bodies)
 * 
 * Detects:
 * - wrong HTTP status codes returned by the controller
 * - wrong response body returned by the controller
 */


@WebMvcTest(TravelBoardPlaceController.class)
public class TravelBoardPlaceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TravelBoardPlaceService travelBoardPlaceService;

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

    // Helper method to mock an existing travel board
    private TravelBoard mockTravelBoard(Long id, User owner) {
        TravelBoard board = new TravelBoard();
        board.setId(id);
        board.setOwner(owner);
        board.setMembers(List.of(owner));
        return board;
    }


    // Helper method to mock a travel board place
    private TravelBoardPlace mockTravelBoardPlace(String externalPlaceId, String name, String address, Double rating, String photoReference, Double lat, Double lng, Set<String> types, User user, TravelBoard board) {
        TravelBoardPlace travelBoardPlace = new TravelBoardPlace();
        travelBoardPlace.setExternalPlaceId(externalPlaceId);
        travelBoardPlace.setName(name);
        travelBoardPlace.setAddress(address);
        travelBoardPlace.setRating(rating);
        travelBoardPlace.setPhotoReference(photoReference);
        travelBoardPlace.setLat(lat);
        travelBoardPlace.setLng(lng);
        travelBoardPlace.setTypes(types);
        travelBoardPlace.setUser(user);
        return travelBoardPlace;
    }

    // ================ POST /travelboard/{boardId}/places TESTS ================

    @Test // test that a place is saved successfully to a travel board
    public void addPlaceToBoard_validInput_returnsCreated() throws Exception {
        User mockUser = mockUser(1L);
        given(userService.validateToken(Mockito.eq("valid-token"))).willReturn(mockUser);

        TravelBoardPlacePostDTO dto = new TravelBoardPlacePostDTO();
        dto.setExternalPlaceId("9876");
        dto.setName("Eiffel Tower");
        dto.setAddress("Rue de Eiffel, 3000 Paris");
        dto.setRating(4.3);
        dto.setPhotoReference("abcde");
        dto.setLat(321.321);
        dto.setLng(123.123);
        dto.setTypes(Set.of("Attraction", "Building"));

        TravelBoardPlace added = mockTravelBoardPlace("9876", "Eiffel Tower", "Rue de Eiffel, 3000 Paris", 4.3, "abcde", 321.321, 123.123, Set.of("Attraction", "Building"), mockUser, mockTravelBoard(1L, mockUser));

        given(travelBoardPlaceService.saveToBoard(Mockito.eq(1L), Mockito.any(), Mockito.any()))
            .willReturn(added);

        MockHttpServletRequestBuilder postRequest = post("/travelboards/1/places")
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
    public void addPlaceToBoard_notMember_returnsForbidden() throws Exception {

        TravelBoardPlacePostDTO dto = new TravelBoardPlacePostDTO();
        dto.setExternalPlaceId("9876");
        dto.setName("Eiffel Tower");
        dto.setAddress("Rue de Eiffel, 3000 Paris");
        dto.setRating(4.3);
        dto.setPhotoReference("abcde");
        dto.setLat(321.321);
        dto.setLng(123.123);
        dto.setTypes(Set.of("Attraction", "Building"));

        given(travelBoardPlaceService.saveToBoard(Mockito.eq(2L), Mockito.any(), Mockito.any()))
            .willThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only board members can add places"));

        // When POST request is made to /travelboards/2/places from user that is not member of the board
        MockHttpServletRequestBuilder postRequest = post("/travelboards/2/places")
            .contentType(MediaType.APPLICATION_JSON) 
            .header("Authorization", "valid-token")
			.content(asJsonString(dto));

        // Then return 403 FORBIDDEN
        mockMvc.perform(postRequest)
            .andExpect(status().isForbidden());
    }

    @Test
    public void addPlaceToBoard_noToken_returnsUnauthorized() throws Exception {
        TravelBoardPlacePostDTO dto = new TravelBoardPlacePostDTO();
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

    // When POST request is made to /travelboards/{boardId}/places from user with Id 1
    MockHttpServletRequestBuilder postRequest = post("/travelboards/1/places")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(dto));

    // then return 401 UNAUTHORIZED when no token is provided
    mockMvc.perform(postRequest)
        .andExpect(status().isUnauthorized());
    }

       @Test // Test that if board does not exist, return 404
    public void addPlaceToBoard_boardNotFound_returnsNotFound() throws Exception {
        given(travelBoardPlaceService.getPlacesByBoard(999L))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));
        
        MockHttpServletRequestBuilder postRequest = get("/travelboards/999/places")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
            .andExpect(status().isNotFound());

    }


    // ================ GET /travelboards/{boardId}/places TESTS ================
  
    @Test // Test getting places for a board returns 200
    public void getPlacesByBoardId_validInput_returnsOk() throws Exception {
        String token = "valid-token";

        User mockUser = mockUser(1L);

        TravelBoardPlace mockPlace = mockTravelBoardPlace("9876", "Eiffel Tower", "Rue de Eiffel, 3000 Paris", 4.3, "abcde", 321.321, 123.123, Set.of("Attraction", "Building"), mockUser, mockTravelBoard(1L, mockUser));
        List<TravelBoardPlace> savedPlaces = new ArrayList<>();
        savedPlaces.add(mockPlace);
        
        Mockito.when(userService.validateToken(token)).thenReturn(mockUser);
        Mockito.when(travelBoardPlaceService.getPlacesByBoard(1L))
            .thenReturn(savedPlaces);


        MockHttpServletRequestBuilder getRequest = get("/travelboards/1/places")
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
            .andExpect(jsonPath("$[0].types", hasItems("Attraction", "Building")));

    }

    @Test // Test that if no token is provided, you cannot get saved places
    public void getPlacesByBoard_noToken_returnsUnauthorized() throws Exception {
        given(userService.validateToken(Mockito.isNull()))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided"));

        MockHttpServletRequestBuilder getRequest = get("/travelboards/1/places")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
            .andExpect(status().isUnauthorized());


    }


    @Test // Test that if board does not exist, return 404
    public void getPlacesByBoard_boardNotFound_returnsNotFound() throws Exception {
        given(travelBoardPlaceService.getPlacesByBoard(999L))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));
        
        MockHttpServletRequestBuilder getRequest = get("/travelboards/999/places")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
            .andExpect(status().isNotFound());

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

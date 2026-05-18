package ch.uzh.ifi.hase.soprafs26.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.repository.JoinRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SavedPlaceRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.List;


@WebAppConfiguration
@SpringBootTest
public class SavedPlaceServiceIntegrationTest {

    @Qualifier("savedPlaceRepository")
    @Autowired
    private SavedPlaceRepository savedPlaceRepository;

    @Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

    @Autowired
    private SavedPlaceService savedPlaceService;

    @Autowired
	private UserService userService;

    @Qualifier("joinRequestRepository")
    @Autowired
    private JoinRequestRepository joinRequestRepository;

    private User testUser;
    private User testUser2;

    @BeforeEach
	public void setup() {
        savedPlaceRepository.deleteAll();
		userRepository.deleteAll();
        joinRequestRepository.deleteAll();

        // GIVEN a registered user in the database
        testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setEmail("test@example.com");
        testUser.setPassword("Test1234!");
        testUser = userService.createUser(testUser);

        testUser2 = new User();
        testUser2.setName("testName2");
        testUser2.setUsername("testUsername2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("Test1234!!");
        testUser2 = userService.createUser(testUser2);
	}



    // ================ SAVE A PLACE TESTS ================
    @Test // Test that a place is being saved successfully
    public void savePlace_validInput_success() {
        // given a registered user and valid input for saving a place
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId("abcd");
        savedPlace.setName("Eiffel Tower");
        savedPlace.setAddress("Rue de Paris, 1000 Paris");
        savedPlace.setRating(4.5);
        savedPlace.setPhotoReference("98765");
        savedPlace.setLat(1234.1234);
        savedPlace.setLng(4321.4321);
        savedPlace.setTypes(Set.of("tourist_attraction", "establishment"));
        savedPlace.setUser(testUser);
        savedPlace.setCity("Paris");

        // when saving a place
        SavedPlace saved = savedPlaceService.saveToUser(testUser.getId(), savedPlace);

        // then the place gets saved correctly to the database
        assertNotNull(saved.getId());
        assertEquals("abcd", saved.getExternalPlaceId());
        assertEquals("Eiffel Tower", saved.getName());
        assertEquals("Rue de Paris, 1000 Paris", saved.getAddress());
        assertEquals(4.5, saved.getRating());
        assertEquals("98765", saved.getPhotoReference());
        assertEquals(1234.1234, saved.getLat());
        assertEquals(4321.4321, saved.getLng());
        assertEquals(Set.of("tourist_attraction", "establishment"), saved.getTypes());
        assertEquals(testUser.getId(), saved.getUser().getId());
        assertEquals("Paris", saved.getCity());
    }


    @Test // Test that if a place is being saved twice, returns CONFLICT
    public void savePlace_alreadySaved_returnConflict() {
        // given a registered user and valid input for saving a place
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId("abcd");
        savedPlace.setName("Eiffel Tower");
        savedPlace.setAddress("Rue de Paris, 1000 Paris");
        savedPlace.setRating(4.5);
        savedPlace.setPhotoReference("98765");
        savedPlace.setLat(1234.1234);
        savedPlace.setLng(4321.4321);
        savedPlace.setTypes(Set.of("tourist_attraction", "establishment"));
        savedPlace.setUser(testUser);
        savedPlace.setCity("Paris");

        savedPlaceService.saveToUser(testUser.getId(), savedPlace);

        // when the same place is saved to the same user again
        // then a conflict error is thrown

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            savedPlaceService.saveToUser(testUser.getId(), savedPlace));

        assertEquals(409, ex.getStatusCode().value());

    }

    @Test // test that saving a place to non existing user throws NOT FOUND
    public void savePlace_userNotFound() {
        // given valid input for saving a place but no user
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId("abcd");
        savedPlace.setName("Eiffel Tower");
        savedPlace.setAddress("Rue de Paris, 1000 Paris");
        savedPlace.setRating(4.5);
        savedPlace.setPhotoReference("98765");
        savedPlace.setLat(1234.1234);
        savedPlace.setLng(4321.4321);
        savedPlace.setTypes(Set.of("tourist_attraction", "establishment"));
        savedPlace.setUser(null);
        savedPlace.setCity("Paris");

    
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            savedPlaceService.saveToUser(999L, savedPlace));

        assertEquals(404, ex.getStatusCode().value());

    }

    // ================ GET SAVED PLACES TESTS ================
    @Test // get all saved places for a user successfully
    public void getAllSavedPlaces_validInput_returnSavedPlaces() {
        // given a user with two saved places
        SavedPlace savedPlace1 = new SavedPlace();
        savedPlace1.setExternalPlaceId("abcd");
        savedPlace1.setName("Eiffel Tower");
        savedPlace1.setAddress("Rue de Paris, 1000 Paris");
        savedPlace1.setRating(4.5);
        savedPlace1.setPhotoReference("98765");
        savedPlace1.setLat(1234.1234);
        savedPlace1.setLng(4321.4321);
        savedPlace1.setTypes(Set.of("tourist_attraction", "establishment"));
        savedPlace1.setUser(testUser);
        savedPlace1.setCity("Paris");

        SavedPlace savedPlace2 = new SavedPlace();
        savedPlace2.setExternalPlaceId("efgh");
        savedPlace2.setName("Akropolis");
        savedPlace2.setAddress("Athens 105, Greece");
        savedPlace2.setRating(4.9);
        savedPlace2.setPhotoReference("123456");
        savedPlace2.setLat(1111.1111);
        savedPlace2.setLng(5555.5555);
        savedPlace2.setTypes(Set.of("tourist_attraction", "museum"));
        savedPlace2.setUser(testUser);
        savedPlace2.setCity("Athens");

        savedPlaceService.saveToUser(testUser.getId(), savedPlace1);
        savedPlaceService.saveToUser(testUser.getId(), savedPlace2);


        // when fetching all saved places for the user
        List<SavedPlace> result = savedPlaceService.getSavedPlacesByUser(testUser.getId());

        // then both places are returned
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getExternalPlaceId().equals("abcd")));
        assertTrue(result.stream().anyMatch(p -> p.getExternalPlaceId().equals("efgh")));
    }

    @Test // test that getting places for non existing user throws NOT FOUND
    public void getPlaces_userNotFound() {
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            savedPlaceService.getSavedPlacesByUser(999L));

        assertEquals(404, ex.getStatusCode().value());
        
    }

    // ================ DELETE SAVED PLACES TESTS ================
    @Test // test that deleting a place is successful
    public void deletePlace_success() {
        // given a registered user and a saved place
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId("abcd");
        savedPlace.setName("Eiffel Tower");
        savedPlace.setAddress("Rue de Paris, 1000 Paris");
        savedPlace.setRating(4.5);
        savedPlace.setPhotoReference("98765");
        savedPlace.setLat(1234.1234);
        savedPlace.setLng(4321.4321);
        savedPlace.setTypes(Set.of("tourist_attraction", "establishment"));
        savedPlace.setUser(testUser);
        savedPlace.setCity("Paris");

        savedPlace = savedPlaceService.saveToUser(testUser.getId(), savedPlace);

        // when the place is deleted
        savedPlaceService.deleteSavedPlace(savedPlace.getId(), testUser.getToken());

        // then the place no longer exists 
        assertNull(savedPlaceRepository.findById(savedPlace.getId()).orElse(null));

    }

    @Test // test that if a place is not found, return NOT FOUND
    public void deletePlace_placeNotFound() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            savedPlaceService.deleteSavedPlace(1L, testUser.getToken()));

        assertEquals(404, ex.getStatusCode().value());

    }

    @Test // test that user can only delete own saved places, else reutrn UNAUTHORIZED
    public void deletePlace_wrongUser_returnUnauthorized() {
        // given a saved place for a user
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId("abcd");
        savedPlace.setName("Eiffel Tower");
        savedPlace.setAddress("Rue de Paris, 1000 Paris");
        savedPlace.setRating(4.5);
        savedPlace.setPhotoReference("98765");
        savedPlace.setLat(1234.1234);
        savedPlace.setLng(4321.4321);
        savedPlace.setTypes(Set.of("tourist_attraction", "establishment"));
        savedPlace.setUser(testUser);
        savedPlace.setCity("Paris");

        savedPlaceService.saveToUser(testUser.getId(), savedPlace);

        // when a different user wants to delete, then assert UNAUTHORIZED
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            savedPlaceService.deleteSavedPlace(savedPlace.getId(), testUser2.getToken()));

        assertEquals(401, ex.getStatusCode().value());
    }

}

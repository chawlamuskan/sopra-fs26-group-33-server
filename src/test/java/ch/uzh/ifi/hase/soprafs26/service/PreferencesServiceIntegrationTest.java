package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import jakarta.transaction.Transactional;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the PreferencesService, which also involves the database.
 *  - uses the actual PreferencesRepository and an in-memory database (H2) to perform real database operations during testing
 * 	- tests the full integration of the PreferencesService with the database
 *  
 * Detects:
 *  - logic errors in the PreferencesService, 
 *  - Database Constraints, 
 *  - JPA mapping errors, 
 * 	- and other issues that may arise when the PreferencesService interacts with the database.
 * 
 * For tests that do not involve the database, @see PreferencesServiceTest.
 */

@WebAppConfiguration
@SpringBootTest
public class PreferencesServiceIntegrationTest {

	@Qualifier("preferencesRepository")
	@Autowired
	private PreferencesRepository preferencesRepository;

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

    @Qualifier("invitationRepository")
	@Autowired
	private InvitationRepository invitationRepository;

	@Qualifier("travelBoardRepository")
	@Autowired
	private TravelBoardRepository travelBoardRepository;

    @Autowired
	private PreferencesService preferencesService;

	@Autowired
	private UserService userService;

    private User testUser;

	@BeforeEach
	public void setup() {
        invitationRepository.deleteAll();
        travelBoardRepository.deleteAll();
        preferencesRepository.deleteAll();
		userRepository.deleteAll();

        // GIVEN a registered user in the database
        testUser = new User();
        testUser.setName("testName");
        testUser.setUsername("testUsername");
        testUser.setEmail("test@example.com");
        testUser.setPassword("Test1234!");
        userService.createUser(testUser);
	}
	
	// ================ SAVE PREFERENCES TESTS ================
    @Test	// test that preferences can be created successfully with valid input
    public void savePreferences_validInput_success() {
        // GIVEN a registered user and valid preferences data
        Preferences preferences = new Preferences();
        preferences.setBio("Test bio");
        preferences.setProfilePicture("data:image/png;base64,abc123");
        preferences.setVisitedCountries(List.of("Switzerland", "Germany"));
        preferences.setWishlistCountries(List.of("Italy", "Spain"));
        preferences.setFriends(List.of(2L, 3L));

        // WHEN saving preferences for the user
        Preferences saved = preferencesService.savePreferences(testUser.getId(), preferences);

        // THEN all preferences are saved correctly in the database
        assertNotNull(saved.getId());
        assertEquals("Test bio", saved.getBio());
        assertEquals("data:image/png;base64,abc123", saved.getProfilePicture());
        assertEquals(List.of("Switzerland", "Germany"), saved.getVisitedCountries());
        assertEquals(List.of("Italy", "Spain"), saved.getWishlistCountries());
        assertEquals(List.of(2L, 3L), saved.getFriends());
    }

    @Test   // test that skipping preferences does not save any data
    public void skipPreferences_noDataSaved() {
        // GIVEN a registered user but with empty preferences data
        User user = userRepository.findByUsername("testUsername");
        assertNotNull(user);

        // WHEN no preferences are provided and savePreferences is called
        // THEN no preferences should be saved for the user
        Preferences saved = preferencesRepository.findByUser(testUser);
        assertNull(saved);
    }

    @Test   // test that only bio is updated when only bio is provided in the input
    public void savePreferences_onlyBio_success() {
        // GIVEN a user that sets preferences only for visited countries
        Preferences preferences = new Preferences();
        preferences.setBio("Test bio");

        // WHEN saving preferences for the user
        Preferences saved = preferencesService.savePreferences(testUser.getId(), preferences);

        // THEN only the provided fields are saved and others remain null or empty
        assertEquals("Test bio", saved.getBio());
        assertNull(saved.getProfilePicture());
        assertNull(saved.getVisitedCountries());
        assertNull(saved.getWishlistCountries());
        assertNull(saved.getFriends());
    }

    @Test   // test that only visited countries is updated when only visited countries is provided in the input
    public void savePreferences_onlyVisitedCountries_success() {
        // GIVEN a user that sets preferences only for visited countries
        Preferences preferences = new Preferences();
        preferences.setVisitedCountries(List.of("Switzerland", "Germany"));

        // WHEN saving preferences for the user
        Preferences saved = preferencesService.savePreferences(testUser.getId(), preferences);

        // THEN only the provided fields are saved and others remain null or empty
        assertNull(saved.getBio());
        assertNull(saved.getProfilePicture());
        assertEquals(List.of("Switzerland", "Germany"), saved.getVisitedCountries());
        assertNull(saved.getWishlistCountries());
        assertNull(saved.getFriends());
    }

    @Test   // test that only wishlist countries is updated when only wishlist countries is provided in the input
    public void savePreferences_onlyWishlistCountries_success() {
        // GIVEN a user that sets preferences only for wishlist countries
        Preferences preferences = new Preferences();
        preferences.setWishlistCountries(List.of("Italy", "Spain"));

        // WHEN saving preferences for the user
        Preferences saved = preferencesService.savePreferences(testUser.getId(), preferences);

        // THEN only the provided fields are saved and others remain null or empty
        assertNull(saved.getBio());
        assertNull(saved.getProfilePicture());
        assertNull(saved.getVisitedCountries());
        assertEquals(List.of("Italy", "Spain"), saved.getWishlistCountries());
        assertNull(saved.getFriends());
    }

    @Test   // test that only friends is updated when only friends is provided in the input
    public void savePreferences_onlyFriends_success() {
        // GIVEN a user that sets preferences only for friends
        Preferences preferences = new Preferences();
        preferences.setFriends(List.of(2L, 3L));

        // WHEN saving preferences for the user
        Preferences saved = preferencesService.savePreferences(testUser.getId(), preferences);

        // THEN only the provided fields are saved and others remain null or empty
        assertNull(saved.getBio());
        assertNull(saved.getProfilePicture());
        assertNull(saved.getVisitedCountries());
        assertNull(saved.getWishlistCountries());
        assertEquals(List.of(2L, 3L), saved.getFriends());
    }

    // ================ GET PREFERENCES TESTS ================
    @Test   // test that fetching existing preferences returns the correct data
    @Transactional
    public void getPreferences_existingPreferences_success() {
        // GIVEN a user with saved preferences in the database
        Preferences preferences = new Preferences();
        preferences.setBio("Test bio");
        preferences.setVisitedCountries(List.of("Switzerland", "Germany"));
        preferencesService.savePreferences(testUser.getId(), preferences);

        // WHEN fetching preferences for the user
        Preferences fetched = preferencesService.getPreferences(testUser.getId());

        // THEN the fetched preferences match the saved ones
        assertNotNull(fetched);
        assertEquals("Test bio", fetched.getBio());
        assertEquals(List.of("Switzerland", "Germany"), fetched.getVisitedCountries());
    }

    @Test  // test that fetching preferences for non-existent user throws an error
    public void getPreferences_nonExistentUser_throwsError() {
        // WHEN fetching preferences for a non-existent user
        // THEN an error is thrown
        assertThrows(ResponseStatusException.class, () -> 
                preferencesService.getPreferences(999L));
    }

    // ================ UPDATE PREFERENCES TESTS ================
    @Test   // test that partial update only changes provided fields
    @Transactional
    public void updatePreferences_partialUpdate_onlyUpdatesProvidedFields() {
        // GIVEN a user with exisitng full preferences
        Preferences initial = new Preferences();
        initial.setBio("Original bio");
        initial.setProfilePicture(("original.jpg"));
        initial.setVisitedCountries(List.of("Switzerland"));
        initial.setWishlistCountries(List.of("Japan"));
        initial.setFriends(List.of(2L));
        preferencesService.savePreferences(testUser.getId(), initial);

        // WHEN updating only visited countries
        Preferences update = new Preferences();
        update.setVisitedCountries(List.of("Switzerland", "Germany"));

        Preferences updated = preferencesService.savePreferences(testUser.getId(), update);

        // THEN only visited countries changed, all other fields remain unchanged
        assertEquals(List.of("Switzerland", "Germany"), updated.getVisitedCountries());   // updated
        assertEquals("Original bio", updated.getBio());                         // unchanged
        assertEquals("original.jpg", updated.getProfilePicture());              // unchanged
        assertEquals(List.of("Japan"), updated.getWishlistCountries());               // unchanged
        assertEquals(List.of(2L), updated.getFriends());                              // unchanged
    }

    @Test   // test that saving for non-existent user throws 404
    public void savePreferences_nonExistentUser_throwsNotFound() {
        // GIVEN a non-existent user ID
        Preferences preferences = new Preferences();
        preferences.setBio("Test bio");
        
        // WHEN saving preferences for non-existent user
        // THEN a 404 NOT FOUND error is thrown
        assertThrows(ResponseStatusException.class, () ->
            preferencesService.savePreferences(999L, preferences));
    }

    // ================ GET SAVED COUNTRIES TESTS ================
    @Test   // test that visited and wishlist countries saved
    @Transactional
    public void getSavedCountries_bothLists_returnsUnion() {
        // GIVEN a user with visited and wishlist countries saved
        Preferences preferences = new Preferences();
        preferences.setVisitedCountries(List.of("Switzerland", "Germany"));
        preferences.setWishlistCountries(List.of("Japan", "Brazil"));
        preferencesService.savePreferences(testUser.getId(), preferences);

        // WHEN fetching saved countries
        List<Map<String, String>> result = preferencesService.getSavedCountries(testUser.getId());

        // THEN all countries are returned with correct status
        assertEquals(4, result.size());
        assertTrue(result.stream().anyMatch(c ->
                c.get("countryName").equals("Switzerland") && c.get("status").equals("visited")));
        assertTrue(result.stream().anyMatch(c ->
                c.get("countryName").equals("Japan") && c.get("status").equals("wishlist")));
    }

    @Test   // test that a country in both lists appears only once as visited
    @Transactional
    public void getSavedCountries_countryInBothLists_returnedOnceAsVisited() {
        // GIVEN Switzerland in both visited and wishlist
        Preferences preferences = new Preferences();
        preferences.setVisitedCountries(List.of("Switzerland"));
        preferences.setWishlistCountries(List.of("Switzerland", "Japan"));
        preferencesService.savePreferences(testUser.getId(), preferences);

        // WHEN fetching saved countries
        List<Map<String, String>> result = preferencesService.getSavedCountries(testUser.getId());

        // THEN Switzerland appears only once as visited
        assertEquals(2, result.size());
        assertEquals(1, result.stream()
            .filter(c -> c.get("countryName").equals("Switzerland"))
            .count());
        assertTrue(result.stream().anyMatch(c -> 
            c.get("countryName").equals("Switzerland") && c.get("status").equals("visited")));
    }
}

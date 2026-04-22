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
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;

import java.util.List;

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

    @Autowired
	private PreferencesService preferencesService;

	@Autowired
	private UserService userService;

    private User testUser;

	@BeforeEach
	public void setup() {
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
}

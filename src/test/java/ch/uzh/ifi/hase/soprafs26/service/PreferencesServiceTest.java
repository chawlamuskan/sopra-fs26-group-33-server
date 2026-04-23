package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the PreferencesService.
 *  - mocks with Mockito (fakes the database responses - no actual database operations are performed)
 * 	- tests the business logic of the PreferencesService in isolation
 * 
 * Detects:
 *  - logic errors in the PreferencesService
 * 
 * For tests that also involve the database, @see PreferencesServiceIntegrationTest.
 */

public class PreferencesServiceTest {

	@Mock
	private UserRepository userRepository;

    @Mock
	private PreferencesRepository preferencesRepository;

	@InjectMocks
	private PreferencesService preferencesService;

	private User testUser;

	// sets up a valid user so all tests can start from a clean slate
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");

		Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
		Mockito.when(preferencesRepository.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
	}

	// ========================= SAVE PREFERENCES TESTS =========================
	@Test 	// test that preferences can be created successfully when none exist yet
	public void savePreferences_noExistingPreferences_createsNew() {
		// GIVEN no exisitng preferences for the user
		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(null);

		Preferences newPrefs = new Preferences();
		newPrefs.setBio("Test bio");
		newPrefs.setVisitedCountries(List.of("Switzerland"));

		// WHEN saving preferences for the first time
		Preferences saved = preferencesService.savePreferences(1L, newPrefs);

		// THEN preferences are saved and linked to the user
		assertEquals("Test bio", saved.getBio());
		assertEquals(List.of("Switzerland"), saved.getVisitedCountries());
		assertEquals(testUser, saved.getUser());
		Mockito.verify(preferencesRepository, Mockito.times(1)).save(Mockito.any());
	}

	@Test	// test that existing preferences are updated with only the provided fields
	public void savePreferences_existingPreferences_updateOnlyProvidedFields() {
		// GIVEN exisitng preferences with all fields set
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		existing.setBio("Original bio");
		existing.setProfilePicture("original.jpg");
		existing.setVisitedCountries(List.of("Switzerland"));
		existing.setWishlistCountries(List.of("Japan"));
		existing.setFriends(List.of(2L));

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN updating only visited countries
		Preferences update = new Preferences();
		update.setVisitedCountries(List.of("Switzerland", "Germany"));	// only these fields

		Preferences updated = preferencesService.savePreferences(1L, update);
		
		// THEN only visited countries changed, all other fields remain unchanged
		assertEquals(List.of("Switzerland", "Germany"), updated.getVisitedCountries());
		assertEquals("Original bio", updated.getBio());
		assertEquals("original.jpg", updated.getProfilePicture());
		assertEquals(List.of("Japan"), updated.getWishlistCountries());
		assertEquals((List.of(2L)), updated.getFriends());
	}

	@Test 	// test that saving preferences for a non-existent user throws error
	public void savePreferences_nonExistentUser_throwsNotFound() {
		// GIVEN a non-existent user ID
		Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

		Preferences preferences = new Preferences();
		preferences.setBio("Test bio");

		// WHEN saving preferences for a non-existent user
		// THEN 404 NOT FOUND error is thrown
		assertThrows(ResponseStatusException.class, () ->
				preferencesService.savePreferences(999L, preferences));
	}

	@Test 	// test that only bio is updated when only bio is provided
	public void savePreferences_onlyBio_updatedOnlyBio() {
		// GIVEN exisitng preferences with all fields set
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		existing.setVisitedCountries(List.of("Switzerland"));
		existing.setWishlistCountries(List.of("Japan"));

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN updating only bio
		Preferences update = new Preferences();
		update.setBio("New bio");

		Preferences updated = preferencesService.savePreferences(1L, update);

		// THEN only bio is updated, other fields remain unchanged
		assertEquals("New bio", updated.getBio());
		assertEquals(List.of("Switzerland"), updated.getVisitedCountries());	// unchanged
		assertEquals(List.of("Japan"), updated.getWishlistCountries());	// unchanged

	}

	@Test 	// test that only friends are updated when only friends are provided
	public void savePreferences_onlyFriends_updatedOnlyFriends() {
		// GIVEN exisitng preferences with all fields set
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		existing.setBio("Original bio");

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN updating only bio
		Preferences update = new Preferences();
		update.setFriends(List.of(2L, 3L));

		Preferences updated = preferencesService.savePreferences(1L, update);

		// THEN only friends are updated, other fields remain unchanged
		assertEquals(List.of(2L, 3L), updated.getFriends());
		assertEquals("Original bio", updated.getBio());	// unchanged
	}

	// ========================= GET PREFERENCES TESTS =========================
	@Test 	// test that existing preferences are returned correctly
	public void getPreferences_exisitngPreferences_success() {
		// GIVEN existing preferences for the user
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		existing.setBio("Test bio");
		existing.setVisitedCountries(List.of("Switzerland"));

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN fetching preferences
		Preferences fetched = preferencesService.getPreferences(1L);

		// THEN the correct preferences are rerturned
		assertNotNull(fetched);
		assertEquals("Test bio", fetched.getBio());
		assertEquals(List.of("Switzerland"), fetched.getVisitedCountries());
	}

	@Test 	// test that fetching preferences when none exists throws 404
	public void getPreferences_noPreferences_throwsNotFound() {
		// GIVEN no preferences for the user
		Mockito.when(preferencesRepository. findByUser(testUser)).thenReturn(null);

		// WHEN fetching preferences
		// THEN a 404 NOT FOUND error is thrown
		assertThrows(ResponseStatusException.class, () ->
			preferencesService.getPreferences(1L));
	}

	@Test 	// test that fetching preferences for non-existent user throws 404
	public void getPreferences_noExistentUser_throwsNotFound() {
		// GIVEN a non-existent user ID
		Mockito.when(preferencesRepository. findById(999L)).thenReturn(Optional.empty());

		// WHEN fetching preferences for a non-existent user
		// THEN a 404 NOT FOUND error is thrown
		assertThrows(ResponseStatusException.class, () ->
			preferencesService.getPreferences(999L));
	}

	// ========================= GET SAVED COUNTRIES TESTS =========================
	@Test 	// test that visited and wishlist countries are combined correctly
	public void getSavedCountries_bothLists_returnsUnion() {
		// GIVEN preferences with visited and wishlist countries
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		existing.setVisitedCountries(List.of("Switzerland", "Germany"));
		existing.setWishlistCountries(List.of("Japan","Brazil"));

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN fetching saved countries
		List<Map<String, String>> result = preferencesService.getSavedCountries(1L);

		// THEN all countries are returned with correct status
		assertEquals(4, result.size());
		assertTrue(result.stream().anyMatch(c ->
			c.get("countryName").equals("Switzerland") && c.get("status").equals("visited")));
		assertTrue(result.stream().anyMatch(c ->
			c.get("countryName").equals("Germany") && c.get("status").equals("visited")));
		assertTrue(result.stream().anyMatch(c ->
			c.get("countryName").equals("Japan") && c.get("status").equals("wishlist")));
		assertTrue(result.stream().anyMatch(c ->
			c.get("countryName").equals("Brazil") && c.get("status").equals("wishlist")));
	}

	@Test 	// test that a country in both lists is returned only once as visited
	public void getSavedCountries_countryBothLists_returnedOnceAsVisited() {
		// GIVEN preferences where Switzerland is in both visited and wishlist
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		existing.setVisitedCountries(List.of("Switzerland"));
		existing.setWishlistCountries(List.of("Switzerland","Japan")); 	// Switzerland in both

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN fetching saved countries
		List<Map<String, String>> result = preferencesService.getSavedCountries(1L);

		// THEN Switzerland appears only once as visited, Japan appears as wishlist
		assertEquals(2, result.size());
		assertTrue(result.stream().anyMatch(c ->
			c.get("countryName").equals("Switzerland") && c.get("status").equals("visited")));
		assertTrue(result.stream().anyMatch(c ->
			c.get("countryName").equals("Japan") && c.get("status").equals("wishlist")));
		// Switzerland should not appear twice
		assertEquals(1, result.stream()
				.filter(c -> c.get("countryName").equals("Switzerland"))
				.count());
	}

	@Test 	// test that empty lists return an empty result
	public void getSavedCountries_emptyLists_returnsEmpty() {
		// GIVEN preferences with no countries
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		existing.setVisitedCountries(List.of());
		existing.setWishlistCountries(List.of()); 

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN fetching saved countries
		List<Map<String, String>> result = preferencesService.getSavedCountries(1L);

		// THEN empty list is returned
		assertTrue(result.isEmpty());
	}	

	@Test 	// test that null lists are handled gracefully
	public void getSavedCountries_nullLists_returnsEmpty() {
		// GIVEN preferences with null country lists
		Preferences existing = new Preferences();
		existing.setUser(testUser);
		// visitedCountries and wishlistCountries not set (null)

		Mockito.when(preferencesRepository.findByUser(testUser)).thenReturn(existing);

		// WHEN fetching saved countries
		List<Map<String, String>> result = preferencesService.getSavedCountries(1L);

		// THEN empty list is returned without errors
		assertTrue(result.isEmpty());
	}

}
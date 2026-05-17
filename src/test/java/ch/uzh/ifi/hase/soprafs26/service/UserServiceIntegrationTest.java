package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SavedPlaceRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardPlaceRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserService, which also involves the database.
 *  - uses the actual UserRepository and an in-memory database (H2) to perform real database operations during testing
 * 	- tests the full integration of the UserService with the database
 *  
 * Detects:
 *  - logic errors in the UserService, 
 *  - Database Constraints, 
 *  - JPA mapping errors, 
 * 	- and other issues that may arise when the UserService interacts with the database.
 * 
 * For tests that do not involve the database, @see UserServiceTest.
 */

@WebAppConfiguration
@SpringBootTest
public class UserServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

	@Qualifier("preferencesRepository")
	@Autowired
	private PreferencesRepository preferencesRepository;

	@Qualifier("invitationRepository")
	@Autowired
	private InvitationRepository invitationRepository;

	@Qualifier("travelBoardRepository")
	@Autowired
	private TravelBoardRepository travelBoardRepository;

	@Autowired
	private UserService userService;

    @Autowired
    private PreferencesService preferencesService;

	@Qualifier("savedPlaceRepository")
	@Autowired
	private SavedPlaceRepository savedPlaceRepository;

	@Qualifier("travelBoardPlaceRepository")
	@Autowired
	private TravelBoardPlaceRepository travelBoardPlaceRepository;

	@Qualifier("friendRequestRepository")
	@Autowired
	private FriendRequestRepository friendRequestRepository;

	@BeforeEach
	public void setup() {
		invitationRepository.deleteAll();
		travelBoardRepository.deleteAll();
		preferencesRepository.deleteAll();
		friendRequestRepository.deleteAll();
		savedPlaceRepository.deleteAll();
		userRepository.deleteAll();
	}
	
	// ================ REGISTRATION TESTS ================
	@Test	// test that a user can be created successfully with valid input
	public void createUser_validInputs_success() {
		// GIVEN no user is present in the database
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");

		// WHEN user is created
		User createdUser = userService.createUser(testUser);

		// THEN check that the user is created correctly and persisted to DB
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getEmail(), createdUser.getEmail());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertNotNull(createdUser.getCreationDate());
	}

	@Test   // test that creating a user with a duplicate username throws an error
	public void createUser_duplicateUsername_throwsException() {
		// GIVEN a user with a specific username already exists in the database
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		// WHEN a second user attempts to register with the same username
		User testUser2 = new User();
		testUser2.setName("testName2");
		testUser2.setUsername("testUsername");	// duplicate username
		testUser2.setEmail("other@example.com");
		testUser2.setPassword("Test1234!");

		// THEN check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}

	@Test	// test that creating a user with a duplicate email throws an error
	public void createUser_duplicateEmail_throwsException() {
		// GIVEN a user with a specific email already exists in the database
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		// WHEN a second user attempts to register with the same email
		User testUser2 = new User();
		testUser2.setName("testName2");
		testUser2.setUsername("testUsername2");
		testUser2.setEmail("test@example.com");	// duplicate email
		testUser2.setPassword("Test1234!");

		// THEN check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}

	// ================ LOGIN TESTS ================
	@Test  // test that a user can log in successfully with valid credentials (by username)
	public void loginUser_byUsername_success() {
		// GIVEN a registered user in the database
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		// WHEN the user logs in with valid credentials (username and password)
		User loggedInUser = userService.loginUser("testUsername", null, "Test1234!");

		// THEN check that the user is logged in correctly (check ONLINE status, and non-null token)
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertNotNull(loggedInUser.getToken());
	}

	@Test  // test that a user can log in successfully with valid credentials (by email)
	public void loginUser_byEmail_success() {
		// GIVEN a registered user in the database
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		// WHEN the user logs in with valid credentials (username and password)
		User loggedInUser = userService.loginUser(null, "test@example.com", "Test1234!");

		// THEN check that the user is logged in correctly (ONLINE status, and non-null token)
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertNotNull(loggedInUser.getToken());
	}

	@Test	// test that logging in with invalid credentials throws an error
	public void loginUser_invalidCredentials_throwsException() {
		// GIVEN a registered user in the database
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		// WHEN the user attempts to log in with invalid credentials
		assertThrows(ResponseStatusException.class, () -> 
			userService.loginUser("testUsername", null, "InvalidPassword"));
	}

	@Test	// test that session persists after logging in (i.e. user remains ONLINE and token is valid)
	public void loginUser_sessionPersists_tokenStoredInDatabase() {
		// GIVEN a registered user in the database
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		// WHEN the user logs in with valid credentials
		User loggedInUser = userService.loginUser("testUsername", null, "Test1234!");
		
		// THEN token and ONLINE status are persisted in the database
		User userFromDB = userRepository.findByUsername("testUsername");
		assertNotNull(userFromDB.getToken());	// token persists in DB
		assertEquals(loggedInUser.getToken(), userFromDB.getToken());	// token in DB matches token returned by login method
		assertEquals(UserStatus.ONLINE, userFromDB.getStatus());	// status persists in DB
	}


// ================ LOGOUT TESTS ================
	@Test  // test that session token is deleted after logout
	public void logoutUser_deletesToken() {
		// GIVEN a logged in user
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		User loggedInUser = userService.loginUser("testUsername", null, "Test1234!");
		String token = loggedInUser.getToken();
		assertNotNull(token);	// verify that token is generated upon login

		// WHEN the user logs out
		userService.logoutByToken(token);

		// THEN check that token in null in database
		User userFromDB = userRepository.findByUsername("testUsername");
		assertNull(userFromDB.getToken());
	}

	@Test  // test that user status is set to OFFLINE after logout
	public void logoutUser_statusSetOffline() {
		// GIVEN a logged in user
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		User loggedInUser = userService.loginUser("testUsername", null, "Test1234!");
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());  // verify that user is ONLINE after login
		
		// WHEN the user logs out
		userService.logoutByToken(loggedInUser.getToken());

		// THEN check that user status is OFFLINE in the database
		User userFromDB = userRepository.findByUsername("testUsername");
		assertEquals(UserStatus.OFFLINE, userFromDB.getStatus());
	}

	@Test  // test that restricted pages cannot be accessed after logout (i.e. token is invalidated)
	public void logoutUser_restrictedAccessAfterLogout() {
		// GIVEN a logged in user
		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		User loggedInUser = userService.loginUser("testUsername", null, "Test1234!");
		String token = loggedInUser.getToken();
		
		// WHEN the user logs out
		userService.logoutByToken(token);

		// THEN check that old token is not accepted by validateToken
		assertThrows(ResponseStatusException.class, () -> userService.validateToken(token));
	}

	@Test // --- START TEST: delete user removes associated data ---
	@Transactional
	public void deleteUser_deletesAssociatedData() {
		// GIVEN a registered user with preferences, a travel board, saved place, travelBoardPlace, invitation, friend requests and friendships
		User testUser = new User();
		testUser.setName("toDelete");
		testUser.setUsername("deleteUser");
		testUser.setEmail("delete@example.com");
		testUser.setPassword("Test1234!");
		userService.createUser(testUser);

		// Create a friend user
		User friendUser = new User();
		friendUser.setName("friend");
		friendUser.setUsername("friendUser");
		friendUser.setEmail("friend@example.com");
		friendUser.setPassword("Test1234!");
		userService.createUser(friendUser);

		// preferences
		Preferences prefs = new Preferences();
		prefs.setBio("bye");
		prefs.setProfilePicture("pic");
		// save via service so it is linked to the user
		preferencesService.savePreferences(testUser.getId(), prefs);

		// travel board owned by user
		TravelBoard board = new TravelBoard();
		board.setName("UserBoard");
		board.setPrivacy(PrivacyLevel.PRIVATE);
		board.setDateCreated(java.time.LocalDate.now());
		board.setOwner(testUser);
		travelBoardRepository.save(board);

		// saved place by user
		SavedPlace sp = new SavedPlace();
		sp.setExternalPlaceId("ext1");
		sp.setName("Place");
		sp.setAddress("Addr");
		sp.setUser(testUser);
		savedPlaceRepository.save(sp);

		// travel board place by user
		TravelBoardPlace tbp = new TravelBoardPlace();
		tbp.setExternalPlaceId("extTB");
		tbp.setName("TBPlace");
		tbp.setAddress("Addr");
		tbp.setUser(testUser);
		tbp.setBoard(board);
		travelBoardPlaceRepository.save(tbp);

		// invitation sent by user
		Invitation inv = new Invitation();
		inv.setBoard(board);
		inv.setSender(testUser);
		inv.setStatus(InviteStatus.PENDING);
		invitationRepository.save(inv);

		// friend request sent by user
		FriendRequest fr1 = new FriendRequest();
		fr1.setSender(testUser);
		fr1.setReceiver(friendUser);
		fr1.setStatus(FriendRequestStatus.PENDING);
		friendRequestRepository.save(fr1);

		// friend request received by user
		FriendRequest fr2 = new FriendRequest();
		fr2.setSender(friendUser);
		fr2.setReceiver(testUser);
		fr2.setStatus(FriendRequestStatus.PENDING);
		friendRequestRepository.save(fr2);

		// Add testUser to friendUser's friends list
		// Re-fetch both users to get managed entities within this transaction
		User managedTestUser = userRepository.findById(testUser.getId()).orElseThrow();
		User managedFriendUser = userRepository.findById(friendUser.getId()).orElseThrow();
		managedFriendUser.getFriends().add(managedTestUser);
		managedTestUser.getFriends().add(managedFriendUser);
		userRepository.save(managedTestUser);
		userRepository.save(managedFriendUser);

		Long id = testUser.getId();

		// WHEN deleting the user
		userService.deleteUser(id);

		// THEN the user's data should be gone
		assertNull(preferencesRepository.findByUser(testUser));
		assertTrue(travelBoardRepository.findByOwnerId(id).isEmpty());
		assertTrue(savedPlaceRepository.findAllByUser(testUser).isEmpty());
		assertTrue(travelBoardPlaceRepository.findAll().stream().noneMatch(p -> p.getUser() != null && p.getUser().getId().equals(id)));
		assertTrue(invitationRepository.findAll().stream().noneMatch(i -> (i.getSender() != null && i.getSender().getId().equals(id)) || (i.getReceiver() != null && i.getReceiver().getId().equals(id))));
		
		// Friend requests should be deleted
		assertTrue(friendRequestRepository.findAll().stream().noneMatch(fr -> 
			(fr.getSender() != null && fr.getSender().getId().equals(id)) || 
			(fr.getReceiver() != null && fr.getReceiver().getId().equals(id))));
		
		// User should be removed from other users' friends lists
		User updatedFriendUser = userRepository.findById(managedFriendUser.getId()).orElse(null);
		assertNotNull(updatedFriendUser);
		assertTrue(updatedFriendUser.getFriends().stream().noneMatch(f -> f.getId().equals(id)));
	}
	// --- END TEST: delete user removes associated data ---

}
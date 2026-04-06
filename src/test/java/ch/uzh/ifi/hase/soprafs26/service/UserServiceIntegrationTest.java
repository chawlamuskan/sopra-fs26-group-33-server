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

	@Autowired
	private UserService userService;

	@BeforeEach
	public void setup() {
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
		testUser.setBio("This is a test bio");

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
		testUser.setBio("This is a test bio");
		userService.createUser(testUser);

		// WHEN a second user attempts to register with the same username
		User testUser2 = new User();
		testUser2.setName("testName2");
		testUser2.setUsername("testUsername");	// duplicate username
		testUser2.setEmail("other@example.com");
		testUser2.setPassword("Test1234!");
		testUser2.setBio("This is another test bio");

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
		testUser.setBio("This is a test bio");
		userService.createUser(testUser);

		// WHEN a second user attempts to register with the same email
		User testUser2 = new User();
		testUser2.setName("testName2");
		testUser2.setUsername("testUsername2");
		testUser2.setEmail("test@example.com");	// duplicate email
		testUser2.setPassword("Test1234!");
		testUser2.setBio("This is another test bio");

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
		testUser.setBio("This is a test bio");
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
		testUser.setBio("This is a test bio");
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
		testUser.setBio("This is a test bio");
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
		testUser.setBio("This is a test bio");
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

}
package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the UserService.
 *  - mocks with Mockito (fakes the database responses - no actual database operations are performed)
 * 	- tests the business logic of the UserService in isolation
 * 
 * Detects:
 *  - logic errors in the UserService
 * 
 * For tests that also involve the database, @see UserServiceIntegrationTest.
 */

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User testUser;

	// sets up a valid user so all tests can start from a clean slate
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		// given
		testUser = new User();
		testUser.setId(1L);
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!"); 	// valid password

		// when a user is saved in the userRepository, return the dummy testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	// ================ REGISTRATION TESTS ================
	@Test 	// test that a user can be created successfully with valid input
	public void createUser_validInputs_success() {

		// WHENEVER someone searches for a username or email, no duplicate user exists
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null); // No user with the same username
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(null); // No user with the same email

		// WHEN user is created
		User createdUser = userService.createUser(testUser);

		// THEN verify user was saved once and all attributes are correct
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getEmail(), createdUser.getEmail());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}
	
	@Test  // test that creating a user with a duplicate username throws an error
	public void createUser_duplicateUsername_throwsException() {
		// GIVEN a first user that has already been created

		// WHENEVER someone searches for a username, 
		// pretend there is already a user with the same username
		// but no user exists with the same email
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(null);

		// THEN attempt to create another user with same username 
		// check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test  // test that creating a user with a duplicate email throws an error
	public void createUser_duplicateEmail_throwsException() {
		// GIVEN a first user that has already been created

		// WHENEVER someone searches for an email, 
		// pretend there is already a user with the same email
		// but no user exists with the same username
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

		// THEN attempt to create another user with same email
		// check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test  // test that creating a user with an invalid password throws an error
	public void createUser_invalidPassword_throwsException() {
		// GIVEN a first user that has already been created

		// WHENEVER someone searches for an email or username, but no duplicate user exists
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(null);

		// THEN check that an error is thrown 
		// when trying to create a user with an invalid password (all cases of invalidity)
		testUser.setPassword("Ab1!"); // too short
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	
		testUser.setPassword("abcdef1!"); // no uppercase
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	
		testUser.setPassword("Abcdefg!"); // no digit
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	
		testUser.setPassword("Abcdef12"); // no special character
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	// ================ LOGIN TESTS ================

	@Test 	// test that a user can log in successfully with valid credentials (by username)
	public void loginUser_byUsername_success() {
		// WHENEVER someone searches for a username, return the testUser
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);
		
		// WHEN the user logs in with valid credentials (username and password)
		User loggedInUser = userService.loginUser("testUsername", null, "Test1234!");

		// THEN check that the user is logged in correctly (check ONLINE status, and non-null token)
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertNotNull(loggedInUser.getToken());
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
	}
	
	@Test 	// test that a user can log in successfully with valid credentials (by email)
	public void loginUser_byEmail_success() {
		// WHENEVER someone searches for an email, return the testUser
        Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(testUser);
		
		// WHEN the user logs in with valid credentials (email and password)
		User loggedInUser = userService.loginUser(null, "test@example.com", "Test1234!");

		// THEN check that the user is logged in correctly (check ONLINE status, and non-null token)
		assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
		assertNotNull(loggedInUser.getToken());
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
	}

	@Test 	// test that logging in with invalid credentials throws an error
	public void loginUser_invalidCredentials_throwsException() {
		// WHENEVER someone searches for a username, return the testUser
        Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
		
		// WHEN the user attempts to log in with invalid credentials
		// THEN check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> 
			userService.loginUser("testUsername", null, "InvalidPassword"));
	}

	@Test 	// test that logging in with non-existent username throws an error
	public void loginUser_nonExistentUsername_throwsException() {
		// WHENEVER someone searches for a username, return null (no user found)
        Mockito.when(userRepository.findByUsername("nonExistent")).thenReturn(null);
		
		// WHEN the user attempts to log in with non-existent username
		// THEN check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> 
			userService.loginUser("nonExistent", null, "Test1234!"));
	}

	@Test 	// test that logging in with non-existent email throws an error
	public void loginUser_nonExistentEmail_throwsException() {
		// WHENEVER someone searches for an email, return null (no user found)
        Mockito.when(userRepository.findByEmail("nonExistent@example.com")).thenReturn(null);
		
		// WHEN the user attempts to log in with non-existent email
		// THEN check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> 
			userService.loginUser(null, "nonExistent@example.com", "Test1234!"));
	}	

}
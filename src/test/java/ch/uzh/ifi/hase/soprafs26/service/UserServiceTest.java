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

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
	}

	@Test
	public void createUser_validInputs_success() {

		// Mocking user repository methods to return null for both username and name
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null); // No user with the same username
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(null); // No user with the same email

		// when -> any object is being save in the userRepository -> return the dummy
		// testUser
		User createdUser = userService.createUser(testUser);

		// then
		Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getEmail(), createdUser.getEmail());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
	}
	
	@Test
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

	@Test
	public void createUser_duplicateEmail_throwsException() {
		// GIVEN a first user that has already been created

		// WHENEVER someone searches for an email, 
		// pretend there is already a user with the same email
		// but no user exists with the same username
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(testUser);
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

		// THEN -> attempt to create another user with same email
		// check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
	}

	@Test
	public void createUser_invalidPassword_throwsException() {
		// GIVEN a first user that has already been created

		// WHENEVER someone searches for an email or username, but no duplicate user exists
		Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);
		Mockito.when(userRepository.findByEmail(Mockito.any())).thenReturn(null);

		// THEN -> check that an error is thrown 
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
}
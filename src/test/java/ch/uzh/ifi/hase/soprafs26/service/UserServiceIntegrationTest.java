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
 * Test class for the UserResource REST resource.
 *
 * @see UserService
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
	
	@Test
	public void createUser_validInputs_success() {
		// given
		assertNull(userRepository.findByUsername("testUsername"));

		User testUser = new User();
		testUser.setName("testName");
		testUser.setUsername("testUsername");
		testUser.setEmail("test@example.com");
		testUser.setPassword("Test1234!");
		testUser.setBio("This is a test bio");

		// WHEN user is created
		User createdUser = userService.createUser(testUser);

		// THEN check that the user is created correctly
		assertEquals(testUser.getId(), createdUser.getId());
		assertEquals(testUser.getName(), createdUser.getName());
		assertEquals(testUser.getUsername(), createdUser.getUsername());
		assertEquals(testUser.getEmail(), createdUser.getEmail());
		assertNotNull(createdUser.getToken());
		assertEquals(UserStatus.ONLINE, createdUser.getStatus());
		assertNotNull(createdUser.getCreationDate());
	}

	@Test
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
		testUser2.setUsername("testUsername");
		testUser2.setEmail("other@example.com");
		testUser2.setPassword("Test1234!");
		testUser2.setBio("This is another test bio");

		// THEN check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}

	@Test
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
		testUser2.setEmail("test@example.com");
		testUser2.setPassword("Test1234!");
		testUser2.setBio("This is another test bio");

		// THEN check that an error is thrown
		assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser2));
	}
}
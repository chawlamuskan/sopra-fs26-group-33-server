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
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;

import java.util.List;
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

	private User user;

	// sets up a valid user so all tests can start from a clean slate
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

		user = new User();
        user.setId(1L);
        user.setUsername("testUser");
	}

	// ================ SAVE PREFERENCES ================
    
    // ================ GET PREFERENCES ================


}
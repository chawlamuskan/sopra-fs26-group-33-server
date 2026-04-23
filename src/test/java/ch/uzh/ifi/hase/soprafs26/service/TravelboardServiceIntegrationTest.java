package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * Test class for the UserResource REST resource.
 *
 * @see TravelBoardService
 */
@WebAppConfiguration
@SpringBootTest
public class TravelboardServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

    @Qualifier("preferencesRepository")
    @Autowired
    private PreferencesRepository preferencesRepository;

	@Qualifier("travelBoardRepository")
	@Autowired
	private TravelBoardRepository travelBoardRepository;

	@Autowired
	private TravelBoardService travelBoardService;

	@BeforeEach
	public void setup() {
		travelBoardRepository.deleteAll();
        preferencesRepository.deleteAll();
        userRepository.deleteAll();
	}
    
    //#118
    @Test
    public void deleteTravelBoard_removesBoardFromDatabase() {
        // create user
        User user = new User();
        user.setName("owner");
        user.setUsername("owner123");
        user.setPassword("pw");
        user.setEmail("owner123@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("token123");
        user = userRepository.save(user);
    
        // simulate login token
        user.setToken("token123");
        userRepository.save(user);
    
        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Test Board");
        board.setOwner(user);
        board.setInviteCode("DEL123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
    
        board = travelBoardRepository.save(board);
    
        Long boardId = board.getId();
    
        // delete
        travelBoardService.deleteTravelBoard(boardId, "token123");
    
        // assert
        assertTrue(travelBoardRepository.findById(boardId).isEmpty());
    }

    //#119
    @Test
    public void renameTravelBoard_updatesBoardNameInDatabase() {
        // create user
        User user = new User();
        user.setName("owner");
        user.setUsername("owner123");
        user.setPassword("pw");
        user.setEmail("owner123@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("token123");
        user = userRepository.save(user);

        // simulate login token
        user.setToken("token123");
        userRepository.save(user);

        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Old Name");
        board.setOwner(user);
        board.setInviteCode("REN123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());

        board = travelBoardRepository.save(board);

        Long boardId = board.getId();

        // rename
        travelBoardService.renameTravelBoard(boardId, "token123", "New Name");

        // assert
        TravelBoard updated = travelBoardRepository.findById(boardId).orElseThrow();

        assertEquals("New Name", updated.getName());
    }
}
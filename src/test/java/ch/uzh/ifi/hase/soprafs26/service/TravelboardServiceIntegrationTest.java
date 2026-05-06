package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
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

    @Qualifier("invitationRepository")
    @Autowired
    private InvitationRepository invitationRepository;

    @Qualifier("friendRequestRepository")
    @Autowired
    private FriendRequestRepository friendRequestRepository;

	@Autowired
	private TravelBoardService travelBoardService;

	@BeforeEach
	public void setup() {
        invitationRepository.deleteAll();
        friendRequestRepository.deleteAll();        
		travelBoardRepository.deleteAll();
        preferencesRepository.deleteAll();
        userRepository.deleteAll();
	}
    
    //#135
    @Test
    public void createTravelBoard_validInput_createsTravelBoard() {
        User user = createTestUser("owner", "owner123", "token123");

        // create travel board input
        TravelBoard board = new TravelBoard();
        board.setName("Test Board");
        board.setInviteCode("CRE123");
        board.setPrivacy(PrivacyLevel.PRIVATE);
        board.setDateCreated(LocalDate.now());

        // create board
        TravelBoard createdBoard = travelBoardService.createTravelBoard(board, user.getToken());

        // assert
        assertNotNull(createdBoard.getId());
        assertEquals("Test Board", createdBoard.getName());
        assertEquals(user.getId(), createdBoard.getOwner().getId());
        assertEquals("CRE123", createdBoard.getInviteCode());
        assertEquals(PrivacyLevel.PRIVATE, createdBoard.getPrivacy());

        TravelBoard storedBoard = travelBoardRepository.findById(createdBoard.getId()).orElseThrow();
        assertEquals("Test Board", storedBoard.getName());
    }

    //#136 - Missing name
    @Test
    public void createTravelBoard_missingName_throwsBadRequest() {
        User user = createTestUser("owner", "owner123", "token123");

        //create board
        TravelBoard board = new TravelBoard();
        board.setName(null);
        board.setPrivacy(PrivacyLevel.PRIVATE);
        board.setInviteCode("CODE123");

        // assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> travelBoardService.createTravelBoard(board, user.getToken())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#136 - Missing privacy
    @Test
    public void createTravelBoard_missingPrivacy_throwsBadRequest() {
        User user = createTestUser("owner", "owner123", "token123");

        //create board
        TravelBoard board = new TravelBoard();
        board.setName("Trip");
        board.setPrivacy(null);
        board.setInviteCode("CODE123");

        // assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> travelBoardService.createTravelBoard(board, user.getToken())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#136 - Invalid dates
    @Test
    public void createTravelBoard_startDateAfterEndDate_throwsBadRequest() {
        User user = createTestUser("owner", "owner123", "token123");

        //create board
        TravelBoard board = new TravelBoard();
        board.setName("Trip");
        board.setPrivacy(PrivacyLevel.PRIVATE);
        board.setInviteCode("CODE123");
        board.setStartDate(LocalDate.of(2026, 6, 10));
        board.setEndDate(LocalDate.of(2026, 6, 5));

        // assert
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> travelBoardService.createTravelBoard(board, user.getToken())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#136 - Duplicate invite Code ,#138
    @Test
    public void createTravelBoard_duplicateInviteCode_throwsConflict() {
        User user = createTestUser("owner", "owner123", "token123");

        //create first board
        TravelBoard firstBoard = new TravelBoard();
        firstBoard.setName("First Trip");
        firstBoard.setPrivacy(PrivacyLevel.PRIVATE);
        firstBoard.setInviteCode("DUP123");
        firstBoard = travelBoardService.createTravelBoard(firstBoard, user.getToken());

        //create second board with the same invite Code
        TravelBoard secondBoard = new TravelBoard();
        secondBoard.setName("Second Trip");
        secondBoard.setPrivacy(PrivacyLevel.PRIVATE);
        secondBoard.setInviteCode("DUP123");

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> travelBoardService.createTravelBoard(secondBoard, user.getToken())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    //#118
    @Test
    public void deleteTravelBoard_removesBoardFromDatabase() {
        User user = createTestUser("owner", "owner123", "token123");
        TravelBoard board = createTestBoard("Test Board", user, "CODE123", PrivacyLevel.PUBLIC); 
    
        // delete
        travelBoardService.deleteTravelBoard(board.getId(), "token123");
    
        // assert
        assertTrue(travelBoardRepository.findById(board.getId()).isEmpty());
    }

    //#119
    @Test
    public void renameTravelBoard_updatesBoardNameInDatabase() {
        User user = createTestUser("owner", "owner123", "token123");
        TravelBoard board = createTestBoard("Old Name", user, "CODE123", PrivacyLevel.PUBLIC); 

        // rename
        travelBoardService.renameTravelBoard(board.getId(), "token123", "New Name");

        // assert
        TravelBoard updated = travelBoardRepository.findById(board.getId()).orElseThrow();

        assertEquals("New Name", updated.getName());
    }

    //#139
    @Test
    public void getInviteCode_returnsCorrectCodeForEachBoard() {
        User user = createTestUser("owner", "owner123", "token123");
        TravelBoard board1 = createTestBoard("First Trip", user, "1CODE123", PrivacyLevel.PRIVATE);
        TravelBoard board2 = createTestBoard("SecondTrip", user, "2CODE123", PrivacyLevel.PUBLIC);

        // get invite codes
        String inviteCode1 = travelBoardService.getInviteCode(board1.getId());
        String inviteCode2 = travelBoardService.getInviteCode(board2.getId());

        // assert
        assertEquals("1CODE123", inviteCode1);
        assertEquals("2CODE123", inviteCode2);
        assertNotEquals(inviteCode1, inviteCode2);
    }

    //#153,#117
    @Test
    public void joinTravelBoard_validCode_userAddedToMembers() {
        User user = createTestUser("owner", "owner123", "token123");
        User joiner = createTestUser("joiner", "joiner123", "token456");
        TravelBoard board = createTestBoard("Test Board", user, "CODE123", PrivacyLevel.PUBLIC); 

        // join add member
        travelBoardService.joinTravelBoardByInviteCode(joiner.getToken(), "CODE123");

        // assert
        assertTrue(travelBoardService.getTravelBoardsByUser(joiner.getToken())
                .stream() // verifies that the board the joiner joined is now included in their travel board list
                .anyMatch(b -> b.getId().equals(board.getId()))); // true if any board in that list has the expected ID
    }

    //#265
    @Test
    public void leaveTravelBoard_validMember_removesOnlyThatUsersMembership() {
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        User member1 = createTestUser("member1", "member123", "member123");
        User member2 = createTestUser("member2", "member456", "member456");

        TravelBoard board = createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC); 
        board.getMembers().add(member1);
        board.getMembers().add(member2);
        board = travelBoardRepository.save(board);
        Long boardId = board.getId();

        // member1 leave board
        travelBoardService.leaveTravelBoard(board.getId(), member1.getToken());

        // assert
        assertTrue(travelBoardRepository.findById(boardId).isPresent());
        assertFalse(travelBoardRepository.findByMembersId(member1.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(boardId)));
        assertTrue(travelBoardRepository.findByMembersId(member2.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(boardId)));
    }

    private User createTestUser(String name, String username, String token) {
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setPassword("pw");
        user.setEmail(username + "@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken(token);
        return userRepository.save(user);
    }

    private TravelBoard createTestBoard(String name, User owner, String inviteCode, PrivacyLevel privacy) {
        TravelBoard board = new TravelBoard();
        board.setName(name);
        board.setOwner(owner);
        board.setInviteCode(inviteCode);
        board.setPrivacy(privacy);
        board.setDateCreated(LocalDate.now());
        return travelBoardRepository.save(board);
    }
}
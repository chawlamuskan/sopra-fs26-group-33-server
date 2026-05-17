package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Test class for the TravelBoardService.
 *  - mocks with Mockito (fakes the database responses - no actual database operations are performed)
 * 	- tests the business logic of the TravelBoardService in isolation
 * 
 * Detects:
 *  - logic errors in the TravelBoardService
 * 
 * For tests that also involve the database, @see TravelBoardServiceIntegrationTest.
 */

public class TravelBoardServiceTest {

	@Mock
	private TravelBoardRepository travelBoardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogService activityLogService;
	@InjectMocks
	private TravelBoardService travelBoardService;

    private User owner;
    private User member;
    private TravelBoard board;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

        owner = createTestUser(1L, "owner", "owner123", "ownertoken123");
        member = createTestUser(2L, "member", "member123", "membertoken123");
        board = createTestBoard(10L, "Test Board", owner, "CODE123", PrivacyLevel.PUBLIC);

		Mockito.when(travelBoardRepository.save(Mockito.any()))
				.thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(userRepository.save(Mockito.any()))
				.thenAnswer(invocation -> invocation.getArgument(0));

        doNothing().when(activityLogService).log(any(), any(), anyString());
	}

	// ================ TESTS CREATE TRAVEL BOARD ================
    //#135
    @Test
    public void createTravelBoard_validInput_success() {
        // given
        TravelBoard newBoard = new TravelBoard();
        newBoard.setName("Test Board");
        newBoard.setPrivacy(PrivacyLevel.PRIVATE);
        newBoard.setInviteCode("code123");

        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findByInviteCode("CODE123")).thenReturn(null);

        // when
        TravelBoard createdBoard = travelBoardService.createTravelBoard(newBoard, owner.getToken());

        // then
        assertEquals("Test Board", createdBoard.getName());
        assertEquals(owner, createdBoard.getOwner());
        assertEquals(PrivacyLevel.PRIVATE, createdBoard.getPrivacy());
        assertEquals("CODE123", createdBoard.getInviteCode());
        assertNotNull(createdBoard.getDateCreated());
    }
	
    //#136
    @Test
    public void createTravelBoard_missingName_throwsBadRequest() {
        // given
        TravelBoard newBoard = new TravelBoard();
        newBoard.setName(null);
        newBoard.setPrivacy(PrivacyLevel.PRIVATE);

        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.createTravelBoard(newBoard, owner.getToken())
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#136
    @Test
    public void createTravelBoard_missingPrivacy_throwsBadRequest() {
        // given
        TravelBoard newBoard = new TravelBoard();
        newBoard.setName("Test Board");
        newBoard.setPrivacy(null);

        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.createTravelBoard(newBoard, owner.getToken())
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#136
    @Test
    public void createTravelBoard_startDateAfterEndDate_throwsBadRequest() {
        // given
        TravelBoard newBoard = new TravelBoard();
        newBoard.setName("Test Board");
        newBoard.setPrivacy(PrivacyLevel.PRIVATE);
        newBoard.setStartDate(LocalDate.of(2026, 6, 10));
        newBoard.setEndDate(LocalDate.of(2026, 6, 5));

        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.createTravelBoard(newBoard, owner.getToken())
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#138
    @Test
    public void createTravelBoard_duplicateInviteCode_throwsConflict() {
        // given
        TravelBoard newBoard = new TravelBoard();
        newBoard.setName("Test Board");
        newBoard.setPrivacy(PrivacyLevel.PRIVATE);
        newBoard.setInviteCode("CODE123");

        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findByInviteCode("CODE123")).thenReturn(board);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.createTravelBoard(newBoard, owner.getToken())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
	
	// ================ TESTS RENAME TRAVEL BOARD ================
    //#119
    @Test
    public void renameTravelBoard_validInput_success() {
        // given
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        travelBoardService.renameTravelBoard(board.getId(), owner.getToken(), "New Name");

        // then
        assertEquals("New Name", board.getName());
    }

    //#120
    @Test
    public void renameTravelBoard_nonOwner_throwsUnauthorized() {
        // given
        User nonOwner = createTestUser(3L, "nonOwner", "nonOwner123", "nonownertoken123");

        Mockito.when(userRepository.findByToken(nonOwner.getToken())).thenReturn(nonOwner);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.renameTravelBoard(board.getId(), nonOwner.getToken(), "New Name")
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    //#119
    @Test
    public void renameTravelBoard_emptyName_throwsBadRequest() {
        // given
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.renameTravelBoard(board.getId(), owner.getToken(), "   ")
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

	// ================ TESTS DELETE TRAVEL BOARD ================
    //#118
    @Test
    public void deleteTravelBoard_validOwner_success() {
        // given
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        travelBoardService.deleteTravelBoard(board.getId(), owner.getToken());

        // then
   
    }

    //#120
    @Test
    public void deleteTravelBoard_nonOwner_throwsUnauthorized() {
        // given
        User nonOwner = createTestUser(3L, "nonOwner", "nonOwner123", "nonownertoken123");

        Mockito.when(userRepository.findByToken(nonOwner.getToken())).thenReturn(nonOwner);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.deleteTravelBoard(board.getId(), nonOwner.getToken())
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    // ================ TESTS LEAVE TRAVEL BOARD ================
    //#265
    @Test
    public void leaveTravelBoard_validMember_success() {
        // given
        board.getMembers().add(member);

        Mockito.when(userRepository.findByToken(member.getToken())).thenReturn(member);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        travelBoardService.leaveTravelBoard(board.getId(), member.getToken());

        // then
        assertFalse(board.getMembers().contains(member));
    }

    //#265
    @Test
    public void leaveTravelBoard_owner_throwsBadRequest() {
        // given
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.leaveTravelBoard(board.getId(), owner.getToken())
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#265
    @Test
    public void leaveTravelBoard_nonMember_throwsNotFound() {
        // given
        Mockito.when(userRepository.findByToken(member.getToken())).thenReturn(member);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.leaveTravelBoard(board.getId(), member.getToken())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // ================ TESTS GET ALL TRAVEL BOARDS BY USER ================    
    //#115
    @Test
    public void getTravelBoardsByUser_returnsCreatedAndJoinedBoards() {
        // given
        TravelBoard ownedBoard = createTestBoard(11L, "Owned Board", owner, "OWN123", PrivacyLevel.PUBLIC);
        TravelBoard joinedBoard = createTestBoard(12L, "Joined Board", createTestUser(3L, "otherOwner", "otherOwner123", "othertoken123"), "JOIN123", PrivacyLevel.PRIVATE);

        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findByOwnerId(owner.getId())).thenReturn(List.of(ownedBoard));
        Mockito.when(travelBoardRepository.findByMembersId(owner.getId())).thenReturn(List.of(joinedBoard));

        // when
        List<TravelBoard> result = travelBoardService.getTravelBoardsByUser(owner.getToken());

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(ownedBoard));
        assertTrue(result.contains(joinedBoard));
    }

    //#115
    @Test
    public void getTravelBoardsByUser_doesNotReturnDuplicateBoard() {
        // given
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findByOwnerId(owner.getId())).thenReturn(List.of(board));
        Mockito.when(travelBoardRepository.findByMembersId(owner.getId())).thenReturn(List.of(board));

        // when
        List<TravelBoard> result = travelBoardService.getTravelBoardsByUser(owner.getToken());

        // then
        assertEquals(1, result.size());
        assertTrue(result.contains(board));
    }

	// ================ TESTS GET SINGLE TRAVEL BOARD ================
    //#316
    @Test
    public void getSingleTravelBoardById_owner_returnsBoard() {
        // given
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        TravelBoard foundBoard = travelBoardService.getSingleTravelBoardById(board.getId(), owner.getToken());

        // then
        assertEquals(board, foundBoard);
    }

    //#316
    @Test
    public void getSingleTravelBoardById_member_returnsBoard() {
        // given
        board.getMembers().add(member);

        Mockito.when(userRepository.findByToken(member.getToken())).thenReturn(member);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        TravelBoard foundBoard = travelBoardService.getSingleTravelBoardById(board.getId(), member.getToken());

        // then
        assertEquals(board, foundBoard);
    }

    //#316
    @Test
    public void getSingleTravelBoardById_nonMember_throwsUnauthorized() {
        // given
        User stranger = createTestUser(3L, "stranger", "stranger123", "strangertoken123");

        Mockito.when(userRepository.findByToken(stranger.getToken())).thenReturn(stranger);
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.getSingleTravelBoardById(board.getId(), stranger.getToken())
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

	// ================ TESTS GET INVITE CODE ================    
    //#139
    @Test
    public void getInviteCode_validBoard_returnsInviteCode() {
        // given
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));

        // when
        String inviteCode = travelBoardService.getInviteCode(board.getId());

        // then
        assertEquals("CODE123", inviteCode);
    }

    //#139
    @Test
    public void getInviteCode_boardNotFound_throwsNotFound() {
        // given
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.getInviteCode(board.getId())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

	// ================ TESTS JOIN TRAVEL BOARD BY INVITE CODE ================
    //#153
    @Test
    public void joinTravelBoardByInviteCode_validCode_success() {
        // given
        Mockito.when(userRepository.findByToken(member.getToken())).thenReturn(member);
        Mockito.when(travelBoardRepository.findByInviteCode(board.getInviteCode())).thenReturn(board);

        // when
        travelBoardService.joinTravelBoardByInviteCode(member.getToken(), board.getInviteCode());

        // then
        assertTrue(board.getMembers().contains(member));
    }
	
    //#154
    @Test
    public void joinTravelBoardByInviteCode_invalidCode_throwsNotFound() {
        // given
        Mockito.when(userRepository.findByToken(member.getToken())).thenReturn(member);
        Mockito.when(travelBoardRepository.findByInviteCode("INVALID123")).thenReturn(null);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.joinTravelBoardByInviteCode(member.getToken(), "INVALID123")
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //#153
    @Test
    public void joinTravelBoardByInviteCode_alreadyMember_throwsConflict() {
        // given
        board.getMembers().add(member);

        Mockito.when(userRepository.findByToken(member.getToken())).thenReturn(member);
        Mockito.when(travelBoardRepository.findByInviteCode(board.getInviteCode())).thenReturn(board);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.joinTravelBoardByInviteCode(member.getToken(), board.getInviteCode())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    //#153
    @Test
    public void joinTravelBoardByInviteCode_owner_throwsConflict() {
        // given
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(travelBoardRepository.findByInviteCode(board.getInviteCode())).thenReturn(board);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> travelBoardService.joinTravelBoardByInviteCode(owner.getToken(), board.getInviteCode())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }


	// ================ helper ================
    private User createTestUser(Long id, String name, String username, String token) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setUsername(username);
        user.setPassword("pw");
        user.setEmail(username + "@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken(token);
        return user;
    }

    private TravelBoard createTestBoard(Long id, String name, User owner, String inviteCode, PrivacyLevel privacy) {
        TravelBoard board = new TravelBoard();
        board.setId(id);
        board.setName(name);
        board.setOwner(owner);
        board.setInviteCode(inviteCode);
        board.setPrivacy(privacy);
        board.setDateCreated(LocalDate.now());
        return board;
    }
}
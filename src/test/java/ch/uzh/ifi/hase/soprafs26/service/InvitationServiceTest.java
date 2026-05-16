package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Test class for the InvitationService.
 *  - mocks with Mockito (fakes the database responses - no actual database operations are performed)
 * 	- tests the business logic of the InvitationService in isolation
 * 
 * Detects:
 *  - logic errors in the InvitationService
 * 
 * For tests that also involve the database, @see InvitationServiceIntegrationTest.
 */

public class InvitationServiceTest {

	@Mock
	private InvitationRepository invitationRepository;

    @Mock
    private UserRepository userRepository;
        
    @Mock
    private TravelBoardRepository travelBoardRepository;

	@InjectMocks
	private InvitationService invitationService;

    private User owner;
    private User receiver;
    private TravelBoard board;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

        owner = createTestUser(1L, "owner", "owner123", "ownertoken123");
        receiver = createTestUser(2L, "receiver", "receiver123", "receivertoken123");
        board = createTestBoard(10L, "Test Board", owner, "CODE123", PrivacyLevel.PUBLIC);

		Mockito.when(invitationRepository.save(Mockito.any()))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	// ================ TESTS CREATE INVITATION ================
    //#180
    @Test
    public void createInvitation_validInput_success() {
        // given
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        Mockito.when(invitationRepository.findByBoardAndReceiverAndStatus(board, receiver, InviteStatus.PENDING)).thenReturn(null);

        // when
        Invitation createdInvitation = invitationService.createInvitation(board.getId(), owner.getToken(), receiver.getId());

        // then
        assertEquals(InviteStatus.PENDING, createdInvitation.getStatus());
        assertEquals(board, createdInvitation.getBoard());
        assertEquals(owner, createdInvitation.getSender());
        assertEquals(receiver, createdInvitation.getReceiver());
    }

    //#180
    @Test
    public void createInvitation_boardNotFound_throwsNotFound() {
        // given
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.createInvitation(board.getId(), owner.getToken(), receiver.getId())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //#180
    @Test
    public void createInvitation_invalidToken_throwsUnauthorized() {
        // given
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        Mockito.when(userRepository.findByToken("invalidtoken")).thenReturn(null);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.createInvitation(board.getId(), "invalidtoken", receiver.getId())
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    //#182
    @Test
    public void createInvitation_nonOwner_throwsUnauthorized() {
        // given
        User nonOwner = createTestUser(3L, "nonOwner", "nonOwner123", "nonownertoken123");

        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        Mockito.when(userRepository.findByToken(nonOwner.getToken())).thenReturn(nonOwner);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.createInvitation(board.getId(), nonOwner.getToken(), receiver.getId())
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    //#180
    @Test
    public void createInvitation_nullReceiverId_throwsBadRequest() {
        // given
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.createInvitation(board.getId(), owner.getToken(), null)
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#180    
    @Test
    public void createInvitation_receiverNotFound_throwsNotFound() {
        // given
        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.createInvitation(board.getId(), owner.getToken(), receiver.getId())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //#263
    @Test
    public void createInvitation_duplicatePendingInvitation_throwsConflict() {
        // given
        Invitation existingInvitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);

        Mockito.when(travelBoardRepository.findById(board.getId())).thenReturn(Optional.of(board));
        Mockito.when(userRepository.findByToken(owner.getToken())).thenReturn(owner);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        Mockito.when(invitationRepository.findByBoardAndReceiverAndStatus(board, receiver, InviteStatus.PENDING)).thenReturn(existingInvitation);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.createInvitation(board.getId(), owner.getToken(), receiver.getId())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }    
	

	// ================ TESTS ACCEPT INVITATION ================
    //#155
    @Test
    public void acceptInvitation_validPendingInvitation_addsUserAsBoardMember() {
        // given
        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);
        invitation.setId(100L);

        Mockito.when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

        // when
        invitationService.acceptInvitation(invitation.getId(), receiver.getToken());

        // then
        assertTrue(board.getMembers().contains(receiver));
        assertEquals(InviteStatus.ACCEPTED, invitation.getStatus());
    }

    //#155
    @Test
    public void acceptInvitation_notFound_throwsNotFound() {
        // given
        Long invitationId = 100L;

        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.acceptInvitation(invitationId, receiver.getToken())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //#155
    @Test
    public void acceptInvitation_wrongUser_throwsUnauthorized() {
        // given
        User wrongUser = createTestUser(3L, "wrongUser", "wrongUser123", "wrongtoken123");

        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);
        invitation.setId(100L);

        Mockito.when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        Mockito.when(userRepository.findByToken(wrongUser.getToken())).thenReturn(wrongUser);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.acceptInvitation(invitation.getId(), wrongUser.getToken())
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    //#155
    @Test
    public void acceptInvitation_alreadyAnswered_throwsConflict() {
        // given
        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.ACCEPTED);
        invitation.setId(100L);

        Mockito.when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.acceptInvitation(invitation.getId(), receiver.getToken())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    //#155
    @Test
    public void acceptInvitation_alreadyBoardMember_throwsConflict() {
        // given
        board.getMembers().add(receiver);

        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);
        invitation.setId(100L);

        Mockito.when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.acceptInvitation(invitation.getId(), receiver.getToken())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

	// ================ TESTS DECLINE INVITATION ================
    //#156
    @Test
    public void declineInvitation_validPendingInvitation_setsStatusDeclined() {
        // given
        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);
        invitation.setId(100L);

        Mockito.when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

        // when
        invitationService.declineInvitation(invitation.getId(), receiver.getToken());

        // then
        assertEquals(InviteStatus.DECLINED, invitation.getStatus());
        assertFalse(board.getMembers().contains(receiver));
    }

    //#156
    @Test
    public void declineInvitation_notFound_throwsNotFound() {
        // given
        Long invitationId = 100L;

        Mockito.when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.declineInvitation(invitationId, receiver.getToken())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //#156
    @Test
    public void declineInvitation_wrongUser_throwsUnauthorized() {
        // given
        User wrongUser = createTestUser(3L, "wrongUser", "wrongUser123", "wrongtoken123");

        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);
        invitation.setId(100L);

        Mockito.when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        Mockito.when(userRepository.findByToken(wrongUser.getToken())).thenReturn(wrongUser);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.declineInvitation(invitation.getId(), wrongUser.getToken())
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    //#156
    @Test
    public void declineInvitation_alreadyAnswered_throwsConflict() {
        // given
        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.ACCEPTED);
        invitation.setId(100L);

        Mockito.when(invitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));
        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.declineInvitation(invitation.getId(), receiver.getToken())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

	// ================ TESTS GET INVITATIONS ================
    //#262
    @Test
    public void getPendingInvitations_validToken_returnsPendingInvitations() {
        // given
        Invitation pendingInvitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);

        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);
        Mockito.when(invitationRepository.findByReceiverAndStatus(receiver, InviteStatus.PENDING)).thenReturn(List.of(pendingInvitation));

        // when
        List<Invitation> result = invitationService.getPendingInvitations(receiver.getToken());

        // then
        assertEquals(1, result.size());
        assertEquals(pendingInvitation, result.get(0));
    }
	
    //#262
    @Test
    public void getPendingInvitations_invalidToken_throwsUnauthorized() {
        // given
        Mockito.when(userRepository.findByToken("invalidtoken")).thenReturn(null);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> invitationService.getPendingInvitations("invalidtoken")
        );

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
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

    private Invitation createTestInvitation(TravelBoard board, User sender, User receiver, InviteStatus status) {
        Invitation invitation = new Invitation();
        invitation.setBoard(board);
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        invitation.setStatus(status);
        return invitation;
    }
}
package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * Test class for the UserResource REST resource.
 *
 * @see TravelBoardService
 */
@WebAppConfiguration
@SpringBootTest
public class InvitationServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

    
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
	private InvitationService invitationService;

	@BeforeEach
	public void setup() {
        invitationRepository.deleteAll();
        friendRequestRepository.deleteAll();
		travelBoardRepository.deleteAll();
        userRepository.deleteAll();
	}
    
    //#155,#181
    @Test
    public void acceptInvitation_validPendingInvitation_addsUserAsBoardMember() {
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        TravelBoard board = createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC); 
        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);

        // accept invitation
        invitationService.acceptInvitation(invitation.getId(), receiver.getToken());

        // assert
        Invitation updatedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();

        assertTrue(travelBoardRepository.findByMembersId(receiver.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(board.getId())));
        assertEquals(InviteStatus.ACCEPTED, updatedInvitation.getStatus());
    }

    //#156,#183
    @Test
    public void declineInvitation_validPendingInvitation_doesNotAddUserAsBoardMember() {
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        TravelBoard board = createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC); 
        Invitation invitation = createTestInvitation(board, owner, receiver, InviteStatus.PENDING);

        // decline invitation
        invitationService.declineInvitation(invitation.getId(), receiver.getToken());

        // assert
        Invitation updatedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();

        assertFalse(travelBoardRepository.findByMembersId(receiver.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(board.getId())));
        assertEquals(InviteStatus.DECLINED, updatedInvitation.getStatus());
    }

    //#180
    @Test
    public void createInvitation_validInput_storesInvitationInDatabase() {
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        TravelBoard board = createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC); 
    
        // create invitation
        Invitation createdInvitation = invitationService.createInvitation(board.getId(), owner.getToken(), receiver.getId());
        
        // assert
        assertNotNull(createdInvitation.getId());
        assertEquals(InviteStatus.PENDING, createdInvitation.getStatus());
        assertEquals(board.getId(), createdInvitation.getBoard().getId());
        assertEquals(owner.getId(), createdInvitation.getSender().getId());
        assertEquals(receiver.getId(), createdInvitation.getReceiver().getId());
    }

    //#182
    @Test
    public void createInvitation_nonOwner_throwsUnauthorized() {
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        TravelBoard board = createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC); 

        // assert: non-owner tries to create invitation → should fail
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> invitationService.createInvitation(board.getId(), sender.getToken(), receiver.getId())
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
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

    private Invitation createTestInvitation(TravelBoard board, User sender, User receiver, InviteStatus status) {
        Invitation invitation = new Invitation();
        invitation.setBoard(board);
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        invitation.setStatus(status);
        return invitationRepository.save(invitation);
    }
}
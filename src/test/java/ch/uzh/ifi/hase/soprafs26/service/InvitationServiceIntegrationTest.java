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

	@Autowired
	private InvitationService invitationService;

	@BeforeEach
	public void setup() {
        invitationRepository.deleteAll();
		travelBoardRepository.deleteAll();
        userRepository.deleteAll();
	}
    
    //#155
    @Test
    public void acceptInvitation_validPendingInvitation_addsUserAsBoardMember() {
        // create user: sender/owner
        User owner = new User();
        owner.setName("owner");
        owner.setUsername("owner123");
        owner.setPassword("pw");
        owner.setEmail("owner123@test.ch");
        owner.setCreationDate(LocalDate.now());
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("ownertoken");
        owner = userRepository.save(owner);

        // create user: receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver123");
        receiver.setPassword("pw");
        receiver.setEmail("receiver123@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken");
        receiver = userRepository.save(receiver);

        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Paris Trip");
        board.setOwner(owner);
        board.setInviteCode("INV123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
        board = travelBoardRepository.save(board);
        Long boardId = board.getId();

        // create invitation
        Invitation invitation = new Invitation();
        invitation.setBoard(board);
        invitation.setSender(owner);
        invitation.setReceiver(receiver);
        invitation.setStatus(InviteStatus.PENDING);
        invitation = invitationRepository.save(invitation);

        // accept invitation
        invitationService.acceptInvitation(invitation.getId(), receiver.getToken());

        // assert
        Invitation updatedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();

        assertTrue(travelBoardRepository.findByMembersId(receiver.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(boardId)));
        assertEquals(InviteStatus.ACCEPTED, updatedInvitation.getStatus());
    }

    //#156
    @Test
    public void declineInvitation_validPendingInvitation_doesNotAddUserAsBoardMember() {
        // create user: sender/owner
        User owner = new User();
        owner.setName("owner");
        owner.setUsername("owner123");
        owner.setPassword("pw");
        owner.setEmail("owner123@test.ch");
        owner.setCreationDate(LocalDate.now());
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("ownertoken");
        owner = userRepository.save(owner);

        // create user: receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver123");
        receiver.setPassword("pw");
        receiver.setEmail("receiver123@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken");
        receiver = userRepository.save(receiver);

        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Paris Trip");
        board.setOwner(owner);
        board.setInviteCode("INV123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
        board = travelBoardRepository.save(board);
        Long boardId = board.getId();

        // create invitation
        Invitation invitation = new Invitation();
        invitation.setBoard(board);
        invitation.setSender(owner);
        invitation.setReceiver(receiver);
        invitation.setStatus(InviteStatus.PENDING);
        invitation = invitationRepository.save(invitation);

        // decline invitation
        invitationService.declineInvitation(invitation.getId(), receiver.getToken());

        // assert
        Invitation updatedInvitation = invitationRepository.findById(invitation.getId()).orElseThrow();

        assertFalse(travelBoardRepository.findByMembersId(receiver.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(boardId)));
        assertEquals(InviteStatus.DECLINED, updatedInvitation.getStatus());
    }

    //#180
    @Test
    public void createInvitation_validInput_storesInvitationInDatabase() {
        // create user: sender/owner
        User owner = new User();
        owner.setName("owner");
        owner.setUsername("owner123");
        owner.setPassword("pw");
        owner.setEmail("owner123@test.ch");
        owner.setCreationDate(LocalDate.now());
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("ownertoken");
        owner = userRepository.save(owner);

        // create user: receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver123");
        receiver.setPassword("pw");
        receiver.setEmail("receiver123@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken");
        receiver = userRepository.save(receiver);

        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Paris Trip");
        board.setOwner(owner);
        board.setInviteCode("CODE123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
        board = travelBoardRepository.save(board);
        Long boardId = board.getId();
    
        // create invitation
        Invitation createdInvitation = invitationService.createInvitation(boardId, owner.getToken(), receiver.getId());
        
        // assert
        assertNotNull(createdInvitation.getId());
        assertEquals(InviteStatus.PENDING, createdInvitation.getStatus());
        assertEquals(boardId, createdInvitation.getBoard().getId());
        assertEquals(owner.getId(), createdInvitation.getSender().getId());
        assertEquals(receiver.getId(), createdInvitation.getReceiver().getId());
    }

    //#182
    @Test
    public void createInvitation_nonOwner_throwsUnauthorized() {
        // create owner
        User owner = new User();
        owner.setName("owner");
        owner.setUsername("owner123");
        owner.setPassword("pw");
        owner.setEmail("owner123@test.ch");
        owner.setCreationDate(LocalDate.now());
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("ownertoken");
        owner = userRepository.save(owner);

        // create non-owner (sender)
        User sender = new User();
        sender.setName("sender");
        sender.setUsername("sender123");
        sender.setPassword("pw");
        sender.setEmail("sender123@test.ch");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sendertoken123");
        sender = userRepository.save(sender);
        String senderToken = sender.getToken();

        // create receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver182");
        receiver.setPassword("pw");
        receiver.setEmail("receiver182@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receiver-token-182");
        receiver = userRepository.save(receiver);
        Long receiverId = receiver.getId();

        // create board with owner
        TravelBoard board = new TravelBoard();
        board.setName("Owner Only Board");
        board.setOwner(owner);
        board.setInviteCode("INV182");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
        board = travelBoardRepository.save(board);
        Long boardId = board.getId();

        // assert: non-owner tries to create invitation → should fail
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> invitationService.createInvitation(boardId, senderToken, receiverId)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

}
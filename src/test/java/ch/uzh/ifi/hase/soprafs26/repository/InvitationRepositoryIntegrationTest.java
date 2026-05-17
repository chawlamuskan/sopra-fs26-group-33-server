package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class InvitationRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private InvitationRepository invitationRepository;

	@Test
	public void findByReceiverAndStatus_pendingInvitations_success() {
		// given
		User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        TravelBoard board = createTestBoard("Test Board", sender, "CODE123", PrivacyLevel.PUBLIC);
        
        Invitation pendingInvitation = createTestInvitation(board, sender, receiver, InviteStatus.PENDING);

        entityManager.flush();

		// when
		List<Invitation> foundInvitations = invitationRepository.findByReceiverAndStatus(receiver, InviteStatus.PENDING);

		// then
        assertEquals(1, foundInvitations.size());
        assertEquals(pendingInvitation.getId(), foundInvitations.get(0).getId());
        assertEquals(sender.getId(), foundInvitations.get(0).getSender().getId());
        assertEquals(receiver.getId(), foundInvitations.get(0).getReceiver().getId());
        assertEquals(board.getId(), foundInvitations.get(0).getBoard().getId());
        assertEquals(InviteStatus.PENDING, foundInvitations.get(0).getStatus());
	}

    @Test
    public void findByReceiverAndStatus_onlyPendingInvitationsReturned() {
        // given
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        User pendingSender = createTestUser("pendingSender", "pendingSender123", "pendingtoken123");
        User acceptedSender = createTestUser("acceptedSender", "acceptedSender123", "acceptedtoken123");
        User declinedSender = createTestUser("declinedSender", "declinedSender123", "declinedtoken123");
        TravelBoard board = createTestBoard("Test Board", pendingSender, "CODE123", PrivacyLevel.PUBLIC);

        Invitation pendingInvitation = createTestInvitation(board, pendingSender, receiver, InviteStatus.PENDING);
        Invitation acceptedInvitation = createTestInvitation(board, acceptedSender, receiver, InviteStatus.ACCEPTED);
        Invitation declinedInvitation = createTestInvitation(board, declinedSender, receiver, InviteStatus.DECLINED);

        entityManager.flush();

        // when
        List<Invitation> foundInvitations = invitationRepository.findByReceiverAndStatus(receiver, InviteStatus.PENDING);

        // then
        assertEquals(1, foundInvitations.size());
        assertTrue(foundInvitations.stream().anyMatch(invitation -> invitation.getId().equals(pendingInvitation.getId())));
        assertFalse(foundInvitations.stream().anyMatch(invitation -> invitation.getId().equals(acceptedInvitation.getId())));
        assertFalse(foundInvitations.stream().anyMatch(invitation -> invitation.getId().equals(declinedInvitation.getId())));
    }

    @Test
    public void findByBoardAndReceiverAndStatus_existingPendingInvitation_success() {
        // given
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        TravelBoard board = createTestBoard("Test Board", sender, "CODE123", PrivacyLevel.PUBLIC);

        Invitation pendingInvitation = createTestInvitation(board, sender, receiver, InviteStatus.PENDING);

        entityManager.flush();

        // when
        Invitation foundInvitation = invitationRepository.findByBoardAndReceiverAndStatus(board, receiver, InviteStatus.PENDING);

        // then
        assertNotNull(foundInvitation);
        assertEquals(pendingInvitation.getId(), foundInvitation.getId());
        assertEquals(sender.getId(), foundInvitation.getSender().getId());
        assertEquals(receiver.getId(), foundInvitation.getReceiver().getId());
        assertEquals(board.getId(), foundInvitation.getBoard().getId());
        assertEquals(InviteStatus.PENDING, foundInvitation.getStatus());
    }

    @Test
    public void findByBoardAndReceiverAndStatus_wrongStatus_returnsNull() {
        // given
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        TravelBoard board = createTestBoard("Test Board", sender, "CODE123", PrivacyLevel.PUBLIC);

        createTestInvitation(board, sender, receiver, InviteStatus.ACCEPTED);

        entityManager.flush();

        // when
        Invitation foundInvitation = invitationRepository.findByBoardAndReceiverAndStatus(board, receiver, InviteStatus.PENDING);

        // then
        assertNull(foundInvitation);
    }

    private User createTestUser(String name, String username, String token) {
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(username + "@test.ch");
        user.setPassword("Password123!");
        user.setToken(token);
        user.setStatus(UserStatus.ONLINE);
        user.setCreationDate(LocalDate.now());

        entityManager.persist(user);
        return user;
    }

    private TravelBoard createTestBoard(String name, User owner, String inviteCode, PrivacyLevel privacy) {
        TravelBoard board = new TravelBoard();
        board.setName(name);
        board.setOwner(owner);
        board.setInviteCode(inviteCode);
        board.setPrivacy(privacy);
        board.setDateCreated(LocalDate.now());

        entityManager.persist(board);
        return board;
    }

    private Invitation createTestInvitation(TravelBoard board, User sender, User receiver, InviteStatus status) {
        Invitation invitation = new Invitation();
        invitation.setBoard(board);
        invitation.setSender(sender);
        invitation.setReceiver(receiver);
        invitation.setStatus(status);

        entityManager.persist(invitation);
        return invitation;
    }
}
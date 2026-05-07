package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class FriendRequestRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private FriendRequestRepository friendRequestRepository;

	@Test
	public void findByReceiverAndStatus_pendingRequests_success() {
		// given
		User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");

        FriendRequest pendingRequest = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);

        entityManager.flush();

		// when
		List<FriendRequest> foundRequests = friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequestStatus.PENDING);

		// then
        assertEquals(1, foundRequests.size());
        assertEquals(pendingRequest.getId(), foundRequests.get(0).getId());
        assertEquals(sender.getId(), foundRequests.get(0).getSender().getId());
        assertEquals(receiver.getId(), foundRequests.get(0).getReceiver().getId());
        assertEquals(FriendRequestStatus.PENDING, foundRequests.get(0).getStatus());
	}

    @Test
    public void findByReceiverAndStatus_onlyPendingRequestsReturned() {
        // given
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        User pendingSender = createTestUser("pendingSender", "pendingSender123", "pendingtoken123");
        User acceptedSender = createTestUser("acceptedSender", "acceptedSender123", "acceptedtoken123");
        User declinedSender = createTestUser("declinedSender", "declinedSender123", "declinedtoken123");

        FriendRequest pendingRequest = createTestFriendRequest(pendingSender, receiver, FriendRequestStatus.PENDING);
        FriendRequest acceptedRequest = createTestFriendRequest(acceptedSender, receiver, FriendRequestStatus.ACCEPTED);
        FriendRequest declinedRequest = createTestFriendRequest(declinedSender, receiver, FriendRequestStatus.DECLINED);

        entityManager.flush();

        // when
        List<FriendRequest> foundRequests = friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequestStatus.PENDING);

        // then
        assertEquals(1, foundRequests.size());
        assertTrue(foundRequests.stream().anyMatch(request -> request.getId().equals(pendingRequest.getId())));
        assertFalse(foundRequests.stream().anyMatch(request -> request.getId().equals(acceptedRequest.getId())));
        assertFalse(foundRequests.stream().anyMatch(request -> request.getId().equals(declinedRequest.getId())));
    }

    @Test
    public void findBySenderAndReceiverAndStatus_existingPendingRequest_success() {
        // given
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");

        FriendRequest pendingRequest = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);

        entityManager.flush();

        // when
        FriendRequest foundRequest = friendRequestRepository.findBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING);

        // then
        assertNotNull(foundRequest);
        assertEquals(pendingRequest.getId(), foundRequest.getId());
        assertEquals(sender.getId(), foundRequest.getSender().getId());
        assertEquals(receiver.getId(), foundRequest.getReceiver().getId());
        assertEquals(FriendRequestStatus.PENDING, foundRequest.getStatus());
    }

    @Test
    public void findBySenderAndReceiverAndStatus_wrongStatus_returnsNull() {
        // given
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");

        createTestFriendRequest(sender, receiver, FriendRequestStatus.ACCEPTED);

        entityManager.flush();

        // when
        FriendRequest foundRequest = friendRequestRepository.findBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING);

        // then
        assertNull(foundRequest);
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

    private FriendRequest createTestFriendRequest(User sender, User receiver, FriendRequestStatus status) {
        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(sender);
        friendRequest.setReceiver(receiver);
        friendRequest.setStatus(status);

        entityManager.persist(friendRequest);
        return friendRequest;
    }
}
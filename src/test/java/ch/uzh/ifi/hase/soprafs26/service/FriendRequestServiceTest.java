package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Test class for the FriendRequestService.
 *  - mocks with Mockito (fakes the database responses - no actual database operations are performed)
 * 	- tests the business logic of the FriendRequestService in isolation
 * 
 * Detects:
 *  - logic errors in the FriendRequestService
 * 
 * For tests that also involve the database, @see FriendRequestServiceIntegrationTest.
 */

public class FriendRequestServiceTest {

	@Mock
	private FriendRequestRepository friendRequestRepository;

    @Mock
    private UserRepository userRepository;

	@InjectMocks
	private FriendRequestService friendRequestService;

    private User sender;
    private User receiver;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

        sender = createTestUser(1L, "sender", "sender123", "sendertoken123");
        receiver = createTestUser(2L, "receiver", "receiver123", "receivertoken123");

		Mockito.when(friendRequestRepository.save(Mockito.any()))
				.thenAnswer(invocation -> invocation.getArgument(0));
	}

	// ================ TESTS SEND FRIEND REQUEST ================
    //#223
    @Test
    public void sendFriendRequest_validInput_success() {
        // given
        Mockito.when(userRepository.findByToken(sender.getToken())).thenReturn(sender);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        Mockito.when(friendRequestRepository.findBySenderAndReceiverAndStatus(
                sender, receiver, FriendRequestStatus.PENDING)).thenReturn(null);
        Mockito.when(friendRequestRepository.findBySenderAndReceiverAndStatus(
                receiver, sender, FriendRequestStatus.PENDING)).thenReturn(null);

        // when
        FriendRequest createdRequest = friendRequestService.sendFriendRequest(
                sender.getToken(), receiver.getId());

        // then
        assertEquals(FriendRequestStatus.PENDING, createdRequest.getStatus());
        assertEquals(sender, createdRequest.getSender());
        assertEquals(receiver, createdRequest.getReceiver());
    }
	
    //#403
    @Test
    public void sendFriendRequest_toYourself_throwsBadRequest() {
        // given
        Mockito.when(userRepository.findByToken(sender.getToken())).thenReturn(sender);
        Mockito.when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(sender.getToken(), sender.getId())
        );

        // then
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    //#403
    @Test
    public void sendFriendRequest_toExistingFriend_throwsConflict() {
        // given
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        Mockito.when(userRepository.findByToken(sender.getToken())).thenReturn(sender);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(sender.getToken(), receiver.getId())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    //#227
    @Test
    public void sendFriendRequest_duplicatePendingRequest_throwsConflict() {
        // given
        FriendRequest existingRequest = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);

        Mockito.when(userRepository.findByToken(sender.getToken())).thenReturn(sender);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        Mockito.when(friendRequestRepository.findBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING)).thenReturn(existingRequest);

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(sender.getToken(), receiver.getId())
        );

        // then
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
	
	// ================ TESTS GET FRIEND REQUEST ================
    //#404
    @Test
    public void getPendingFriendRequests_validToken_returnsPendingRequests() {
        // given
        FriendRequest pendingRequest = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);

        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);
        Mockito.when(friendRequestRepository.findByReceiverAndStatus(receiver, FriendRequestStatus.PENDING))
                .thenReturn(List.of(pendingRequest));

        // when
        List<FriendRequest> result = friendRequestService.getPendingFriendRequests(receiver.getToken());

        // then
        assertEquals(1, result.size());
        assertEquals(pendingRequest, result.get(0));
    }

	// ================ TESTS ACCEPT FRIEND REQUEST ================
	//#405
    @Test
    public void acceptFriendRequest_validPendingRequest_createsFriendship() {
        // given
        FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);
        request.setId(100L);

        Mockito.when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

        // when
        friendRequestService.acceptFriendRequest(request.getId(), receiver.getToken());

        // then
        assertEquals(FriendRequestStatus.ACCEPTED, request.getStatus());
        assertTrue(sender.getFriends().contains(receiver));
        assertTrue(receiver.getFriends().contains(sender));
    }

	//#405
	@Test
	public void acceptFriendRequest_notFound_throwsNotFound() {
	    // given
	    Long requestId = 100L;

	    Mockito.when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

	    // when
	    ResponseStatusException exception = assertThrows(
	            ResponseStatusException.class,
	            () -> friendRequestService.acceptFriendRequest(requestId, receiver.getToken())
	    );

	    // then
	    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	//#405
	@Test
	public void acceptFriendRequest_wrongUser_throwsUnauthorized() {
	    // given
	    User wrongUser = createTestUser(3L, "wrongUser", "wrongUser123", "wrongtoken123");

	    FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);
	    request.setId(100L);

	    Mockito.when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
	    Mockito.when(userRepository.findByToken(wrongUser.getToken())).thenReturn(wrongUser);

	    // when
	    ResponseStatusException exception = assertThrows(
	            ResponseStatusException.class,
	            () -> friendRequestService.acceptFriendRequest(request.getId(), wrongUser.getToken())
	    );

	    // then
	    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	//#405
	@Test
	public void acceptFriendRequest_alreadyAnswered_throwsConflict() {
	    // given
	    FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.ACCEPTED);
	    request.setId(100L);

	    Mockito.when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
	    Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

	    // when
	    ResponseStatusException exception = assertThrows(
	            ResponseStatusException.class,
	            () -> friendRequestService.acceptFriendRequest(request.getId(), receiver.getToken())
	    );

	    // then
	    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	// ================ TESTS DECLINE FRIEND REQUEST ================
	//#406
    @Test
    public void declineFriendRequest_validPendingRequest_setsStatusDeclined() {
        // given
        FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);
        request.setId(100L);

        Mockito.when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
        Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

        // when
        friendRequestService.declineFriendRequest(request.getId(), receiver.getToken());

        // then
        assertEquals(FriendRequestStatus.DECLINED, request.getStatus());
        assertFalse(sender.getFriends().contains(receiver));
        assertFalse(receiver.getFriends().contains(sender));
    }

	//#406
	@Test
	public void declineFriendRequest_notFound_throwsNotFound() {
	    // given
	    Long requestId = 100L;

	    Mockito.when(friendRequestRepository.findById(requestId)).thenReturn(Optional.empty());

	    // when
	    ResponseStatusException exception = assertThrows(
	            ResponseStatusException.class,
	            () -> friendRequestService.declineFriendRequest(requestId, receiver.getToken())
	    );

	    // then
	    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	//#406
	@Test
	public void declineFriendRequest_wrongUser_throwsUnauthorized() {
	    // given
	    User wrongUser = createTestUser(3L, "wrongUser", "wrongUser123", "wrongtoken123");

	    FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);
	    request.setId(100L);

	    Mockito.when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
	    Mockito.when(userRepository.findByToken(wrongUser.getToken())).thenReturn(wrongUser);

	    // when
	    ResponseStatusException exception = assertThrows(
	            ResponseStatusException.class,
	            () -> friendRequestService.declineFriendRequest(request.getId(), wrongUser.getToken())
	    );

	    // then
	    assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
	}

	//#406
	@Test
	public void declineFriendRequest_alreadyAnswered_throwsConflict() {
	    // given
	    FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.ACCEPTED);
	    request.setId(100L);

	    Mockito.when(friendRequestRepository.findById(request.getId())).thenReturn(Optional.of(request));
	    Mockito.when(userRepository.findByToken(receiver.getToken())).thenReturn(receiver);

	    // when
	    ResponseStatusException exception = assertThrows(
	            ResponseStatusException.class,
	            () -> friendRequestService.declineFriendRequest(request.getId(), receiver.getToken())
	    );

	    // then
	    assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
	}

	// ================ TESTS REMOVE FRIEND ================
    //#226
    @Test
    public void removeFriend_existingFriend_removesFriendship() {
        // given
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        Mockito.when(userRepository.findByToken(sender.getToken())).thenReturn(sender);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

        // when
        friendRequestService.removeFriend(sender.getToken(), receiver.getId());

        // then
        assertFalse(sender.getFriends().contains(receiver));
        assertFalse(receiver.getFriends().contains(sender));
    }
	
    //#226
    @Test
    public void removeFriend_nonFriend_throwsNotFound() {
        // given
        Mockito.when(userRepository.findByToken(sender.getToken())).thenReturn(sender);
        Mockito.when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));

        // when
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.removeFriend(sender.getToken(), receiver.getId())
        );

        // then
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

	// ================ TESTS GET FRIENDS ================
    //#402
    @Test
    public void getFriends_validToken_returnsFriends() {
        // given
        sender.getFriends().add(receiver);

        Mockito.when(userRepository.findByToken(sender.getToken())).thenReturn(sender);

        // when
        List<User> friends = friendRequestService.getFriends(sender.getToken());

        // then
        assertEquals(1, friends.size());
        assertEquals(receiver, friends.get(0));
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

    private FriendRequest createTestFriendRequest(User sender, User receiver, FriendRequestStatus status) {
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(status);
        return request;
    }
}
package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Test class for the UserResource REST resource.
 *
 * @see FriendRequestService
 */
@WebAppConfiguration
@SpringBootTest
public class FriendRequestServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;
    
	@Qualifier("travelBoardRepository")
	@Autowired
	private TravelBoardRepository travelBoardRepository;

    @Qualifier("friendRequestRepository")
    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Qualifier("invitationRepository")
    @Autowired
    private InvitationRepository invitationRepository;

    @Qualifier("preferencesRepository")
    @Autowired
    private PreferencesRepository preferencesRepository;

	@Autowired
	private FriendRequestService friendRequestService;

	@BeforeEach
	public void setup() {
        invitationRepository.deleteAll();
        invitationRepository.flush();
        friendRequestRepository.deleteAll();
        friendRequestRepository.flush();
        travelBoardRepository.deleteAll();
        travelBoardRepository.flush();
        preferencesRepository.deleteAll();
        preferencesRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
	}
    

    //#227
    @Test
    public void sendFriendRequest_duplicatePendingRequest_throwsConflict() {
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");


        //create/send friend request
        friendRequestService.sendFriendRequest(sender.getToken(), receiver.getId());

        //assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(sender.getToken(), receiver.getId())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    //#226
    @Test
    @Transactional
    public void removeFriend_existingFriendship_updatesFriendListCorrectly() {
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");

        makeFriends(sender, receiver);

        // remove friend
        friendRequestService.removeFriend(sender.getToken(), receiver.getId());

        // assert
        User updatedSender = userRepository.findById(sender.getId()).orElseThrow();
        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();

        assertFalse(updatedSender.getFriends().stream().anyMatch(user -> user.getId().equals(receiver.getId())));
        assertFalse(updatedReceiver.getFriends().stream().anyMatch(user -> user.getId().equals(sender.getId())));
    }

    //#226
    @Test
    public void removeFriend_nonFriend_throwsNotFound() {
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");

        //assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.removeFriend(sender.getToken(), receiver.getId())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //#223
    @Test
    public void sendFriendRequest_validInput_storesPendingRequestInDatabase() {
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");

        //create friendrequest
        FriendRequest createdRequest = friendRequestService.sendFriendRequest(sender.getToken(), receiver.getId());

        //assert
        assertNotNull(createdRequest.getId());
        assertEquals(FriendRequestStatus.PENDING, createdRequest.getStatus());
        assertEquals(sender.getId(), createdRequest.getSender().getId());
        assertEquals(receiver.getId(), createdRequest.getReceiver().getId());

        //search for created
        FriendRequest storedRequest = friendRequestRepository.findById(createdRequest.getId()).orElseThrow();

        //assert
        assertEquals(FriendRequestStatus.PENDING, storedRequest.getStatus());
        assertEquals(sender.getId(), storedRequest.getSender().getId());
        assertEquals(receiver.getId(), storedRequest.getReceiver().getId());
    }

    //#402
    @Test
    @Transactional
    public void getFriendList_onlyAcceptedFriends_returnsOnlyAcceptedFriends() {
        User user = createTestUser("user", "user123", "token123");
        User acceptedFriend = createTestUser("acceptedFriend", "acceptedFriend123", "acceptedFriendtoken123");
        User pendingUser = createTestUser("pendingUser", "pendingUser123", "pendingUsertoken123");

        makeFriends(user, acceptedFriend);

        // create pending friend request that should not appear in friend list
        createTestFriendRequest(pendingUser, user, FriendRequestStatus.PENDING);

        // fetch friend list
        List<User> friends = friendRequestService.getFriends(user.getToken());

        // assert
        assertTrue(friends.stream().anyMatch(friend -> friend.getId().equals(acceptedFriend.getId())));
        assertFalse(friends.stream().anyMatch(friend -> friend.getId().equals(user.getId())));
        assertFalse(friends.stream().anyMatch(friend -> friend.getId().equals(pendingUser.getId())));
    }

    //#403 friend request to yourself 
    @Test
    public void sendFriendRequest_toYourself_throwsBadRequest() {
        User user = createTestUser("user", "user123", "token123");

        // assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(user.getToken(), user.getId())
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(0, friendRequestRepository.findAll().size());
    }

    //#403 friend request to existing friend 
    @Test
    @Transactional
    public void sendFriendRequest_toExistingFriend_throwsConflict() {
        User user = createTestUser("user", "user123", "token123");
        User friend = createTestUser("friend", "friend123", "friendtoken123");

        makeFriends(user, friend);

        // assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(user.getToken(), friend.getId())
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(0, friendRequestRepository.findAll().size());
    }

    //#404
    @Test
    public void getPendingFriendRequests_returnsOnlyPendingRequests() {
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        User pendingSender = createTestUser("pendingSender", "pendingSender123", "pendingSendertoken123");
        User acceptedSender = createTestUser("acceptedSender", "acceptedSender123", "acceptedSendertoken123");
        User declinedSender = createTestUser("declinedSender", "declinedSender123", "declinedSendertoken123");
        FriendRequest pendingRequest = createTestFriendRequest(pendingSender, receiver, FriendRequestStatus.PENDING);
        FriendRequest acceptedRequest = createTestFriendRequest(acceptedSender, receiver, FriendRequestStatus.ACCEPTED);
        FriendRequest declinedRequest = createTestFriendRequest(declinedSender, receiver, FriendRequestStatus.DECLINED);

        // fetch pending requests
        List<FriendRequest> pendingRequests = friendRequestService.getPendingFriendRequests(receiver.getToken());

        // assert
        assertTrue(pendingRequests.stream().anyMatch(request -> request.getId().equals(pendingRequest.getId())));
        assertFalse(pendingRequests.stream().anyMatch(request -> request.getId().equals(acceptedRequest.getId())));
        assertFalse(pendingRequests.stream().anyMatch(request -> request.getId().equals(declinedRequest.getId())));
    }

    //#405
    @Test
    @Transactional
    public void acceptFriendRequest_validPendingRequest_createsFriendship() {
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);

        // accept friend request
        friendRequestService.acceptFriendRequest(request.getId(), receiver.getToken());

        // assert
        FriendRequest updatedRequest = friendRequestRepository.findById(request.getId()).orElseThrow();
        User updatedSender = userRepository.findById(sender.getId()).orElseThrow();
        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();

        assertEquals(FriendRequestStatus.ACCEPTED, updatedRequest.getStatus());
        assertTrue(updatedSender.getFriends().stream().anyMatch(friend -> friend.getId().equals(receiver.getId())));
        assertTrue(updatedReceiver.getFriends().stream().anyMatch(friend -> friend.getId().equals(sender.getId())));
    }

    //#406
    @Test
    @Transactional
    public void declineFriendRequest_validPendingRequest_doesNotCreateFriendship() {
        User sender = createTestUser("sender", "sender123", "sendertoken123");
        User receiver = createTestUser("receiver", "receiver123", "receivertoken123");
        FriendRequest request = createTestFriendRequest(sender, receiver, FriendRequestStatus.PENDING);

        // decline request
        friendRequestService.declineFriendRequest(request.getId(), receiver.getToken());

        // assert
        FriendRequest updatedRequest = friendRequestRepository.findById(request.getId()).orElseThrow();
        User updatedSender = userRepository.findById(sender.getId()).orElseThrow();
        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();

        assertEquals(FriendRequestStatus.DECLINED, updatedRequest.getStatus());
        assertFalse(updatedSender.getFriends().stream().anyMatch(friend -> friend.getId().equals(receiver.getId())));
        assertFalse(updatedReceiver.getFriends().stream().anyMatch(friend -> friend.getId().equals(sender.getId())));
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

    private FriendRequest createTestFriendRequest(User sender, User receiver, FriendRequestStatus status) {
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(status);
        return friendRequestRepository.save(request);
    }

    private void makeFriends(User user1, User user2) {
        user1.getFriends().add(user2);
        user2.getFriends().add(user1);
        userRepository.save(user1);
        userRepository.save(user2);
    }
}
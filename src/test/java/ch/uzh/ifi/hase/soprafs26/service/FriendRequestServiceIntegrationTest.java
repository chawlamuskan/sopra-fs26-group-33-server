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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @MockitoBean GeocodingService geocodingService;
    

    //#227
    @Test
    public void sendFriendRequest_duplicatePendingRequest_throwsConflict() {
        // create user: sender
        User sender = new User();
        sender.setName("sender");
        sender.setUsername("sender123");
        sender.setPassword("pw");
        sender.setEmail("sender123@test.ch");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sendertoken");
        sender = userRepository.save(sender);
        String senderToken = sender.getToken();

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
        Long receiverId = receiver.getId();

        //create/send friend request
        friendRequestService.sendFriendRequest(senderToken, receiverId);

        //assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(senderToken, receiverId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    //#226
    @Test
    @Transactional
    public void removeFriend_existingFriendship_updatesFriendListCorrectly() {
        // create user: sender
        User sender = new User();
        sender.setName("sender");
        sender.setUsername("sender1234");
        sender.setPassword("pw");
        sender.setEmail("sender1234@test.ch");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sendertoken1234");
        userRepository.save(sender);

        // create user: receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver1234");
        receiver.setPassword("pw");
        receiver.setEmail("receiver1234@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken1234");
        userRepository.save(receiver);

        //add each other as friends
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        userRepository.save(sender);
        userRepository.save(receiver);

        String senderToken = sender.getToken();
        Long senderId = sender.getId();
        Long receiverId = receiver.getId();

        // remove friend
        friendRequestService.removeFriend(senderToken, receiverId);

        // assert
        User updatedSender = userRepository.findById(senderId).orElseThrow();
        User updatedReceiver = userRepository.findById(receiverId).orElseThrow();

        assertFalse(updatedSender.getFriends().stream().anyMatch(user -> user.getId().equals(receiverId)));
        assertFalse(updatedReceiver.getFriends().stream().anyMatch(user -> user.getId().equals(senderId)));
    }

    //#226
    @Test
    public void removeFriend_nonFriend_throwsNotFound() {
        // create user: sender
        User sender = new User();
        sender.setName("sender");
        sender.setUsername("sender1234");
        sender.setPassword("pw");
        sender.setEmail("sender1234@test.ch");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sendertoken1234");
        userRepository.save(sender);

        // create user: receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver1234");
        receiver.setPassword("pw");
        receiver.setEmail("receiver1234@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken1234");
        userRepository.save(receiver);


        String senderToken = sender.getToken();
        Long receiverId = receiver.getId();

        //assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.removeFriend(senderToken, receiverId)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    //#223
    @Test
    public void sendFriendRequest_validInput_storesPendingRequestInDatabase() {
        // create user: sender
        User sender = new User();
        sender.setName("sender");
        sender.setUsername("sender1234");
        sender.setPassword("pw");
        sender.setEmail("sender1234@test.ch");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sendertoken1234");
        userRepository.save(sender);
        Long senderId = sender.getId();

        // create user: receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver1234");
        receiver.setPassword("pw");
        receiver.setEmail("receiver1234@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken1234");
        userRepository.save(receiver);
        Long receiverId = receiver.getId();

        //create friendrequest
        FriendRequest createdRequest = friendRequestService.sendFriendRequest(sender.getToken(), receiver.getId());

        //assert
        assertNotNull(createdRequest.getId());
        assertEquals(FriendRequestStatus.PENDING, createdRequest.getStatus());
        assertEquals(senderId, createdRequest.getSender().getId());
        assertEquals(receiverId, createdRequest.getReceiver().getId());

        //search for created
        FriendRequest storedRequest = friendRequestRepository.findById(createdRequest.getId()).orElseThrow();

        //assert
        assertEquals(FriendRequestStatus.PENDING, storedRequest.getStatus());
        assertEquals(senderId, storedRequest.getSender().getId());
        assertEquals(receiverId, storedRequest.getReceiver().getId());
    }

    //#402
    @Test
    @Transactional
    public void getFriendList_onlyAcceptedFriends_returnsOnlyAcceptedFriends() {
        // create user
        User user = new User();
        user.setName("user");
        user.setUsername("user402");
        user.setPassword("pw");
        user.setEmail("user402@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("usertoken402");
        userRepository.save(user);

        // create accepted friend
        User acceptedFriend = new User();
        acceptedFriend.setName("acceptedFriend");
        acceptedFriend.setUsername("accepted402");
        acceptedFriend.setPassword("pw");
        acceptedFriend.setEmail("accepted402@test.ch");
        acceptedFriend.setCreationDate(LocalDate.now());
        acceptedFriend.setStatus(UserStatus.ONLINE);
        acceptedFriend.setToken("acceptedtoken402");
        userRepository.save(acceptedFriend);

        // create pending/not accepted user
        User pendingUser = new User();
        pendingUser.setName("pendingUser");
        pendingUser.setUsername("pending402");
        pendingUser.setPassword("pw");
        pendingUser.setEmail("pending402@test.ch");
        pendingUser.setCreationDate(LocalDate.now());
        pendingUser.setStatus(UserStatus.ONLINE);
        pendingUser.setToken("pendingtoken402");
        userRepository.save(pendingUser);

        // add only acceptedFriend as actual friend
        user.getFriends().add(acceptedFriend);
        acceptedFriend.getFriends().add(user);
        userRepository.save(user);
        userRepository.save(acceptedFriend);

        // create pending friend request that should not appear in friend list
        FriendRequest pendingRequest = new FriendRequest();
        pendingRequest.setSender(pendingUser);
        pendingRequest.setReceiver(user);
        pendingRequest.setStatus(FriendRequestStatus.PENDING);
        friendRequestRepository.save(pendingRequest);

        Long userId = user.getId();
        Long acceptedFriendId = acceptedFriend.getId();
        Long pendingUserId = pendingUser.getId();

        // fetch friend list
        List<User> friends = friendRequestService.getFriends(user.getToken());

        // assert
        assertTrue(friends.stream().anyMatch(friend -> friend.getId().equals(acceptedFriendId)));
        assertFalse(friends.stream().anyMatch(friend -> friend.getId().equals(userId)));
        assertFalse(friends.stream().anyMatch(friend -> friend.getId().equals(pendingUserId)));
    }

    //#403 friend request to yourself 
    @Test
    public void sendFriendRequest_toYourself_throwsBadRequest() {
        // create user
        User user = new User();
        user.setName("user");
        user.setUsername("user403");
        user.setPassword("pw");
        user.setEmail("user403@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("usertoken403");
        userRepository.save(user);

        String userToken = user.getToken();
        Long userId = user.getId();

        // assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(userToken, userId)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(0, friendRequestRepository.findAll().size());
    }

    //#403 friend request to existing friend 
    @Test
    @Transactional
    public void sendFriendRequest_toExistingFriend_throwsConflict() {
        // create user
        User user = new User();
        user.setName("user");
        user.setUsername("user403friend");
        user.setPassword("pw");
        user.setEmail("user403friend@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("usertoken403friend");
        userRepository.save(user);

        // create friend
        User friend = new User();
        friend.setName("friend");
        friend.setUsername("friend403");
        friend.setPassword("pw");
        friend.setEmail("friend403@test.ch");
        friend.setCreationDate(LocalDate.now());
        friend.setStatus(UserStatus.ONLINE);
        friend.setToken("friendtoken403");
        userRepository.save(friend);

        // add each other as friends
        user.getFriends().add(friend);
        friend.getFriends().add(user);
        userRepository.save(user);
        userRepository.save(friend);

        String userToken = user.getToken();
        Long friendId = friend.getId();

        // assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> friendRequestService.sendFriendRequest(userToken, friendId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(0, friendRequestRepository.findAll().size());
    }

    //#404
    @Test
    public void getPendingFriendRequests_returnsOnlyPendingRequests() {
        // create receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver404");
        receiver.setPassword("pw");
        receiver.setEmail("receiver404@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken404");
        userRepository.save(receiver);

        // create pending sender
        User pendingSender = new User();
        pendingSender.setName("pendingSender");
        pendingSender.setUsername("pendingSender404");
        pendingSender.setPassword("pw");
        pendingSender.setEmail("pendingSender404@test.ch");
        pendingSender.setCreationDate(LocalDate.now());
        pendingSender.setStatus(UserStatus.ONLINE);
        pendingSender.setToken("pendingSenderToken404");
        userRepository.save(pendingSender);

        // create accepted sender
        User acceptedSender = new User();
        acceptedSender.setName("acceptedSender");
        acceptedSender.setUsername("acceptedSender404");
        acceptedSender.setPassword("pw");
        acceptedSender.setEmail("acceptedSender404@test.ch");
        acceptedSender.setCreationDate(LocalDate.now());
        acceptedSender.setStatus(UserStatus.ONLINE);
        acceptedSender.setToken("acceptedSenderToken404");
        userRepository.save(acceptedSender);

        // create declined sender
        User declinedSender = new User();
        declinedSender.setName("declinedSender");
        declinedSender.setUsername("declinedSender404");
        declinedSender.setPassword("pw");
        declinedSender.setEmail("declinedSender404@test.ch");
        declinedSender.setCreationDate(LocalDate.now());
        declinedSender.setStatus(UserStatus.ONLINE);
        declinedSender.setToken("declinedSenderToken404");
        userRepository.save(declinedSender);

        // create pending request
        FriendRequest pendingRequest = new FriendRequest();
        pendingRequest.setSender(pendingSender);
        pendingRequest.setReceiver(receiver);
        pendingRequest.setStatus(FriendRequestStatus.PENDING);
        pendingRequest = friendRequestRepository.save(pendingRequest);
        Long pendingRequestId = pendingRequest.getId();

        // create accepted request
        FriendRequest acceptedRequest = new FriendRequest();
        acceptedRequest.setSender(acceptedSender);
        acceptedRequest.setReceiver(receiver);
        acceptedRequest.setStatus(FriendRequestStatus.ACCEPTED);
        acceptedRequest = friendRequestRepository.save(acceptedRequest);
        Long acceptedRequestId = acceptedRequest.getId();

        // create declined request
        FriendRequest declinedRequest = new FriendRequest();
        declinedRequest.setSender(declinedSender);
        declinedRequest.setReceiver(receiver);
        declinedRequest.setStatus(FriendRequestStatus.DECLINED);
        declinedRequest = friendRequestRepository.save(declinedRequest);
        Long declinedRequestId = declinedRequest.getId();

        // fetch pending requests
        List<FriendRequest> pendingRequests = friendRequestService.getPendingFriendRequests(receiver.getToken());

        // assert
        assertTrue(pendingRequests.stream().anyMatch(request -> request.getId().equals(pendingRequestId)));
        assertFalse(pendingRequests.stream().anyMatch(request -> request.getId().equals(acceptedRequestId)));
        assertFalse(pendingRequests.stream().anyMatch(request -> request.getId().equals(declinedRequestId)));
    }

    //#405
    @Test
    @Transactional
    public void acceptFriendRequest_validPendingRequest_createsFriendship() {
        // create sender
        User sender = new User();
        sender.setName("sender");
        sender.setUsername("sender405");
        sender.setPassword("pw");
        sender.setEmail("sender405@test.ch");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sendertoken405");
        userRepository.save(sender);

        // create receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver405");
        receiver.setPassword("pw");
        receiver.setEmail("receiver405@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken405");
        userRepository.save(receiver);

        // create pending friend request
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);
        request = friendRequestRepository.save(request);

        Long requestId = request.getId();
        Long senderId = sender.getId();
        Long receiverId = receiver.getId();

        // accept friend request
        friendRequestService.acceptFriendRequest(requestId, receiver.getToken());

        // assert
        FriendRequest updatedRequest = friendRequestRepository.findById(requestId).orElseThrow();
        User updatedSender = userRepository.findById(senderId).orElseThrow();
        User updatedReceiver = userRepository.findById(receiverId).orElseThrow();

        assertEquals(FriendRequestStatus.ACCEPTED, updatedRequest.getStatus());
        assertTrue(updatedSender.getFriends().stream().anyMatch(friend -> friend.getId().equals(receiverId)));
        assertTrue(updatedReceiver.getFriends().stream().anyMatch(friend -> friend.getId().equals(senderId)));
    }

    //#406
    @Test
    @Transactional
    public void declineFriendRequest_validPendingRequest_doesNotCreateFriendship() {
        // create sender
        User sender = new User();
        sender.setName("sender");
        sender.setUsername("sender406");
        sender.setPassword("pw");
        sender.setEmail("sender406@test.ch");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sendertoken406");
        userRepository.save(sender);

        // create receiver
        User receiver = new User();
        receiver.setName("receiver");
        receiver.setUsername("receiver406");
        receiver.setPassword("pw");
        receiver.setEmail("receiver406@test.ch");
        receiver.setCreationDate(LocalDate.now());
        receiver.setStatus(UserStatus.ONLINE);
        receiver.setToken("receivertoken406");
        userRepository.save(receiver);

        // create pending friend request
        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);
        request = friendRequestRepository.save(request);

        Long requestId = request.getId();
        Long senderId = sender.getId();
        Long receiverId = receiver.getId();

        // decline request
        friendRequestService.declineFriendRequest(requestId, receiver.getToken());

        // assert
        FriendRequest updatedRequest = friendRequestRepository.findById(requestId).orElseThrow();
        User updatedSender = userRepository.findById(senderId).orElseThrow();
        User updatedReceiver = userRepository.findById(receiverId).orElseThrow();

        assertEquals(FriendRequestStatus.DECLINED, updatedRequest.getStatus());
        assertFalse(updatedSender.getFriends().stream().anyMatch(friend -> friend.getId().equals(receiverId)));
        assertFalse(updatedReceiver.getFriends().stream().anyMatch(friend -> friend.getId().equals(senderId)));
    }
}
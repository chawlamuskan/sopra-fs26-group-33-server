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

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

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
        friendRequestRepository.deleteAll();
        travelBoardRepository.deleteAll();
        preferencesRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();
	}
    

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

}
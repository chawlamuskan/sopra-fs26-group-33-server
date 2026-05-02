package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
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
	}
    

    //#227
    @Test
    public void sendFriendRequest_duplicatePendingRequest_throwsConflict() {
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
        String senderToken = owner.getToken();

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

}
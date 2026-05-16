package ch.uzh.ifi.hase.soprafs26.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

/**
 * NotificationBellBadgeCountIntegrationTest
 * - verifies that the backend returns the pending invitations and friend requests
 *   that drive the notification bell badge count in the client.
 */
@WebAppConfiguration
@SpringBootTest(properties = "GOOGLE_MAPS_API_KEY=test")
public class NotificationBellBadgeCountIntegrationTest {

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
        userRepository.deleteAll();
        userRepository.flush();
    }

    @Test
    public void getPendingNotifications_returnsCombinedPendingCount() {
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

        // create user: invitation sender / board owner
        User owner = new User();
        owner.setName("owner");
        owner.setUsername("owner123");
        owner.setPassword("pw");
        owner.setEmail("owner123@test.ch");
        owner.setCreationDate(LocalDate.now());
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("ownertoken");
        owner = userRepository.save(owner);

        // create user: friend request sender
        User friendSender = new User();
        friendSender.setName("friendSender");
        friendSender.setUsername("friendSender123");
        friendSender.setPassword("pw");
        friendSender.setEmail("friendSender123@test.ch");
        friendSender.setCreationDate(LocalDate.now());
        friendSender.setStatus(UserStatus.ONLINE);
        friendSender.setToken("friendsendertoken");
        friendSender = userRepository.save(friendSender);

        // create first board and invitation
        TravelBoard boardOne = new TravelBoard();
        boardOne.setName("Paris Trip");
        boardOne.setOwner(owner);
        boardOne.setInviteCode("CODE123");
        boardOne.setPrivacy(PrivacyLevel.PUBLIC);
        boardOne.setDateCreated(LocalDate.now());
        boardOne = travelBoardRepository.save(boardOne);

        Invitation firstInvitation = invitationService.createInvitation(boardOne.getId(), owner.getToken(), receiver.getId());
        assertEquals(boardOne.getId(), firstInvitation.getBoard().getId());

        // create second board and invitation
        TravelBoard boardTwo = new TravelBoard();
        boardTwo.setName("Berlin Trip");
        boardTwo.setOwner(owner);
        boardTwo.setInviteCode("CODE456");
        boardTwo.setPrivacy(PrivacyLevel.PUBLIC);
        boardTwo.setDateCreated(LocalDate.now());
        boardTwo = travelBoardRepository.save(boardTwo);

        Invitation secondInvitation = invitationService.createInvitation(boardTwo.getId(), owner.getToken(), receiver.getId());
        assertEquals(boardTwo.getId(), secondInvitation.getBoard().getId());

        // create one pending friend request
        friendRequestService.sendFriendRequest(friendSender.getToken(), receiver.getId());

        List<Invitation> pendingInvitations = invitationService.getPendingInvitations(receiver.getToken());
        List<?> pendingFriendRequests = friendRequestService.getPendingFriendRequests(receiver.getToken());

        assertEquals(2, pendingInvitations.size());
        assertEquals(1, pendingFriendRequests.size());
        assertEquals(3, pendingInvitations.size() + pendingFriendRequests.size());
    }
}

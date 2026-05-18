package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.JoinRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.JoinRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import jakarta.transaction.Transactional;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the JoinRequestService, which also involves the database.
 * - uses the actual repositories and an in-memory database (H2)
 * - tests the full integration of the JoinRequestService with the database
 *
 * Detects:
 * - logic errors in the JoinRequestService
 * - database constraints
 * - JPA mapping errors
 *
 * For tests that do not involve the database, @see JoinRequestServiceTest.
 */
@WebAppConfiguration
@SpringBootTest
public class JoinRequestServiceIntegrationTest {

    @Qualifier("joinRequestRepository")
    @Autowired
    private JoinRequestRepository joinRequestRepository;

    @Qualifier("travelBoardRepository")
    @Autowired
    private TravelBoardRepository travelBoardRepository;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Qualifier("invitationRepository")
    @Autowired
    private InvitationRepository invitationRepository;

    @Qualifier("preferencesRepository")
    @Autowired
    private PreferencesRepository preferencesRepository;

    @Autowired
    private JoinRequestService joinRequestService;

    private User sender;
    private User owner;
    private TravelBoard board;

    @BeforeEach
    public void setup() {
        joinRequestRepository.deleteAll();
        invitationRepository.deleteAll();
        travelBoardRepository.deleteAll();
        preferencesRepository.deleteAll();
        userRepository.deleteAll();

        // create owner
        owner = new User();
        owner.setName("owner");
        owner.setUsername("owner123");
        owner.setEmail("owner@test.ch");
        owner.setPassword("pw");
        owner.setCreationDate(LocalDate.now());
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("owner-token");
        owner = userRepository.save(owner);

        // create sender
        sender = new User();
        sender.setName("sender");
        sender.setUsername("sender123");
        sender.setEmail("sender@test.ch");
        sender.setPassword("pw");
        sender.setCreationDate(LocalDate.now());
        sender.setStatus(UserStatus.ONLINE);
        sender.setToken("sender-token");
        sender = userRepository.save(sender);

        // create board owned by owner
        board = new TravelBoard();
        board.setName("Test Board");
        board.setOwner(owner);
        board.setInviteCode("BOARD123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
        board = travelBoardRepository.save(board);
    }

    // ========================= SEND JOIN REQUEST TESTS =========================

    @Test // test that a join request is stored in the database
    public void sendJoinRequest_validInput_storedInDatabase() {
        // WHEN sender sends a join request
        JoinRequest result = joinRequestService.sendJoinRequest("sender-token", board.getId());

        // THEN request is stored with correct fields
        assertNotNull(result.getId());
        assertEquals(JoinRequestStatus.PENDING, result.getStatus());
        assertEquals(sender.getId(), result.getSender().getId());
        assertEquals(board.getId(), result.getBoard().getId());
    }

    @Test // test that duplicate pending request throws 409
    public void sendJoinRequest_duplicatePending_throwsConflict() {
        // GIVEN a pending request already exists
        joinRequestService.sendJoinRequest("sender-token", board.getId());

        // WHEN sending another request
        // THEN 409 CONFLICT is thrown
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("sender-token", board.getId()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test // test that already a member throws 409
    public void sendJoinRequest_alreadyMember_throwsConflict() {
        // GIVEN sender is already a member
        board.getMembers().add(sender);
        travelBoardRepository.save(board);

        // WHEN sender tries to send a join request
        // THEN 409 CONFLICT is thrown
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("sender-token", board.getId()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test // test that owner sending request to own board throws 409
    public void sendJoinRequest_isOwner_throwsConflict() {
        // WHEN owner sends a join request to their own board
        // THEN 409 CONFLICT is thrown
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("owner-token", board.getId()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // ========================= GET PENDING JOIN REQUESTS TESTS =========================

    @Test // test that pending requests are returned for board owner
    public void getPendingJoinRequests_validOwner_returnsList() {
        // GIVEN a pending join request exists
        joinRequestService.sendJoinRequest("sender-token", board.getId());

        // WHEN owner fetches pending join requests
        List<JoinRequest> result = joinRequestService.getPendingJoinRequests("owner-token");

        // THEN list contains the pending request
        assertEquals(1, result.size());
        assertEquals(JoinRequestStatus.PENDING, result.get(0).getStatus());
        assertEquals(sender.getId(), result.get(0).getSender().getId());
    }

    @Test // test that non-owner gets empty list
    public void getPendingJoinRequests_nonOwner_returnsEmptyList() {
        // GIVEN a pending join request exists for owner's board
        joinRequestService.sendJoinRequest("sender-token", board.getId());

        // WHEN sender (non-owner) fetches pending join requests
        List<JoinRequest> result = joinRequestService.getPendingJoinRequests("sender-token");

        // THEN empty list is returned since sender owns no boards
        assertTrue(result.isEmpty());
    }

    // ========================= ACCEPT JOIN REQUEST TESTS =========================
    
    @Test // test that accepting adds sender to board members
    public void acceptJoinRequest_validInput_addsSenderToMembers() {
        // GIVEN a pending join request
        JoinRequest joinRequest = joinRequestService.sendJoinRequest(
                "sender-token", board.getId());

        // WHEN owner accepts the join request
        joinRequestService.acceptJoinRequest(joinRequest.getId(), "owner-token");

        // THEN sender is added to board members and status is ACCEPTED
        JoinRequest updated = joinRequestRepository.findById(joinRequest.getId()).orElseThrow();
        assertEquals(JoinRequestStatus.ACCEPTED, updated.getStatus());

        TravelBoard updatedBoard = travelBoardRepository.findByIdWithMembers(board.getId()).orElseThrow();
        assertTrue(updatedBoard.getMembers().stream()
                .anyMatch(m -> m.getId().equals(sender.getId())));
    }

    @Test // test that non-owner cannot accept throws 401
    public void acceptJoinRequest_notOwner_throwsUnauthorized() {
        // GIVEN a pending join request
        JoinRequest joinRequest = joinRequestService.sendJoinRequest(
                "sender-token", board.getId());

        // create a third user who is neither sender nor owner
        User other = new User();
        other.setName("other");
        other.setUsername("other123");
        other.setEmail("other@test.ch");
        other.setPassword("pw");
        other.setCreationDate(LocalDate.now());
        other.setStatus(UserStatus.ONLINE);
        other.setToken("other-token");
        other = userRepository.save(other);

        final Long reqId = joinRequest.getId();

        // WHEN non-owner tries to accept
        // THEN 401 UNAUTHORIZED is thrown
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                joinRequestService.acceptJoinRequest(reqId, "other-token"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test // test that accepting already answered request throws 409
    public void acceptJoinRequest_alreadyAnswered_throwsConflict() {
        // GIVEN a join request that was already accepted
        JoinRequest joinRequest = joinRequestService.sendJoinRequest(
                "sender-token", board.getId());
        joinRequestService.acceptJoinRequest(joinRequest.getId(), "owner-token");

        final Long reqId = joinRequest.getId();

        // WHEN trying to accept again
        // THEN 409 CONFLICT is thrown
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                joinRequestService.acceptJoinRequest(reqId, "owner-token"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    // ========================= DECLINE JOIN REQUEST TESTS =========================

    @Test // test that declining does not add sender to members
    public void declineJoinRequest_validInput_senderNotAddedToMembers() {
        // GIVEN a pending join request
        JoinRequest joinRequest = joinRequestService.sendJoinRequest(
                "sender-token", board.getId());

        // WHEN owner declines the join request
        joinRequestService.declineJoinRequest(joinRequest.getId(), "owner-token");

        // THEN status is DECLINED and sender is not a member
        JoinRequest updated = joinRequestRepository.findById(joinRequest.getId()).orElseThrow();
        assertEquals(JoinRequestStatus.DECLINED, updated.getStatus());

        TravelBoard updatedBoard = travelBoardRepository.findByIdWithMembers(board.getId()).orElseThrow();
        assertFalse(updatedBoard.getMembers().stream()
                .anyMatch(m -> m.getId().equals(sender.getId())));
    }

    @Test // test that declining already answered request throws 409
    public void declineJoinRequest_alreadyAnswered_throwsConflict() {
        // GIVEN a join request that was already declined
        JoinRequest joinRequest = joinRequestService.sendJoinRequest(
                "sender-token", board.getId());
        joinRequestService.declineJoinRequest(joinRequest.getId(), "owner-token");

        final Long reqId = joinRequest.getId();

        // WHEN trying to decline again
        // THEN 409 CONFLICT is thrown
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                joinRequestService.declineJoinRequest(reqId, "owner-token"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
}
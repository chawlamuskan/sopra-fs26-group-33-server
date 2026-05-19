package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.JoinRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.JoinRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for the JoinRequestService.
 * - mocks with Mockito (no actual database operations)
 * - tests the business logic of the JoinRequestService in isolation
 *
 * Detects:
 * - logic errors in the JoinRequestService
 *
 * For tests that also involve the database, @see JoinRequestServiceIntegrationTest.
 */
public class JoinRequestServiceTest {

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @Mock
    private TravelBoardRepository travelBoardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JoinRequestService joinRequestService;

    @Mock
    private ActivityLogService activityLogService;

    private User sender;
    private User owner;
    private TravelBoard board;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // sender - user wanting to join
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setToken("sender-token");

        // owner - user who owns the board
        owner = new User();
        owner.setId(2L);
        owner.setUsername("owner");
        owner.setToken("owner-token");

        // board owned by owner
        board = new TravelBoard();
        board.setId(10L);
        board.setName("Test Board");
        board.setOwner(owner);
        board.setMembers(new ArrayList<>());

        // default mocks
        Mockito.when(userRepository.findByToken("sender-token")).thenReturn(sender);
        Mockito.when(userRepository.findByToken("owner-token")).thenReturn(owner);
        Mockito.when(travelBoardRepository.findById(10L)).thenReturn(Optional.of(board));
        Mockito.when(joinRequestRepository.save(Mockito.any()))
                .thenAnswer(i -> i.getArgument(0));
    }

    // ========================= SEND JOIN REQUEST TESTS =========================

    @Test // test that a join request can be sent successfully
    public void sendJoinRequest_validInput_success() {
        // GIVEN no existing pending request
        Mockito.when(joinRequestRepository.findBySenderAndBoardAndStatus(
                sender, board, JoinRequestStatus.PENDING)).thenReturn(null);

        // WHEN sending a join request
        JoinRequest result = joinRequestService.sendJoinRequest("sender-token", 10L);

        // THEN request is created with PENDING status
        assertNotNull(result);
        assertEquals(JoinRequestStatus.PENDING, result.getStatus());
        assertEquals(sender, result.getSender());
        assertEquals(board, result.getBoard());
        Mockito.verify(joinRequestRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test // test that sending a request with invalid token throws 401
    public void sendJoinRequest_invalidToken_throwsUnauthorized() {
        // GIVEN invalid token
        Mockito.when(userRepository.findByToken("invalid-token")).thenReturn(null);

        // WHEN sending a join request with invalid token
        // THEN 401 UNAUTHORIZED is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("invalid-token", 10L));
    }

    @Test // test that sending a request for non-existent board throws 404
    public void sendJoinRequest_boardNotFound_throwsNotFound() {
        // GIVEN non-existent board
        Mockito.when(travelBoardRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN sending a join request for non-existent board
        // THEN 404 NOT FOUND is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("sender-token", 999L));
    }

    @Test // test that sending a request when already a member throws 409
    public void sendJoinRequest_alreadyMember_throwsConflict() {
        // GIVEN sender is already a member of the board
        board.getMembers().add(sender);

        // WHEN sending a join request
        // THEN 409 CONFLICT is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("sender-token", 10L));
    }

    @Test // test that sending a request when owner throws 409
    public void sendJoinRequest_isOwner_throwsConflict() {
        // GIVEN owner tries to send a join request to their own board
        Mockito.when(userRepository.findByToken("owner-token")).thenReturn(owner);

        // WHEN owner sends a join request to their own board
        // THEN 409 CONFLICT is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("owner-token", 10L));
    }

    @Test // test that sending a duplicate pending request throws 409
    public void sendJoinRequest_duplicatePending_throwsConflict() {
        // GIVEN an existing pending request already exists
        JoinRequest existing = new JoinRequest();
        existing.setStatus(JoinRequestStatus.PENDING);
        Mockito.when(joinRequestRepository.findBySenderAndBoardAndStatus(
                sender, board, JoinRequestStatus.PENDING)).thenReturn(existing);

        // WHEN sending another join request
        // THEN 409 CONFLICT is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.sendJoinRequest("sender-token", 10L));
    }

    // ========================= GET PENDING JOIN REQUESTS TESTS =========================

    @Test // test that pending join requests are returned for board owner
    public void getPendingJoinRequests_validToken_returnsList() {
        // GIVEN two pending join requests for boards owned by owner
        JoinRequest req1 = new JoinRequest();
        req1.setId(1L);
        req1.setSender(sender);
        req1.setBoard(board);
        req1.setStatus(JoinRequestStatus.PENDING);

        Mockito.when(joinRequestRepository.findByBoardOwnerAndStatus(
                owner, JoinRequestStatus.PENDING)).thenReturn(List.of(req1));

        // WHEN fetching pending join requests
        List<JoinRequest> result = joinRequestService.getPendingJoinRequests("owner-token");

        // THEN list with one request is returned
        assertEquals(1, result.size());
        assertEquals(JoinRequestStatus.PENDING, result.get(0).getStatus());
    }

    @Test // test that invalid token throws 401
    public void getPendingJoinRequests_invalidToken_throwsUnauthorized() {
        // GIVEN invalid token
        Mockito.when(userRepository.findByToken("invalid-token")).thenReturn(null);

        // WHEN fetching pending join requests with invalid token
        // THEN 401 UNAUTHORIZED is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.getPendingJoinRequests("invalid-token"));
    }

    // ========================= ACCEPT JOIN REQUEST TESTS =========================

    @Test // test that owner can accept a pending join request
    public void acceptJoinRequest_validInput_success() {
        // GIVEN a pending join request
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setSender(sender);
        joinRequest.setBoard(board);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        Mockito.when(joinRequestRepository.findById(1L))
                .thenReturn(Optional.of(joinRequest));

        // WHEN owner accepts the join request
        joinRequestService.acceptJoinRequest(1L, "owner-token");

        // THEN sender is added to board members and status is ACCEPTED
        assertTrue(board.getMembers().contains(sender));
        assertEquals(JoinRequestStatus.ACCEPTED, joinRequest.getStatus());
        Mockito.verify(travelBoardRepository, Mockito.times(1)).save(board);
        Mockito.verify(joinRequestRepository, Mockito.times(1)).save(joinRequest);
    }

    @Test // test that non-owner cannot accept a join request
    public void acceptJoinRequest_notOwner_throwsUnauthorized() {
        // GIVEN a pending join request and a non-owner trying to accept
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setSender(sender);
        joinRequest.setBoard(board);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        User nonOwner = new User();
        nonOwner.setId(3L);
        nonOwner.setToken("nonowner-token");
        Mockito.when(userRepository.findByToken("nonowner-token")).thenReturn(nonOwner);
        Mockito.when(joinRequestRepository.findById(1L))
                .thenReturn(Optional.of(joinRequest));

        // WHEN non-owner tries to accept
        // THEN 401 UNAUTHORIZED is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.acceptJoinRequest(1L, "nonowner-token"));
    }

    @Test // test that accepting already answered request throws 409
    public void acceptJoinRequest_alreadyAnswered_throwsConflict() {
        // GIVEN a join request that was already accepted
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setSender(sender);
        joinRequest.setBoard(board);
        joinRequest.setStatus(JoinRequestStatus.ACCEPTED); // already answered

        Mockito.when(joinRequestRepository.findById(1L))
                .thenReturn(Optional.of(joinRequest));

        // WHEN trying to accept again
        // THEN 409 CONFLICT is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.acceptJoinRequest(1L, "owner-token"));
    }

    // ========================= DECLINE JOIN REQUEST TESTS =========================

    @Test // test that owner can decline a pending join request
    public void declineJoinRequest_validInput_success() {
        // GIVEN a pending join request
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setSender(sender);
        joinRequest.setBoard(board);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        Mockito.when(joinRequestRepository.findById(1L))
                .thenReturn(Optional.of(joinRequest));

        // WHEN owner declines the join request
        joinRequestService.declineJoinRequest(1L, "owner-token");

        // THEN status is DECLINED and sender is NOT added to members
        assertEquals(JoinRequestStatus.DECLINED, joinRequest.getStatus());
        assertFalse(board.getMembers().contains(sender));
        Mockito.verify(joinRequestRepository, Mockito.times(1)).save(joinRequest);
    }

    @Test // test that non-owner cannot decline a join request
    public void declineJoinRequest_notOwner_throwsUnauthorized() {
        // GIVEN a pending join request and a non-owner trying to decline
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setSender(sender);
        joinRequest.setBoard(board);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        User nonOwner = new User();
        nonOwner.setId(3L);
        nonOwner.setToken("nonowner-token");
        Mockito.when(userRepository.findByToken("nonowner-token")).thenReturn(nonOwner);
        Mockito.when(joinRequestRepository.findById(1L))
                .thenReturn(Optional.of(joinRequest));

        // WHEN non-owner tries to decline
        // THEN 401 UNAUTHORIZED is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.declineJoinRequest(1L, "nonowner-token"));
    }

    @Test // test that declining already answered request throws 409
    public void declineJoinRequest_alreadyAnswered_throwsConflict() {
        // GIVEN a join request that was already declined
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setSender(sender);
        joinRequest.setBoard(board);
        joinRequest.setStatus(JoinRequestStatus.DECLINED); // already answered

        Mockito.when(joinRequestRepository.findById(1L))
                .thenReturn(Optional.of(joinRequest));

        // WHEN trying to decline again
        // THEN 409 CONFLICT is thrown
        assertThrows(ResponseStatusException.class, () ->
                joinRequestService.declineJoinRequest(1L, "owner-token"));
    }
}
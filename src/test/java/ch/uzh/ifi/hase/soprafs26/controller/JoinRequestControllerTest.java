package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.JoinRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.JoinRequestService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * JoinRequestControllerTest
 * - simulates HTTP requests to test the JoinRequestController
 * - mocks the JoinRequestService
 * - tests correct HTTP status codes and response bodies
 *
 * Detects:
 * - wrong HTTP status codes
 * - wrong response body structure
 * - missing authorization checks
 */
@WebMvcTest(JoinRequestController.class)
public class JoinRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JoinRequestService joinRequestService;

    @MockitoBean
    private UserService userService;

    // helper to create a mock logged-in user
    private User mockUser(Long id, String token) {
        User user = new User();
        user.setId(id);
        user.setUsername("testUser" + id);
        user.setToken(token);
        return user;
    }

    // helper to create a mock join request
    private JoinRequest mockJoinRequest(Long id, User sender, TravelBoard board) {
        JoinRequest req = new JoinRequest();
        req.setId(id);
        req.setSender(sender);
        req.setBoard(board);
        req.setStatus(JoinRequestStatus.PENDING);
        return req;
    }

    // helper to create a mock travel board
    private TravelBoard mockBoard(Long id, String name, User owner) {
        TravelBoard board = new TravelBoard();
        board.setId(id);
        board.setName(name);
        board.setOwner(owner);
        board.setMembers(new ArrayList<>());
        return board;
    }

    // ========================= POST /joinRequests/{boardId} TESTS =========================

    @Test // test that a join request is created successfully
    public void sendJoinRequest_validInput_returnsCreated() throws Exception {
        // GIVEN a logged-in user and a valid board
        User sender = mockUser(1L, "sender-token");
        User owner = mockUser(2L, "owner-token");
        TravelBoard board = mockBoard(10L, "Test Board", owner);
        JoinRequest joinRequest = mockJoinRequest(1L, sender, board);

        given(userService.validateToken(Mockito.eq("sender-token"))).willReturn(sender);
        given(joinRequestService.sendJoinRequest(
                Mockito.eq("sender-token"), Mockito.eq(10L)))
                .willReturn(joinRequest);

        // WHEN POST /joinRequests/10 is called
        MockHttpServletRequestBuilder postRequest = post("/joinRequests/10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "sender-token");

        // THEN return 201 CREATED with correct fields
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.boardId", is(10)))
                .andExpect(jsonPath("$.senderId", is(1)))
                .andExpect(jsonPath("$.senderUsername", is("testUser1")))
                .andExpect(jsonPath("$.boardName", is("Test Board")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test // test that sending a request without token returns 401
    public void sendJoinRequest_noToken_returnsUnauthorized() throws Exception {
        // GIVEN no token provided
        given(userService.validateToken(Mockito.isNull()))
                .willThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "No token provided"));

        // WHEN POST /joinRequests/10 is called without token
        MockHttpServletRequestBuilder postRequest = post("/joinRequests/10")
                .contentType(MediaType.APPLICATION_JSON);

        // THEN return 401 UNAUTHORIZED
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test // test that already a member returns 409
    public void sendJoinRequest_alreadyMember_returnsConflict() throws Exception {
        // GIVEN sender is already a member
        User sender = mockUser(1L, "sender-token");
        given(userService.validateToken(Mockito.eq("sender-token"))).willReturn(sender);
        given(joinRequestService.sendJoinRequest(
                Mockito.eq("sender-token"), Mockito.eq(10L)))
                .willThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT, "You are already a member of this board"));

        // WHEN POST /joinRequests/10 is called
        MockHttpServletRequestBuilder postRequest = post("/joinRequests/10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "sender-token");

        // THEN return 409 CONFLICT
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    @Test // test that duplicate pending request returns 409
    public void sendJoinRequest_duplicatePending_returnsConflict() throws Exception {
        // GIVEN a pending request already exists
        User sender = mockUser(1L, "sender-token");
        given(userService.validateToken(Mockito.eq("sender-token"))).willReturn(sender);
        given(joinRequestService.sendJoinRequest(
                Mockito.eq("sender-token"), Mockito.eq(10L)))
                .willThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "You already have a pending join request for this board"));

        // WHEN POST /joinRequests/10 is called again
        MockHttpServletRequestBuilder postRequest = post("/joinRequests/10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "sender-token");

        // THEN return 409 CONFLICT
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    @Test // test that board not found returns 404
    public void sendJoinRequest_boardNotFound_returnsNotFound() throws Exception {
        // GIVEN board does not exist
        User sender = mockUser(1L, "sender-token");
        given(userService.validateToken(Mockito.eq("sender-token"))).willReturn(sender);
        given(joinRequestService.sendJoinRequest(
                Mockito.eq("sender-token"), Mockito.eq(999L)))
                .willThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Board not found"));

        // WHEN POST /joinRequests/999 is called
        MockHttpServletRequestBuilder postRequest = post("/joinRequests/999")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "sender-token");

        // THEN return 404 NOT FOUND
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    // ========================= GET /joinRequests TESTS =========================

    @Test // test that pending join requests are returned for board owner
    public void getPendingJoinRequests_validOwner_returnsOk() throws Exception {
        // GIVEN two pending join requests
        User owner = mockUser(2L, "owner-token");
        User sender = mockUser(1L, "sender-token");
        TravelBoard board = mockBoard(10L, "Test Board", owner);
        JoinRequest req1 = mockJoinRequest(1L, sender, board);

        given(userService.validateToken(Mockito.eq("owner-token"))).willReturn(owner);
        given(joinRequestService.getPendingJoinRequests(Mockito.eq("owner-token")))
                .willReturn(List.of(req1));

        // WHEN GET /joinRequests is called
        MockHttpServletRequestBuilder getRequest = get("/joinRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "owner-token");

        // THEN return 200 OK with list of requests
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].boardId", is(10)))
                .andExpect(jsonPath("$[0].senderId", is(1)))
                .andExpect(jsonPath("$[0].status", is("PENDING")));
    }

    @Test // test that no token returns 401
    public void getPendingJoinRequests_noToken_returnsUnauthorized() throws Exception {
        // GIVEN no token
        given(userService.validateToken(Mockito.isNull()))
                .willThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "No token provided"));

        // WHEN GET /joinRequests is called without token
        MockHttpServletRequestBuilder getRequest = get("/joinRequests")
                .contentType(MediaType.APPLICATION_JSON);

        // THEN return 401 UNAUTHORIZED
        mockMvc.perform(getRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test // test that empty list is returned when no pending requests
    public void getPendingJoinRequests_noRequests_returnsEmptyList() throws Exception {
        // GIVEN no pending requests
        User owner = mockUser(2L, "owner-token");
        given(userService.validateToken(Mockito.eq("owner-token"))).willReturn(owner);
        given(joinRequestService.getPendingJoinRequests(Mockito.eq("owner-token")))
                .willReturn(List.of());

        // WHEN GET /joinRequests is called
        MockHttpServletRequestBuilder getRequest = get("/joinRequests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "owner-token");

        // THEN return 200 OK with empty list
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    // ========================= PUT /joinRequests/{id}/accept TESTS =========================

    @Test // test that owner can accept a join request
    public void acceptJoinRequest_validOwner_returnsNoContent() throws Exception {
        // GIVEN a valid token and join request
        User owner = mockUser(2L, "owner-token");
        given(userService.validateToken(Mockito.eq("owner-token"))).willReturn(owner);
        Mockito.doNothing().when(joinRequestService)
                .acceptJoinRequest(Mockito.eq(1L), Mockito.eq("owner-token"));

        // WHEN PUT /joinRequests/1/accept is called
        MockHttpServletRequestBuilder putRequest = put("/joinRequests/1/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "owner-token");

        // THEN return 204 NO CONTENT
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    @Test // test that non-owner gets 401 when accepting
    public void acceptJoinRequest_notOwner_returnsUnauthorized() throws Exception {
        // GIVEN a non-owner trying to accept
        User nonOwner = mockUser(3L, "nonowner-token");
        given(userService.validateToken(Mockito.eq("nonowner-token"))).willReturn(nonOwner);
        Mockito.doThrow(new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Only the board owner can accept join requests"))
                .when(joinRequestService)
                .acceptJoinRequest(Mockito.eq(1L), Mockito.eq("nonowner-token"));

        // WHEN PUT /joinRequests/1/accept is called by non-owner
        MockHttpServletRequestBuilder putRequest = put("/joinRequests/1/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "nonowner-token");

        // THEN return 401 UNAUTHORIZED
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test // test that accepting already answered request returns 409
    public void acceptJoinRequest_alreadyAnswered_returnsConflict() throws Exception {
        // GIVEN a join request already answered
        User owner = mockUser(2L, "owner-token");
        given(userService.validateToken(Mockito.eq("owner-token"))).willReturn(owner);
        Mockito.doThrow(new ResponseStatusException(
                HttpStatus.CONFLICT, "Join request was already answered"))
                .when(joinRequestService)
                .acceptJoinRequest(Mockito.eq(1L), Mockito.eq("owner-token"));

        // WHEN PUT /joinRequests/1/accept is called again
        MockHttpServletRequestBuilder putRequest = put("/joinRequests/1/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "owner-token");

        // THEN return 409 CONFLICT
        mockMvc.perform(putRequest)
                .andExpect(status().isConflict());
    }

    // ========================= PUT /joinRequests/{id}/decline TESTS =========================

    @Test // test that owner can decline a join request
    public void declineJoinRequest_validOwner_returnsNoContent() throws Exception {
        // GIVEN a valid token and join request
        User owner = mockUser(2L, "owner-token");
        given(userService.validateToken(Mockito.eq("owner-token"))).willReturn(owner);
        Mockito.doNothing().when(joinRequestService)
                .declineJoinRequest(Mockito.eq(1L), Mockito.eq("owner-token"));

        // WHEN PUT /joinRequests/1/decline is called
        MockHttpServletRequestBuilder putRequest = put("/joinRequests/1/decline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "owner-token");

        // THEN return 204 NO CONTENT
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    @Test // test that non-owner gets 401 when declining
    public void declineJoinRequest_notOwner_returnsUnauthorized() throws Exception {
        // GIVEN a non-owner trying to decline
        User nonOwner = mockUser(3L, "nonowner-token");
        given(userService.validateToken(Mockito.eq("nonowner-token"))).willReturn(nonOwner);
        Mockito.doThrow(new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Only the board owner can decline join requests"))
                .when(joinRequestService)
                .declineJoinRequest(Mockito.eq(1L), Mockito.eq("nonowner-token"));

        // WHEN PUT /joinRequests/1/decline is called by non-owner
        MockHttpServletRequestBuilder putRequest = put("/joinRequests/1/decline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "nonowner-token");

        // THEN return 401 UNAUTHORIZED
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test // test that declining already answered request returns 409
    public void declineJoinRequest_alreadyAnswered_returnsConflict() throws Exception {
        // GIVEN a join request already answered
        User owner = mockUser(2L, "owner-token");
        given(userService.validateToken(Mockito.eq("owner-token"))).willReturn(owner);
        Mockito.doThrow(new ResponseStatusException(
                HttpStatus.CONFLICT, "Join request was already answered"))
                .when(joinRequestService)
                .declineJoinRequest(Mockito.eq(1L), Mockito.eq("owner-token"));

        // WHEN PUT /joinRequests/1/decline is called again
        MockHttpServletRequestBuilder putRequest = put("/joinRequests/1/decline")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "owner-token");

        // THEN return 409 CONFLICT
        mockMvc.perform(putRequest)
                .andExpect(status().isConflict());
    }

    @Test // test that no token on decline returns 401
    public void declineJoinRequest_noToken_returnsUnauthorized() throws Exception {
        // GIVEN no token provided
        given(userService.validateToken(Mockito.isNull()))
                .willThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "No token provided"));

        // WHEN PUT /joinRequests/1/decline is called without token
        MockHttpServletRequestBuilder putRequest = put("/joinRequests/1/decline")
                .contentType(MediaType.APPLICATION_JSON);

        // THEN return 401 UNAUTHORIZED
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}
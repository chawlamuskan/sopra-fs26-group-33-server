package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.*;
import ch.uzh.ifi.hase.soprafs26.service.TravelBoardService;
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

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ActivityLogControllerTest
 * - tests that activity logs are returned correctly in the board GET response
 * - tests authorization for accessing activity logs
 */
@WebMvcTest(TravelBoardController.class)
public class ActivityLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TravelBoardService travelBoardService;

    @MockitoBean
    private UserService userService;

    private User mockUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("testUser");
        user.setToken("valid-token");
        return user;
    }

    private TravelBoard mockBoardWithLogs(User owner, List<ActivityLog> logs) {
        TravelBoard board = new TravelBoard();
        board.setId(1L);
        board.setName("Paris Trip");
        board.setOwner(owner);
        board.getActivityLogs().addAll(logs);
        return board;
    }

    private ActivityLog mockLog(Long id, User user, TravelBoard board, String action) {
        ActivityLog log = new ActivityLog();
        log.setId(id);
        log.setUser(user);
        log.setBoard(board);
        log.setAction(action);
        return log;
    }

    @Test
    // Test that board GET returns activity logs in the response
    public void getBoard_withActivityLogs_returnsLogsInResponse() throws Exception {
        User owner = mockUser(1L);
        given(userService.validateToken("valid-token")).willReturn(owner);

        TravelBoard board = mockBoardWithLogs(owner, List.of());
        ActivityLog log1 = mockLog(1L, owner, board, "added Eiffel Tower");
        ActivityLog log2 = mockLog(2L, owner, board, "joined the board");
        board.getActivityLogs().addAll(List.of(log1, log2));

        given(travelBoardService.getSingleTravelBoardById(1L, "valid-token")).willReturn(board);

        MockHttpServletRequestBuilder getRequest = get("/travelboards/1")
            .header("Authorization", "valid-token")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activityLogs", hasSize(2)))
            .andExpect(jsonPath("$.activityLogs[0].action", is("added Eiffel Tower")))
            .andExpect(jsonPath("$.activityLogs[0].userId", is(1)))
            .andExpect(jsonPath("$.activityLogs[1].action", is("joined the board")));
    }

    @Test
    // Test that board GET returns empty activity logs when none exist
    public void getBoard_noActivityLogs_returnsEmptyList() throws Exception {
        User owner = mockUser(1L);
        given(userService.validateToken("valid-token")).willReturn(owner);

        TravelBoard board = mockBoardWithLogs(owner, List.of());
        given(travelBoardService.getSingleTravelBoardById(1L, "valid-token")).willReturn(board);

        MockHttpServletRequestBuilder getRequest = get("/travelboards/1")
            .header("Authorization", "valid-token")
            .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activityLogs", hasSize(0)));
    }

    @Test
    // Test that board GET returns 401 when no token is provided
    public void getBoard_noToken_returnsUnauthorized() throws Exception {
        given(userService.validateToken(Mockito.isNull()))
            .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided"));

        mockMvc.perform(get("/travelboards/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    // Test that board GET returns 404 when board does not exist
    public void getBoard_boardNotFound_returnsNotFound() throws Exception {
        given(userService.validateToken("valid-token")).willReturn(mockUser(1L));
        given(travelBoardService.getSingleTravelBoardById(999L, "valid-token"))
            .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        mockMvc.perform(get("/travelboards/999")
                .header("Authorization", "valid-token")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
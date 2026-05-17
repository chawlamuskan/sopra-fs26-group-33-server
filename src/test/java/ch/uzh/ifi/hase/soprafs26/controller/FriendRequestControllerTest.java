package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import ch.uzh.ifi.hase.soprafs26.service.FriendRequestService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestPostDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;


@WebMvcTest(FriendRequestController.class)
public class FriendRequestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FriendRequestService friendRequestService;

    @MockitoBean
    private UserService userService;


    //#227
    @Test
    public void sendFriendRequest_duplicatePendingFriendRequest_returnsConflict() throws Exception {
        String token = "ABC";

        FriendRequestPostDTO friendRequestPostDTO = new FriendRequestPostDTO();
        friendRequestPostDTO.setReceiverId(2L);

        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);

        Mockito.doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "A pending friendRequest for this user exists"))
                .when(friendRequestService)
                .sendFriendRequest(token, 2L);

        MockHttpServletRequestBuilder postRequest = post("/friendRequests")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(friendRequestPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    //#226
    @Test
    public void removeFriend_validFriend_returnsNoContent() throws Exception {
        String token = "ABC123";
        Long friendId = 2L;

        User user = new User();
        user.setId(1L);
        Mockito.when(userService.validateToken(token)).thenReturn(user);

        MockHttpServletRequestBuilder deleteRequest = delete("/friends/{friendId}", friendId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());
    }

    //#404
    @Test
    public void getPendingFriendRequests_validToken_returnsOk() throws Exception {
        String token = "ABC123";
    
        User receiver = new User();
        User sender = new User();
   
        FriendRequest pendingRequest = new FriendRequest();
        pendingRequest.setId(100L);
        pendingRequest.setSender(sender);
        pendingRequest.setReceiver(receiver);
        pendingRequest.setStatus(FriendRequestStatus.PENDING);
    
        Mockito.when(userService.validateToken(token)).thenReturn(receiver);
        Mockito.when(friendRequestService.getPendingFriendRequests(token))
                .thenReturn(List.of(pendingRequest));
    
        MockHttpServletRequestBuilder getRequest = get("/friendRequests")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(getRequest)
                .andExpect(status().isOk());
    }

    //#405
    @Test
    public void acceptFriendRequest_validInput_returnsNoContent() throws Exception {
        String token = "ABC123";
        Long friendRequestId = 100L;
    
        User user = new User();
    
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doNothing().when(friendRequestService).acceptFriendRequest(friendRequestId, token);
    
        MockHttpServletRequestBuilder putRequest = put("/friendRequests/{friendRequestId}/accept", friendRequestId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    //#406
    @Test
    public void declineFriendRequest_validInput_returnsNoContent() throws Exception {
        String token = "ABC123";
        Long friendRequestId = 100L;
    
        User user = new User();
    
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doNothing().when(friendRequestService).declineFriendRequest(friendRequestId, token);
    
        MockHttpServletRequestBuilder putRequest = put("/friendRequests/{friendRequestId}/decline", friendRequestId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    //#402
    @Test
    public void getFriends_validToken_returnsOk() throws Exception {
        String token = "ABC123";
    
        User user = new User();
        user.setId(1L);

        User friend = new User();
        friend.setId(2L);
        friend.setName("Friend Name");
        friend.setUsername("friend123");
        friend.setEmail("friend@example.com");
    
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.when(friendRequestService.getFriends(token)).thenReturn(List.of(friend));
    
        MockHttpServletRequestBuilder getRequest = get("/friends")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(getRequest)
                .andExpect(status().isOk());
    }

    //#223
    @Test
    public void sendFriendRequest_validInput_returnsCreated() throws Exception {
        String token = "ABC123";
    
        FriendRequestPostDTO friendRequestPostDTO = new FriendRequestPostDTO();
        friendRequestPostDTO.setReceiverId(2L);
    
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender123");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver123");
    
        FriendRequest createdRequest = new FriendRequest();
        createdRequest.setId(100L);
        createdRequest.setSender(sender);
        createdRequest.setReceiver(receiver);
        createdRequest.setStatus(FriendRequestStatus.PENDING);
    
        Mockito.when(userService.validateToken(token)).thenReturn(sender);
        Mockito.when(friendRequestService.sendFriendRequest(token, 2L)).thenReturn(createdRequest);
    
        MockHttpServletRequestBuilder postRequest = post("/friendRequests")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(friendRequestPostDTO));
    
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());
    }

	

	/**
	 * Helper Method to convert userPostDTO into a JSON string such that the input
	 * can be processed
	 * Input will look like this: {"name": "Test User", "username": "testUsername"}
	 * 
	 * @param object
	 * @return string
	 */
	private String asJsonString(final Object object) {
		try {
			return new ObjectMapper().writeValueAsString(object);
		} catch (JacksonException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					String.format("The request body could not be created.%s", e.toString()));
		}
	}
}
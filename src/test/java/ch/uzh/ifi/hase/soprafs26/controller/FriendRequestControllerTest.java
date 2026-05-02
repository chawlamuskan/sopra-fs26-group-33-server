package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import ch.uzh.ifi.hase.soprafs26.service.FriendRequestService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
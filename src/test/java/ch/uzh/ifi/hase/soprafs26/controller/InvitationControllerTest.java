package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import ch.uzh.ifi.hase.soprafs26.service.InvitationService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InvitationPostDTO;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;


@WebMvcTest(InvitationController.class)
public class InvitationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InvitationService invitationService;

    @MockitoBean
    private UserService userService;

    //#262
    @Test
    public void getPendingInvitations_validToken_returnsInvitations() throws Exception {
        String token = "ABC123";
    
        User receiver = new User();
        receiver.setId(1L);
        receiver.setUsername("receiver");
    
        TravelBoard board = new TravelBoard();
        board.setId(10L);
        board.setName("Trip");
    
        Invitation invitation = new Invitation();
        invitation.setId(100L);
        invitation.setReceiver(receiver);
        invitation.setBoard(board);
    
        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
    
        Mockito.when(invitationService.getPendingInvitations(token)).thenReturn(List.of(invitation));
    
        MockHttpServletRequestBuilder getRequest = get("/invitations")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(getRequest)
                .andExpect(status().isOk());
    }

    //#263
    @Test
    public void createInvitation_duplicatePendingInvitation_returnsConflict() throws Exception {
        Long boardId = 1L;
        String token = "ABC";

        InvitationPostDTO invitationPostDTO = new InvitationPostDTO();
        invitationPostDTO.setReceiverId(2L);

        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);

        Mockito.doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "A pending invitation for this user and travel board already exists"))
                .when(invitationService)
                .createInvitation(boardId, token, 2L);

        MockHttpServletRequestBuilder postRequest = post("/travelboards/{boardId}/invitations", boardId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invitationPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    //#180
    @Test
    public void createInvitation_validInput_returnsCreated() throws Exception {
        Long boardId = 1L;
        String token = "ABC123";
    
        InvitationPostDTO invitationPostDTO = new InvitationPostDTO();
        invitationPostDTO.setReceiverId(2L);
    
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender123");
    
        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver123");
    
        TravelBoard board = new TravelBoard();
        board.setId(boardId);
        board.setName("Test Board");
    
        Invitation createdInvitation = new Invitation();
        createdInvitation.setId(100L);
        createdInvitation.setSender(sender);
        createdInvitation.setReceiver(receiver);
        createdInvitation.setBoard(board);
    
        Mockito.when(userService.validateToken(token)).thenReturn(sender);
        Mockito.when(invitationService.createInvitation(boardId, token, 2L)).thenReturn(createdInvitation);
    
        MockHttpServletRequestBuilder postRequest = post("/travelboards/{boardId}/invitations", boardId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invitationPostDTO));
    
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());
    } 

    //#155
    @Test
    public void acceptInvitation_validInput_returnsNoContent() throws Exception {
        String token = "ABC123";
        Long invitationId = 100L;
    
        User user = new User();
    
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doNothing().when(invitationService).acceptInvitation(invitationId, token);
    
        MockHttpServletRequestBuilder putRequest = put("/invitations/{invitationId}/accept", invitationId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }

    //#156
    @Test
    public void declineInvitation_validInput_returnsNoContent() throws Exception {
        String token = "ABC123";
        Long invitationId = 100L;
    
        User user = new User();
    
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doNothing().when(invitationService).declineInvitation(invitationId, token);
    
        MockHttpServletRequestBuilder putRequest = put("/invitations/{invitationId}/decline", invitationId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
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
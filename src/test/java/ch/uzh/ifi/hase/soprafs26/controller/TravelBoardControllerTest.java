package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.service.TravelBoardService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPutDTO;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TravelBoardController.class)
public class TravelBoardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TravelBoardService travelBoardService;

    @MockitoBean
    private UserService userService;

    //#119
	@Test
	public void renameTravelBoard_validInput_noContent() throws Exception {
        Long boardId = 1L;
        String token = "ABC";

		TravelBoardPutDTO travelBoardPutDTO = new TravelBoardPutDTO();
		travelBoardPutDTO.setName("Renamed Board");

        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
		Mockito.doNothing().when(travelBoardService).renameTravelBoard(boardId, token, "Renamed Board");
		
		MockHttpServletRequestBuilder putRequest = put("/travelboards/{boardId}", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
				.content(asJsonString(travelBoardPutDTO));

		mockMvc.perform(putRequest)
				.andExpect(status().isNoContent());
	}

    //#118
	@Test
	public void deleteTravelBoard_validInput_noContent() throws Exception {
		Long boardId = 1L;
        String token = "ABC";

        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doNothing().when(travelBoardService).deleteTravelBoard(boardId, token);

		MockHttpServletRequestBuilder deleteRequest = delete("/travelboards/{boardId}", boardId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON); 

		mockMvc.perform(deleteRequest)
				.andExpect(status().isNoContent()); 
	}

    //#120
	@Test
    public void renameTravelBoard_notOwner_unauthorized() throws Exception {
        Long boardId = 1L;
        String token = "ABC";
        
        TravelBoardPutDTO travelBoardPutDTO = new TravelBoardPutDTO();
        travelBoardPutDTO.setName("Hacked Name");
        
        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - must be owner"))
                .when(travelBoardService)
                .renameTravelBoard(boardId, token, "Hacked Name");
        
        MockHttpServletRequestBuilder putRequest = put("/travelboards/{boardId}", boardId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(asJsonString(travelBoardPutDTO));
        
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());
    }

    //#120
    @Test
    public void deleteTravelBoard_notOwner_unauthorized() throws Exception {
        Long boardId = 1L;
        String token = "ABC";

        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - must be owner"))
                .when(travelBoardService)
                .deleteTravelBoard(boardId, token);

        MockHttpServletRequestBuilder deleteRequest = delete("/travelboards/{boardId}", boardId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(deleteRequest)
                .andExpect(status().isUnauthorized());
    }

    //#153
    @Test
    public void joinTravelBoard_validCode_ok() throws Exception {
        String token = "ABC";
        String inviteCode = "VALID123";

        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doNothing().when(travelBoardService).joinTravelBoardByInviteCode(token, inviteCode);

        MockHttpServletRequestBuilder postRequest = post("/travelboards/join")
                .param("inviteCode", inviteCode)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isOk());
    }

    //#154
    @Test
    public void joinTravelBoard_invalidCode_notFound() throws Exception {
        String token = "ABC";
        String inviteCode = "INVALID123";

        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite code is invalid"))
                .when(travelBoardService)
                .joinTravelBoardByInviteCode(token, inviteCode);

        MockHttpServletRequestBuilder postRequest = post("/travelboards/join")
                .param("inviteCode", inviteCode)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    //#152
    @Test
    public void joinTravelBoard_missingToken_unauthorized() throws Exception {
        String inviteCode = "BOARD123";

        Mockito.when(userService.validateToken((String) Mockito.isNull()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        MockHttpServletRequestBuilder postRequest = post("/travelboards/join")
                .param("inviteCode", inviteCode)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
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
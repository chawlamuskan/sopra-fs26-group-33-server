package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.service.TravelBoardService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPostDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.ArrayList;


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

    //#115
    @Test
    public void getTravelBoardsByUser_validToken_ok() throws Exception {
        String token = "ABC";
    
        User user = new User();
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.when(travelBoardService.getTravelBoardsByUser(token))
                .thenReturn(new ArrayList<>());
    
        MockHttpServletRequestBuilder getRequest = get("/travelboards")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(getRequest)
                .andExpect(status().isOk());
    }

    //135
    @Test
    public void createTravelBoard_validInput_created() throws Exception {
        String token = "ABC";
    
        TravelBoardPostDTO travelBoardPostDTO = new TravelBoardPostDTO();
        travelBoardPostDTO.setName("Test Board");
        travelBoardPostDTO.setInviteCode("CRE123");
        travelBoardPostDTO.setPrivacy(PrivacyLevel.PRIVATE);
    
        User user = new User();
        user.setId(1L);
        user.setToken(token);
    
        TravelBoard createdBoard = new TravelBoard();
        createdBoard.setId(1L);
        createdBoard.setName("Test Board");
        createdBoard.setInviteCode("CRE123");
        createdBoard.setPrivacy(PrivacyLevel.PRIVATE);
        createdBoard.setOwner(user);
    
        Mockito.when(userService.validateToken(token)).thenReturn(user);
        Mockito.when(travelBoardService.createTravelBoard(Mockito.any(TravelBoard.class), Mockito.eq(token)))
                .thenReturn(createdBoard);
    
        MockHttpServletRequestBuilder postRequest = post("/travelboards")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(travelBoardPostDTO));
    
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());
    }

    //#139
    @Test
    public void getInviteCode_validBoardId_ok() throws Exception {
        Long boardId = 1L;
    
        Mockito.when(travelBoardService.getInviteCode(boardId))
                .thenReturn("CRE123");
    
        MockHttpServletRequestBuilder getRequest = get("/travelboards/{boardId}/inviteCode", boardId)
                .contentType(MediaType.APPLICATION_JSON);
    
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(content().string("CRE123"));
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
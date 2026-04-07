package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.TravelBoardService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;





@RestController
public class TravelBoardController {

	private final TravelBoardService travelBoardService;
    private final UserService userService;

	TravelBoardController(TravelBoardService travelBoardService, UserService userService) {
		this.travelBoardService = travelBoardService;
        this.userService = userService;
	}


	@PostMapping("/travelboards")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public TravelBoardGetDTO createTravelBoardGetDTO(@RequestBody TravelBoardPostDTO travelBoardPostDTO, @RequestHeader(value = "Authorization", required = false) String token) {
        userService.validateToken(token);

		TravelBoard travelBoardInput = DTOMapper.INSTANCE.convertTravelBoardPostDTOtoEntity(travelBoardPostDTO);

		TravelBoard createdTravelBoard = travelBoardService.createTravelBoard(travelBoardInput, token);

		return DTOMapper.INSTANCE.convertEntityToTravelBoardGetDTO(createdTravelBoard);
	}


    @PutMapping("/travelboards/{boardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
    public void renameTravelBoard(@PathVariable Long boardId, @RequestHeader(value = "Authorization", required = false) String token, @RequestBody TravelBoardPutDTO travelBoardPutDTO) {
        userService.validateToken(token);
        travelBoardService.renameTravelBoard(
            boardId, token, travelBoardPutDTO.getName());

    }

    @DeleteMapping("/travelboards/{boardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTravelBoard(@PathVariable Long boardId, @RequestHeader(value = "Authorization", required = false) String token) {
        userService.validateToken(token);
        travelBoardService.deleteTravelBoard(boardId, token);
    }

    @GetMapping("/travelboards/my")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<TravelBoardGetDTO> getTravelBoardsByUser(@RequestHeader(value = "Authorization", required = false) String token){
        userService.validateToken(token);
        List<TravelBoard> travelBoards = travelBoardService.getTravelBoardsByUser(token);

        List<TravelBoardGetDTO> travelBoardGetDTOs = new ArrayList<>();

        for (TravelBoard travelBoard : travelBoards) {
            travelBoardGetDTOs.add(DTOMapper.INSTANCE.convertEntityToTravelBoardGetDTO(travelBoard));
        }

    return travelBoardGetDTOs;
        
    }

    @GetMapping("/travelboards/{boardId}/inviteCode")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public String getInviteCode(@PathVariable Long boardId) {
        return travelBoardService.getInviteCode(boardId);
    }

    @PostMapping("/travelboards/join")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public void joinTravelBoardByInviteCode(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam String inviteCode) {
        userService.validateToken(token);
        travelBoardService.joinTravelBoardByInviteCode(token, inviteCode);

	}

}

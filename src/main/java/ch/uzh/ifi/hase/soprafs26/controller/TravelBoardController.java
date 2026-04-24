package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	public TravelBoardGetDTO createTravelBoard(@RequestBody TravelBoardPostDTO travelBoardPostDTO, @RequestHeader(value = "Authorization", required = false) String token) {
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

    @DeleteMapping("/travelboards/{boardId}/membership")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveTravelBoard(@PathVariable Long boardId, @RequestHeader(value = "Authorization", required = false) String token) {
        userService.validateToken(token);
        travelBoardService.leaveTravelBoard(boardId, token);
      }

    @GetMapping("/travelboards")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<TravelBoardGetDTO> getTravelBoardsByUser(@RequestHeader(value = "Authorization", required = false) String token){
        userService.validateToken(token);
        List<TravelBoard> travelBoards = travelBoardService.getTravelBoardsByUser(token);

        List<TravelBoardGetDTO> travelBoardGetDTOs = new ArrayList<>();

        for (TravelBoard travelBoard : travelBoards) {
            TravelBoardGetDTO dto = DTOMapper.INSTANCE.convertEntityToTravelBoardGetDTO(travelBoard);
            dto.setMemberIds(
                travelBoard.getMembers().stream().map(user -> user.getId()).collect(Collectors.toList())
            );
            travelBoardGetDTOs.add(dto);
        }

    return travelBoardGetDTOs;
        
    }

    @GetMapping("/travelboards/{boardId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public TravelBoardGetDTO getSingleTravelBoardById(@PathVariable Long boardId, @RequestHeader(value = "Authorization", required = false) String token){
        userService.validateToken(token);

        TravelBoard board = travelBoardService.getSingleTravelBoardById(boardId, token);

        return DTOMapper.INSTANCE.convertEntityToTravelBoardGetDTO(board); 
        
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

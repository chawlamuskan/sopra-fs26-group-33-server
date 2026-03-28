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





@RestController
public class TravelBoardController {

	private final TravelBoardService travelBoardService;

	TravelBoardController(TravelBoardService travelBoardService) {
		this.travelBoardService = travelBoardService;
	}


	@PostMapping("/travelboards")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public TravelBoardGetDTO createTravelBoardGetDTO(@RequestBody TravelBoardPostDTO travelBoardPostDTO, @RequestParam Long ownerId) {

		TravelBoard travelBoardInput = DTOMapper.INSTANCE.convertTravelBoardPostDTOtoEntity(travelBoardPostDTO);

		TravelBoard createdTravelBoard = travelBoardService.createTravelBoard(travelBoardInput, ownerId);

		return DTOMapper.INSTANCE.convertEntityToTravelBoardGetDTO(createdTravelBoard);
	}


    @PutMapping("/travelboards/{boardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
	@ResponseBody
    public void renameTravelBoard(@PathVariable Long boardId, @RequestParam Long userId, @RequestBody TravelBoardPutDTO travelBoardPutDTO) {
        travelBoardService.renameTravelBoard(
            boardId, userId, travelBoardPutDTO.getName());

    }

    @DeleteMapping("/travelboards/{boardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTravelBoard(@PathVariable Long boardId, @RequestParam Long userId) {

        travelBoardService.deleteTravelBoard(boardId, userId);
    }

    @GetMapping("/users/{userId}/travelboards")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<TravelBoardGetDTO> getTravelBoardsByUser(@PathVariable Long userId){
        List<TravelBoard> travelBoards = travelBoardService.getTravelBoardsByUser(userId);

        List<TravelBoardGetDTO> travelBoardGetDTOs = new ArrayList<>();

        for (TravelBoard travelBoard : travelBoards) {
            travelBoardGetDTOs.add(DTOMapper.INSTANCE.convertEntityToTravelBoardGetDTO(travelBoard));
        }

    return travelBoardGetDTOs;
        
    }

}

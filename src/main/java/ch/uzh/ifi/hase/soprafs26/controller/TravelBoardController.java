package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPostDTO;
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


}

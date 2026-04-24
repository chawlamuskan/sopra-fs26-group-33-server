package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPlaceGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.service.TravelBoardPlaceService;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

@RestController
public class TravelBoardPlaceController {

    private final TravelBoardPlaceService travelBoardPlaceService;
    private final UserService userService;

    TravelBoardPlaceController(TravelBoardPlaceService travelBoardPlaceService, UserService userService) {
        this.travelBoardPlaceService = travelBoardPlaceService;
        this.userService = userService;
    }

    @PostMapping("/travelboards/{boardId}/places")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public TravelBoardPlaceGetDTO createTravelBoardPlace(
        @PathVariable Long boardId,
        @RequestBody TravelBoardPlacePostDTO travelBoardPlacePostDTO,
        @RequestHeader(value = "Authorization", required = false) String token) {
        
        userService.validateToken(token);
        TravelBoardPlace travelBoardPlace = DTOMapper.INSTANCE.convertTravelBoardPlacePostDTOToEntity(travelBoardPlacePostDTO);

        TravelBoardPlace createdTravelBoardPlace = travelBoardPlaceService.saveToBoard(boardId, travelBoardPlace);

        return DTOMapper.INSTANCE.convertEntityToTravelBoardPlaceGetDTO(createdTravelBoardPlace);
        
        }
    
    @GetMapping("/travelboards/{boardId}/places")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<TravelBoardPlaceGetDTO> getPlacesByBoard (
        @PathVariable Long boardId,
        @RequestHeader (value = "Authorization", required = false) String token
    ) {
        userService.validateToken(token);
        List<TravelBoardPlace> travelBoardPlaces = travelBoardPlaceService.getPlacesByBoard(boardId);
        List<TravelBoardPlaceGetDTO> travelBoardPlaceGetDTOs = new ArrayList<>();

        for (TravelBoardPlace travelBoardPlace : travelBoardPlaces) {
            TravelBoardPlaceGetDTO dto = DTOMapper.INSTANCE.convertEntityToTravelBoardPlaceGetDTO(travelBoardPlace);
            travelBoardPlaceGetDTOs.add(dto);
            
        }
        return travelBoardPlaceGetDTOs;
    }
    
    
}

package ch.uzh.ifi.hase.soprafs26.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlaceGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import ch.uzh.ifi.hase.soprafs26.service.SavedPlaceService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;


@RestController
public class SavedPlaceController {

    private final SavedPlaceService savedPlaceService;
    private final UserService userService;

    SavedPlaceController(SavedPlaceService savedPlaceService, UserService userService) {
        this.savedPlaceService = savedPlaceService;
        this.userService = userService;
        
    }

    @PostMapping("/users/{userId}/savedplaces")
    @ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
    public SavedPlaceGetDTO createSavedPlace(
        @PathVariable Long userId, 
        @RequestBody SavedPlacePostDTO savedPlacePostDTO, 
        @RequestHeader(value = "Authorization", required = false) String token) {
        
        // check if the user is logged in and the token belongs to the user whose saved place is being set
        User loggedInUser = userService.validateToken(token);
		if (!loggedInUser.getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
				"You are not allowed to set other users' saved places.");
		}
        
        SavedPlace savedPlace = DTOMapper.INSTANCE.convertSavedPlacePostDTOToEntity(savedPlacePostDTO);

        SavedPlace createdSavedPlace = savedPlaceService.saveToUser(userId, savedPlace);

        return DTOMapper.INSTANCE.convertEntityToSavedPlaceGetDTO(createdSavedPlace);
    }
    
    @GetMapping("/users/{userId}/savedplaces")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<SavedPlaceGetDTO> getSavedPlacesByUser (
        @PathVariable Long userId,
        @RequestHeader (value = "Authorization", required = false) String token
    ) {
        userService.validateToken(token);
        List<SavedPlace> savedPlaces = savedPlaceService.getSavedPlacesByUser(userId);
        List<SavedPlaceGetDTO> savedPlaceGetDTOs = new ArrayList<>();

        for (SavedPlace savedPlace : savedPlaces) {
            SavedPlaceGetDTO dto = DTOMapper.INSTANCE.convertEntityToSavedPlaceGetDTO(savedPlace);
            savedPlaceGetDTOs.add(dto);
            
        }
        return savedPlaceGetDTOs;
    }

    @DeleteMapping("/users/{userId}/savedplaces/{savedPlaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSavedPlace(
        @PathVariable Long savedPlaceId,
        @RequestHeader (value = "Authorization", required = false) String token
    ){
        userService.validateToken(token);
        savedPlaceService.deleteSavedPlace(savedPlaceId, token);
    }
    
}

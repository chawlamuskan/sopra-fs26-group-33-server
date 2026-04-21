package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PreferencesPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PreferencesGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.service.PreferencesService;

/**
 * Preferences Controller
 * This class is responsible for handling all REST request that are related to
 * the user preferences.
 * The controller will receive the request and delegate the execution to the
 * PreferencesService and finally return the result.
 */

@RestController
public class PreferencesController {

	private final PreferencesService preferencesService;
    private final UserService userService;

	PreferencesController(PreferencesService preferencesService, UserService userService) {
		this.preferencesService = preferencesService;
		this.userService = userService;
	}

    // Create User Preferences
    @PostMapping("/users/{userId}/preferences")
	@ResponseStatus(HttpStatus.CREATED) // POST /users/{userId}/preferences - 201 CREATED based on REST
	@ResponseBody
	public PreferencesGetDTO savePreferences(
        @PathVariable Long userId,
        @RequestHeader(value = "Authorization", required = false) String token,
        @RequestBody PreferencesPostDTO preferencesPostDTO) {
        
		// check if the user is logged in and the token belongs to the user whose preferences are being set
        User loggedInUser = userService.validateToken(token);
		if (!loggedInUser.getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
				"You are not allowed to set other users' preferences.");
		}

		// create Preferences entity from DTO
		Preferences preferences = DTOMapper.INSTANCE.convertPreferencesPostDTOtoEntity(preferencesPostDTO);
		// save preferences
        Preferences savedPreferences = preferencesService.savePreferences(userId, preferences);
        
		return DTOMapper.INSTANCE.convertEntityToPreferencesGetDTO(savedPreferences);
    }

    // Get User Preferences
    @GetMapping("/users/{userId}/preferences")
	@ResponseStatus(HttpStatus.OK)  // GET /users/{userId}/preferences - 200 OK based on REST
	@ResponseBody
	public PreferencesGetDTO getPreferences(
		@PathVariable Long userId,
		@RequestHeader(value = "Authorization", required = false) String token) {
		
		userService.validateToken(token);

		Preferences preferences = preferencesService.getPreferences(userId);
		return DTOMapper.INSTANCE.convertEntityToPreferencesGetDTO(preferences);
	}

	// Update User Preferences
	@PutMapping("/users/{id}/preferences")
	@ResponseStatus(HttpStatus.NO_CONTENT) // PUT /users/{id}/preferences -> 204 NO CONTENT based on REST
	@ResponseBody
	public void updatePreferences(
		@PathVariable Long id,
        @RequestHeader(value = "Authorization", required = false) String token,
		@RequestBody PreferencesPostDTO preferencesPostDTO) {

		userService.validateToken(token);

		Preferences preferences = DTOMapper.INSTANCE.convertPreferencesPostDTOtoEntity(preferencesPostDTO);

		preferencesService.savePreferences(id, preferences);
	}

}

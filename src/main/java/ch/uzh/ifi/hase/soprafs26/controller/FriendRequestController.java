package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.FriendRequestService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;





@RestController
public class FriendRequestController {

	private final FriendRequestService friendRequestService;
	private final UserService userService;

	FriendRequestController(FriendRequestService friendRequestService, UserService userService) {
		this.friendRequestService = friendRequestService;
		this.userService = userService;
	}


	@PostMapping("/friendRequest")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public FriendRequestGetDTO sendFriendRequest(@RequestBody FriendRequestPostDTO friendRequestPostDTO, @RequestHeader(value = "Authorization", required = false) String token) {
		userService.validateToken(token);

		FriendRequest createdRequest = friendRequestService.sendFriendRequest(token, friendRequestPostDTO.getReceiverId());

		return DTOMapper.INSTANCE.convertEntityToFriendRequestGetDTO(createdRequest);
	}


}
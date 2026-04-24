//package ch.uzh.ifi.hase.soprafs26.controller;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//
//import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
//import ch.uzh.ifi.hase.soprafs26.entity.User;
//import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestGetDTO;
//import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestPostDTO;
//import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
//import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
//import ch.uzh.ifi.hase.soprafs26.service.FriendRequestService;
//import ch.uzh.ifi.hase.soprafs26.service.UserService;
//
//
//
//
//
//@RestController
//public class FriendRequestController {
//
//	private final FriendRequestService friendRequestService;
//	private final UserService userService;
//
//	FriendRequestController(FriendRequestService friendRequestService, UserService userService) {
//		this.friendRequestService = friendRequestService;
//		this.userService = userService;
//	}
//
//
//	@PostMapping("/friendRequests")
//	@ResponseStatus(HttpStatus.CREATED)
//	@ResponseBody
//	public FriendRequestGetDTO sendFriendRequest(@RequestBody FriendRequestPostDTO friendRequestPostDTO, @RequestHeader(value = "Authorization", required = false) String token) {
//		userService.validateToken(token);
//
//		FriendRequest createdRequest = friendRequestService.sendFriendRequest(token, friendRequestPostDTO.getReceiverId());
//
//		return DTOMapper.INSTANCE.convertEntityToFriendRequestGetDTO(createdRequest);
//	}
//
//	@GetMapping("/friendRequests")
//	@ResponseStatus(HttpStatus.OK)
//	@ResponseBody
//	public List<FriendRequestGetDTO> getPendingFriendRequests(@RequestHeader(value = "Authorization", required = false) String token) {
//		userService.validateToken(token);
//
//		List<FriendRequest> pendingFriendRequests = friendRequestService.getPendingFriendRequests(token);
//
//		List<FriendRequestGetDTO> friendRequestGetDTOs = new ArrayList<>();
//
//		for (FriendRequest friendRequest : pendingFriendRequests) {
//            friendRequestGetDTOs.add(DTOMapper.INSTANCE.convertEntityToFriendRequestGetDTO(friendRequest));
//        }
//		return friendRequestGetDTOs;
//	}
//
//	@PutMapping("/friendRequests/{friendRequestId}/accept")
//	@ResponseStatus(HttpStatus.NO_CONTENT)
//	@ResponseBody
//	public void acceptFriendRequest(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable Long friendRequestId) {
//		userService.validateToken(token);
//
//		friendRequestService.acceptFriendRequest(friendRequestId, token);
//	}	
//
//	@PutMapping("/friendRequests/{friendRequestId}/decline")
//	@ResponseStatus(HttpStatus.NO_CONTENT)
//	@ResponseBody
//	public void declineFriendRequest(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable Long friendRequestId) {
//		userService.validateToken(token);
//
//		friendRequestService.declineFriendRequest(friendRequestId, token);
//	}
//
//	@GetMapping("/friends")
//	@ResponseStatus(HttpStatus.OK)
//	@ResponseBody
//	public List<UserGetDTO> getFriends(@RequestHeader(value = "Authorization", required = false) String token) {
//		userService.validateToken(token);
//
//		List<User> friends = friendRequestService.getFriends(token);
//
//		List<UserGetDTO> friendsDTOs = new ArrayList<>();
//
//		for (User friend : friends) {
//            friendsDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(friend));
//        }
//		return friendsDTOs;
//	}
//
//	@DeleteMapping("/friends/{friendId}")
//	@ResponseStatus(HttpStatus.NO_CONTENT)
//	public void removeFriend(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable Long friendId) {
//	    userService.validateToken(token);
//	    friendRequestService.removeFriend(token, friendId);
//	}
//
//
//}
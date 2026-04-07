package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InvitationGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InvitationPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.InvitationService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;





@RestController
public class InvitationController {

	private final InvitationService invitationService;
	private final UserService userService;

	InvitationController(InvitationService invitationService, UserService userService) {
		this.invitationService = invitationService;
		this.userService = userService;
	}


	@PostMapping("/travelboards/{boardId}/invitations")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public InvitationGetDTO createInvitationGetDTO(@RequestBody InvitationPostDTO invitationPostDTO, @RequestHeader(value = "Authorization", required = false) String token, @PathVariable Long boardId) {
		userService.validateToken(token);

		Invitation createdInvitation = invitationService.createInvitation(boardId, token, invitationPostDTO.getReceiverId());

		return DTOMapper.INSTANCE.convertEntityToInvitationGetDTO(createdInvitation);
	}


}
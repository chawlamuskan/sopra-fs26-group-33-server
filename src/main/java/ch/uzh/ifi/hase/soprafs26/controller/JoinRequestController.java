package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JoinRequestGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.JoinRequestService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class JoinRequestController {

    private final JoinRequestService joinRequestService;
    private final UserService userService;

    JoinRequestController(JoinRequestService joinRequestService, UserService userService) {
        this.joinRequestService = joinRequestService;
        this.userService = userService;
    }

    // POST /joinRequests/{boardId} - send a join request to a board
    @PostMapping("/joinRequests/{boardId}")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public JoinRequestGetDTO sendJoinRequest(
            @PathVariable Long boardId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        userService.validateToken(token);
        JoinRequest created = joinRequestService.sendJoinRequest(token, boardId);
        return DTOMapper.INSTANCE.convertEntityToJoinRequestGetDTO(created);
    }

    // GET /joinRequests - get pending join requests for boards I own
    @GetMapping("/joinRequests")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<JoinRequestGetDTO> getPendingJoinRequests(
            @RequestHeader(value = "Authorization", required = false) String token) {
        userService.validateToken(token);
        List<JoinRequest> requests = joinRequestService.getPendingJoinRequests(token);
        List<JoinRequestGetDTO> dtos = new ArrayList<>();
        for (JoinRequest r : requests) {
            dtos.add(DTOMapper.INSTANCE.convertEntityToJoinRequestGetDTO(r));
        }
        return dtos;
    }

    // PUT /joinRequests/{id}/accept - accept a join request
    @PutMapping("/joinRequests/{joinRequestId}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptJoinRequest(
            @PathVariable Long joinRequestId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        userService.validateToken(token);
        joinRequestService.acceptJoinRequest(joinRequestId, token);
    }

    // PUT /joinRequests/{id}/decline - decline a join request
    @PutMapping("/joinRequests/{joinRequestId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineJoinRequest(
            @PathVariable Long joinRequestId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        userService.validateToken(token);
        joinRequestService.declineJoinRequest(joinRequestId, token);
    }
}
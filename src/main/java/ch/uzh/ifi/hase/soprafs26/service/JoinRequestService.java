package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.JoinRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.JoinRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final TravelBoardRepository travelBoardRepository;
    private final UserRepository userRepository;

    public JoinRequestService(
            @Qualifier("joinRequestRepository") JoinRequestRepository joinRequestRepository,
            @Qualifier("travelBoardRepository") TravelBoardRepository travelBoardRepository,
            @Qualifier("userRepository") UserRepository userRepository) {
        this.joinRequestRepository = joinRequestRepository;
        this.travelBoardRepository = travelBoardRepository;
        this.userRepository = userRepository;
    }

    // send a join request to a board
    public JoinRequest sendJoinRequest(String token, Long boardId) {
        User sender = userRepository.findByToken(token);
        if (sender == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        TravelBoard board = travelBoardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Board not found"));

        // check if already a member or owner
        boolean isOwner = board.getOwner().getId().equals(sender.getId());
        boolean isMember = board.getMembers().stream()
                .anyMatch(m -> m.getId().equals(sender.getId()));

        if (isOwner || isMember) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You are already a member of this board");
        }

        // check for existing pending request
        JoinRequest existing = joinRequestRepository.findBySenderAndBoardAndStatus(
                sender, board, JoinRequestStatus.PENDING);
        if (existing != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You already have a pending join request for this board");
        }

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setSender(sender);
        joinRequest.setBoard(board);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        return joinRequestRepository.save(joinRequest);
    }

    // get all pending join requests for boards owned by the logged-in user
    public List<JoinRequest> getPendingJoinRequests(String token) {
        User owner = userRepository.findByToken(token);
        if (owner == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
        return joinRequestRepository.findByBoardOwnerAndStatus(owner, JoinRequestStatus.PENDING);
    }

    // accept a join request — add user to board members
    public void acceptJoinRequest(Long joinRequestId, String token) {
        JoinRequest joinRequest = joinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Join request not found"));

        User owner = userRepository.findByToken(token);

        // only board owner can accept
        if (!joinRequest.getBoard().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Only the board owner can accept join requests");
        }

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Join request was already answered");
        }

        // add sender to board members
        TravelBoard board = joinRequest.getBoard();
        User sender = joinRequest.getSender();

        if (!board.getMembers().contains(sender)) {
            board.getMembers().add(sender);
            travelBoardRepository.save(board);
        }

        joinRequest.setStatus(JoinRequestStatus.ACCEPTED);
        joinRequestRepository.save(joinRequest);
    }

    // decline a join request
    public void declineJoinRequest(Long joinRequestId, String token) {
        JoinRequest joinRequest = joinRequestRepository.findById(joinRequestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Join request not found"));

        User owner = userRepository.findByToken(token);

        if (!joinRequest.getBoard().getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Only the board owner can decline join requests");
        }

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Join request was already answered");
        }

        joinRequest.setStatus(JoinRequestStatus.DECLINED);
        joinRequestRepository.save(joinRequest);
    }
}
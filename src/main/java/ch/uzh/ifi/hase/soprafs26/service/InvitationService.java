package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;



@Service
@Transactional
public class InvitationService {

	private final Logger log = LoggerFactory.getLogger(InvitationService.class);
    
    private final InvitationRepository invitationRepository;
	private final TravelBoardRepository travelBoardRepository;
    private final UserRepository userRepository;

    public InvitationService(@Qualifier("travelBoardRepository") TravelBoardRepository travelBoardRepository, InvitationRepository invitationRepository, UserRepository userRepository) {
		this.travelBoardRepository = travelBoardRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
	}

	public List<Invitation> getInvitations() {
		return this.invitationRepository.findAll();
	}

	public Invitation createInvitation(Long boardId, String token, Long receiverId) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        User sender = userRepository.findByToken(token);
        if (sender == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"); 
        }

        if (!board.getOwner().getId().equals(sender.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - you must be owner");
        }

        if (receiverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver id must not be null");
        }
        
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));

        Invitation newInvitation = new Invitation();
        newInvitation.setStatus(InviteStatus.PENDING);
        newInvitation.setBoard(board);
        newInvitation.setSender(sender);
        newInvitation.setReceiver(receiver);

        newInvitation = invitationRepository.save(newInvitation);

        log.debug("Created Information for Invitation: {}", newInvitation);
        return newInvitation;
	}

}

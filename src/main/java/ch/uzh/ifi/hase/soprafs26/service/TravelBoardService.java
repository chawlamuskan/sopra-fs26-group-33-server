package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class TravelBoardService {

	private final Logger log = LoggerFactory.getLogger(TravelBoardService.class);

	private final TravelBoardRepository travelBoardRepository;
    private final UserRepository userRepository;

	public TravelBoardService(@Qualifier("travelBoardRepository") TravelBoardRepository travelBoardRepository, UserRepository userRepository) {
		this.travelBoardRepository = travelBoardRepository;
        this.userRepository = userRepository;
	}

	public List<TravelBoard> getTravelBoards() {
		return this.travelBoardRepository.findAll();
	}

	public TravelBoard createTravelBoard(TravelBoard newTravelBoard, Long ownerId) {
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));
        
        if (newTravelBoard.getName() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board name cannot be empty");
        }
        if (newTravelBoard.getPrivacy() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Privacy level is required");
        }
        if (newTravelBoard.getStartDate() != null && newTravelBoard.getEndDate() != null
            && newTravelBoard.getStartDate().isAfter(newTravelBoard.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be after end date");
            }

        newTravelBoard.setOwner(owner);
        newTravelBoard.setInviteCode(UUID.randomUUID().toString().substring(0, 8));
        newTravelBoard.setDateCreated(LocalDate.now());


        newTravelBoard = travelBoardRepository.save(newTravelBoard);

        log.debug("Created Information for TravelBoard: {}", newTravelBoard);
        return newTravelBoard;
	}

    public void renameTravelBoard(Long boardId, Long userId, String newName){
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - you must be owner");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board name cannot be empty");
        }

        board.setName(newName.trim());
        board = travelBoardRepository.save(board);

    }


    public void deleteTravelBoard(Long boardId, Long userId){
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - you must be owner");
        }

        travelBoardRepository.delete(board);
    }

    public List<TravelBoard> getTravelBoardsByUser(Long userId) {
        List<TravelBoard> ownerBoards = travelBoardRepository.findByOwnerId(userId);
        List<TravelBoard> memberBoards = travelBoardRepository.findByMembersId(userId);

        List<TravelBoard> result = new ArrayList<>();

        result.addAll(ownerBoards);

        for (TravelBoard board : memberBoards) {
            if (!result.contains(board)) {  
                result.add(board);
            }
        }

        return result;
    }

    public String getInviteCode(Long boardId) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));
        
        return board.getInviteCode();
    }

    public void joinTravelBoardByInviteCode(Long userId, String inviteCode){
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        TravelBoard board = travelBoardRepository.findByInviteCode(inviteCode);

        if (board == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite code is invalid");
        }

        if (board.getMembers().contains(user) || board.getOwner().getId().equals(userId)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a member of this board");
        }

        board.getMembers().add(user);
        travelBoardRepository.save(board);        
    }
}

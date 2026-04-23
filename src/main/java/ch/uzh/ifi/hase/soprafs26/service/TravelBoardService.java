package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Place;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PlaceRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class TravelBoardService {

	private final Logger log = LoggerFactory.getLogger(TravelBoardService.class);

	private final TravelBoardRepository travelBoardRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;

	public TravelBoardService(@Qualifier("travelBoardRepository") TravelBoardRepository travelBoardRepository, UserRepository userRepository, PlaceRepository placeRepository) {
		this.travelBoardRepository = travelBoardRepository;
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
	}

	public List<TravelBoard> getTravelBoards() {
		return this.travelBoardRepository.findAll();
	}

	public TravelBoard createTravelBoard(TravelBoard newTravelBoard, String token) {
        User owner = userRepository.findByToken(token);

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

        String inviteCode = newTravelBoard.getInviteCode();
        if (inviteCode != null && !inviteCode.trim().isEmpty()) {
            inviteCode = inviteCode.trim().toUpperCase();
            if (travelBoardRepository.findByInviteCode(inviteCode) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Invite code already exists");
            }
            newTravelBoard.setInviteCode(inviteCode);
        }
        else {
            newTravelBoard.setInviteCode(null);
        }

        String location = newTravelBoard.getLocation();
        if (location != null && location.trim().isEmpty()) {
            newTravelBoard.setLocation(null);
        }

        newTravelBoard.setOwner(owner);
        newTravelBoard.setDateCreated(LocalDate.now());


        newTravelBoard = travelBoardRepository.save(newTravelBoard);

        log.debug("Created Information for TravelBoard: {}", newTravelBoard);
        return newTravelBoard;
	}

    public void renameTravelBoard(Long boardId, String token, String newName){
        User user = userRepository.findByToken(token);
        Long userId = user.getId();
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


    public void deleteTravelBoard(Long boardId, String token){
        User user = userRepository.findByToken(token);
        Long userId = user.getId();
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        if (!board.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - you must be owner");
        }

        travelBoardRepository.delete(board);
    }

    public void leaveTravelBoard(Long boardId, String token) {
        User user = userRepository.findByToken(token);

        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        if (board.getOwner().getId().equals(user.getId())) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner cannot leave the board with this action");
        }

        if (!board.getMembers().contains(user)) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User is not a member of this travel board");
        }

        board.getMembers().remove(user);
        travelBoardRepository.save(board);
    }

    public List<TravelBoard> getTravelBoardsByUser(String token) {
        User user = userRepository.findByToken(token);
        Long userId = user.getId();
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

    public void joinTravelBoardByInviteCode(String token, String inviteCode){
        User user = userRepository.findByToken(token);
        Long userId = user.getId();

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

    public void addPlaces(Long boardId, String token, Place newPlace) {
        User user = userRepository.findByToken(token);
        Long userId = user.getId();

        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        if (!board.getOwner().getId().equals(userId) && !(board.getMembers().contains(user))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only board members can add places");
        }

        if (newPlace.getLatitude() == null || newPlace.getLongitude() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Latitude and longitude are required");
        }

        Place savedPlace = placeRepository.save(newPlace);
        board.getPlaces().add(savedPlace);
        travelBoardRepository.save(board);

    }   
}

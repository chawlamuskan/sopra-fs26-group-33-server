package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardPlaceRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
public class TravelBoardPlaceService {

    private final TravelBoardPlaceRepository travelBoardPlaceRepository;
    private final TravelBoardRepository travelBoardRepository;
    private final UserRepository userRepository;

    public TravelBoardPlaceService(
        @Qualifier ("travelBoardPlaceRepository") TravelBoardPlaceRepository travelBoardPlaceRepository,
        @Qualifier ("travelBoardRepository") TravelBoardRepository travelBoardRepository,
        @Qualifier ("userRepository") UserRepository userRepository) {
            this.travelBoardPlaceRepository = travelBoardPlaceRepository;
            this.travelBoardRepository = travelBoardRepository;
            this.userRepository = userRepository;
            
        }
    

    // add a place to a travel board

    public TravelBoardPlace saveToBoard(Long boardId, TravelBoardPlace newTravelBoardPlace, User user) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        // check if the user is a member of the board
        if (!board.getOwner().getId().equals(user.getId()) && !(board.getMembers().contains(user))) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only board members can add places");
         }
        checkIfPlaceAlreadySaved(newTravelBoardPlace, board);

        newTravelBoardPlace.setBoard(board);
        newTravelBoardPlace.setUser(user); 
        
        return travelBoardPlaceRepository.save(newTravelBoardPlace);
    }

    // get all saved places of a travel board 
    public List<TravelBoardPlace> getPlacesByBoard (Long boardId) {
        TravelBoard board = travelBoardRepository.findById(boardId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        return travelBoardPlaceRepository.findAllByBoard(board);
    }

    // remove a place from a travel board
    public void deletePlaceFromBoard(Long travelBoardPlaceId, String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        TravelBoardPlace travelBoardPlace = travelBoardPlaceRepository.findById(travelBoardPlaceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board place not found"));

        TravelBoard board = travelBoardPlace.getBoard();
         if (!board.getMembers().contains(user)) {
          throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a member of this travel board");
        }
        
        travelBoardPlaceRepository.delete(travelBoardPlace);
    }


    // check if a place has already been saved to a travel board
    private void checkIfPlaceAlreadySaved(TravelBoardPlace place, TravelBoard board) {
        boolean alreadySaved = travelBoardPlaceRepository.existsByExternalPlaceIdAndBoard(place.getExternalPlaceId(), board);
        
        if (alreadySaved) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already saved to this travel board");
        } 
    }
    
}

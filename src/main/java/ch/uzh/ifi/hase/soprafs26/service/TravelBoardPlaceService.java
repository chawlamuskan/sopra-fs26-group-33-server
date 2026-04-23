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

@Service
public class TravelBoardPlaceService {

    private final TravelBoardPlaceRepository travelBoardPlaceRepository;
    private final TravelBoardRepository travelBoardRepository;

    public TravelBoardPlaceService(
        @Qualifier ("travelBoardPlaceRepository") TravelBoardPlaceRepository travelBoardPlaceRepository,
        @Qualifier ("travelBoardRepository") TravelBoardRepository travelBoardRepository) {
            this.travelBoardPlaceRepository = travelBoardPlaceRepository;
            this.travelBoardRepository = travelBoardRepository;
        }
    

    // add a place to a travel board

    public TravelBoardPlace saveToBoard(Long boardId, TravelBoardPlace newTravelBoardPlace) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        checkIfPlaceAlreadySaved(newTravelBoardPlace, board);

        newTravelBoardPlace.setBoard(board);
        
        return travelBoardPlaceRepository.save(newTravelBoardPlace);
    }

    // get all saved places of a travel board 
    public List<TravelBoardPlace> getPlacesByBoard (Long boardId) {
        TravelBoard board = travelBoardRepository.findById(boardId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        return travelBoardPlaceRepository.findAllByBoard(board);
    }

    // check if a place has already been saved to a travel board
    private void checkIfPlaceAlreadySaved(TravelBoardPlace place, TravelBoard board) {
        boolean alreadySaved = travelBoardPlaceRepository.existsByExternalPlaceIdAndBoard(place.getExternalPlaceId(), board);
        
        if (alreadySaved) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already saved to this travel board");
        } 
    }
    
}

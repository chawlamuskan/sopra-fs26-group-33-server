package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.repository.SavedPlaceRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;

@Service
public class SavedPlaceService {

    private final SavedPlaceRepository savedPlaceRepository;
    private final UserRepository userRepository;
    private final TravelBoardRepository travelBoardRepository;

    public SavedPlaceService(
        @Qualifier ("savedPlaceRepository") SavedPlaceRepository savedPlaceRepository,
        @Qualifier ("userRepository") UserRepository userRepository,
        @Qualifier ("travelBoardRepository") TravelBoardRepository travelBoardRepository) {
        this.savedPlaceRepository = savedPlaceRepository;
        this.userRepository = userRepository;
        this.travelBoardRepository = travelBoardRepository;
    }

    // add a place to user
    public SavedPlace saveToUser(Long userId, SavedPlace newSavedPlace) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        checkIfPlaceAlreadySaved(newSavedPlace, user, null);

        newSavedPlace.setUser(user);
        
        return savedPlaceRepository.save(newSavedPlace);
    }

    // add a place to a travel board

    public SavedPlace saveToBoard(Long boardId, SavedPlace newSavedPlace) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        checkIfPlaceAlreadySaved(newSavedPlace, null, board);

        newSavedPlace.setBoard(board);
        
        return savedPlaceRepository.save(newSavedPlace);
    }


    // get all saved places of a user 
    public List<SavedPlace> getSavedPlacesByUser (Long userId) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return savedPlaceRepository.findAllByUser(user);
    }


    // get all saved places of a travel board 
    public List<SavedPlace> getSavedPlacesByBoard (Long boardId) {
        TravelBoard board = travelBoardRepository.findById(boardId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        return savedPlaceRepository.findAllByBoard(board);
    }

    // check if a place has already been saved to a travel board or user
    private void checkIfPlaceAlreadySaved(SavedPlace savedPlace, User user, TravelBoard board) {
        SavedPlace savedByUser = savedPlaceRepository.findByNameAndAddressAndUser(savedPlace.getName(), savedPlace.getAddress(), user);
        SavedPlace savedByBoard = savedPlaceRepository.findByNameAndAddressAndBoard(savedPlace.getName(), savedPlace.getAddress(), board);
        
        if (savedByUser != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already saved to this user");
        } else if (savedByBoard != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already saved to this board");
        }
    }


    
}

package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.repository.SavedPlaceRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

@Service
public class SavedPlaceService {

    private final SavedPlaceRepository savedPlaceRepository;
    private final UserRepository userRepository;

    public SavedPlaceService(
        @Qualifier ("savedPlaceRepository") SavedPlaceRepository savedPlaceRepository,
        @Qualifier ("userRepository") UserRepository userRepository) {
        this.savedPlaceRepository = savedPlaceRepository;
        this.userRepository = userRepository;
       
    }

    // add a place to user
    public SavedPlace saveToUser(Long userId, SavedPlace newSavedPlace) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        checkIfPlaceAlreadySaved(newSavedPlace, user);

        newSavedPlace.setUser(user);
        
        return savedPlaceRepository.save(newSavedPlace);
    }



    // get all saved places of a user 
    public List<SavedPlace> getSavedPlacesByUser (Long userId) {
        User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return savedPlaceRepository.findAllByUser(user);
    }

    // remove a place from saved places
    public void deleteSavedPlace(Long SavedPlaceId, String token) {
        User user = userRepository.findByToken(token);
        SavedPlace savedPlace = savedPlaceRepository.findById(SavedPlaceId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved place not found"));

        if (!savedPlace.getUser().equals(user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - you can only delete your own saved places");
        }
        
        savedPlaceRepository.delete(savedPlace);
    }



    // check if a place has already been saved to a user
    private void checkIfPlaceAlreadySaved(SavedPlace savedPlace, User user) {
        boolean alreadySaved = savedPlaceRepository.existsByExternalPlaceIdAndUser(savedPlace.getExternalPlaceId(), user);
        
        if (alreadySaved) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already saved to this user");
        } 
    }

    
}

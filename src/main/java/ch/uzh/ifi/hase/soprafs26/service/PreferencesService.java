package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;

/**
 * Preferences Service
 * This class is the "worker" and responsible for all functionality related to the user preferences
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back to the caller.
 */

@Service
@Transactional
public class PreferencesService {

	private final UserRepository userRepository;
	private final PreferencesRepository preferencesRepository;

	public PreferencesService(
        @Qualifier("userRepository") UserRepository userRepository, 
        @Qualifier("preferencesRepository") PreferencesRepository preferencesRepository) {
		
        this.userRepository = userRepository;
		this.preferencesRepository = preferencesRepository;
	}

	// Save/Update user Preferences
    public Preferences savePreferences(Long userId, Preferences newPreferences) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        
        Preferences existing = preferencesRepository.findByUser(user);

        if (existing == null) {
            newPreferences.setUser(user);
            return preferencesRepository.save(newPreferences);
        }

        if (newPreferences.getBio() != null) {
            existing.setBio(newPreferences.getBio());
        }
        if (newPreferences.getProfilePicture() != null) {
            existing.setProfilePicture(newPreferences.getProfilePicture());
        }
        if (newPreferences.getVisitedCountries() != null) {
            existing.setVisitedCountries(newPreferences.getVisitedCountries());
        }
        if (newPreferences.getWishlistCountries() != null) {
            existing.setWishlistCountries(newPreferences.getWishlistCountries());
        }
        if (newPreferences.getFriends() != null) {
            existing.setFriends(newPreferences.getFriends());
        }
        return preferencesRepository.save(existing);

    }
	
	// retrieve User Preferences by userId
    public Preferences getPreferences(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found"));

        Preferences preferences = preferencesRepository.findByUser(user);
        if (preferences == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Preferences not found for this user");
        }
        return preferences;
    }
	

}

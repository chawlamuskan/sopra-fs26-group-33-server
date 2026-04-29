package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;


@DataJpaTest
public class SavedPlaceRepositoryIntegrationTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SavedPlaceRepository savedPlaceRepository;

    // helper to create and persist a user
    private User persistUser() {
        User user = new User();
        user.setName("Firstname Lastname");
        user.setUsername("firstname@lastname");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.OFFLINE);
        user.setToken("1");
        user.setCreationDate(java.time.LocalDate.now());
        user.setPassword("Password123!");

        entityManager.persist(user);
        entityManager.flush();
        return user;

    }

    // helper to create and persist saved place for a user
    private SavedPlace persistSavedPlace(String externalPlaceId, String name, String address, Double rating, String photoReference, Double lat, Double lng, Set<String> types, User user) {
        SavedPlace savedPlace = new SavedPlace();
        savedPlace.setExternalPlaceId(externalPlaceId);
        savedPlace.setName(name);
        savedPlace.setAddress(address);
        savedPlace.setRating(rating);
        savedPlace.setPhotoReference(photoReference);
        savedPlace.setLat(lat);
        savedPlace.setLng(lng);
        savedPlace.setTypes(types);
        savedPlace.setUser(user);

        entityManager.persist(savedPlace);
        entityManager.flush();
        return savedPlace;
    }

    @Test
    public void findAllByUser_success() {
        // GIVEN a user with two saved places
        User user = persistUser();
        persistSavedPlace("1234", "Eiffel Tower", "Rue de Eiffel, 3000 Paris", 1.0, "123", 1.0, 1.0, null, user);
        persistSavedPlace("4321", "Akropolis", "Akropolis Street, 150 Athens", 1.0, "123", 1.0, 1.0, null, user);


        // WHEN fetching all places for that user
        List<SavedPlace> found = savedPlaceRepository.findAllByUser(user);

        // THEN both saved places are returned
        assertEquals(2, found.size());
    }


    @Test
    public void existsByExternalPlaceIdAndUser_placeExists_returnsTrue() {
        // GIVEN a user with an already saved place
        User user = persistUser();
        persistSavedPlace("9876", "Eiffel Tower", "Rue de Eiffel, 3000 Paris", null, null, null, null, null, user);

        // WHEN checking if that place already exists for that user
        boolean exists = savedPlaceRepository.existsByExternalPlaceIdAndUser("9876", user);

        // THEN it returns true (duplicate detected)
        assertTrue(exists);
    }

    @Test
    public void existsByExternalPlaceIdAndUser_placeDoesNotExist_returnsFalse() {
        // GIVEN a user with no saved places
        User user = persistUser();

        // WHEN checking if a place exists for that user
        boolean exists = savedPlaceRepository.existsByExternalPlaceIdAndUser("place-1", user);

        // THEN it returns false (no duplicate)
        assertFalse(exists);
    }


}

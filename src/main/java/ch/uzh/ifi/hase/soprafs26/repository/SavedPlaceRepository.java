package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;


@Repository("savedPlaceRepository")
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findAllByUser (User user);
    List<SavedPlace> findAllByBoard (TravelBoard board);
    SavedPlace findByNameAndAddressAndUser(String name, String address, User user);
    SavedPlace findByNameAndAddressAndBoard(String name, String address, TravelBoard board);
}

package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;

@Repository("travelBoardPlaceRepository")
public interface TravelBoardPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findAllByBoard (TravelBoard board);
    boolean existsByExternalPlaceIdAndBaord(String externalPlaceId, TravelBoard board);
}
    


package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;

@Repository("travelBoardPlaceRepository")
public interface TravelBoardPlaceRepository extends JpaRepository<TravelBoardPlace, Long> {
    List<TravelBoardPlace> findAllByBoard (TravelBoard board);
    boolean existsByExternalPlaceIdAndBoard(String externalPlaceId, TravelBoard board);
}
    


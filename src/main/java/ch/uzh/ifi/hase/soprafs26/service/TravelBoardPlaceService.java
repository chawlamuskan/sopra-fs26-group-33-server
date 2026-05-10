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

@Service
public class TravelBoardPlaceService {

    private final TravelBoardPlaceRepository travelBoardPlaceRepository;
    private final TravelBoardRepository travelBoardRepository;
    private final GeocodingService geocodingService;
    private final ActivityLogService activityLogService;

    public TravelBoardPlaceService(
        @Qualifier("travelBoardPlaceRepository") TravelBoardPlaceRepository travelBoardPlaceRepository,
        @Qualifier("travelBoardRepository") TravelBoardRepository travelBoardRepository,
        GeocodingService geocodingService,
        ActivityLogService activityLogService) {
            this.travelBoardPlaceRepository = travelBoardPlaceRepository;
            this.travelBoardRepository = travelBoardRepository;
            this.geocodingService = geocodingService;
            this.activityLogService = activityLogService;
    }

    public TravelBoardPlace saveToBoard(Long boardId, TravelBoardPlace newTravelBoardPlace, User user) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        if (!board.getOwner().getId().equals(user.getId()) && !(board.getMembers().contains(user))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only board members can add places");
        }

        checkIfPlaceAlreadySaved(newTravelBoardPlace, board);

        newTravelBoardPlace.setBoard(board);
        newTravelBoardPlace.setUser(user);

        if (newTravelBoardPlace.getAddress() != null && newTravelBoardPlace.getCity() == null) {
            newTravelBoardPlace.setCity(geocodingService.resolveCityFromAddress(newTravelBoardPlace.getAddress()));
        }

        TravelBoardPlace saved = travelBoardPlaceRepository.save(newTravelBoardPlace);

        // ← log the action
        activityLogService.log(board, user, "added " + newTravelBoardPlace.getName());

        return saved;
    }

    public void removeFromBoard(Long boardId, Long placeId, User user) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        TravelBoardPlace place = travelBoardPlaceRepository.findById(placeId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));

        travelBoardPlaceRepository.delete(place);

        // ← log the action
        activityLogService.log(board, user, "removed " + place.getName());
    }

    public List<TravelBoardPlace> getPlacesByBoard(Long boardId) {
        TravelBoard board = travelBoardRepository.findById(boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Travel board not found"));

        return travelBoardPlaceRepository.findAllByBoard(board);
    }

    private void checkIfPlaceAlreadySaved(TravelBoardPlace place, TravelBoard board) {
        boolean alreadySaved = travelBoardPlaceRepository.existsByExternalPlaceIdAndBoard(place.getExternalPlaceId(), board);
        if (alreadySaved) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Place already saved to this travel board");
        }
    }
}
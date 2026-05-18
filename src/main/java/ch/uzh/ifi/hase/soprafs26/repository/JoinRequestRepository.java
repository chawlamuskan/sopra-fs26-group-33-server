package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.constant.JoinRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@Repository("joinRequestRepository")
public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    // find pending request for same sender + board (to avoid duplicates)
    JoinRequest findBySenderAndBoardAndStatus(
        User sender, TravelBoard board, JoinRequestStatus status);
    // find all pending requests where logged-in user is the board owner
    java.util.List<JoinRequest> findByBoardOwnerAndStatus(
        User owner, JoinRequestStatus status);
}
package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;

@Repository("invitationRepository")
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByReceiverAndStatus(User receiver, InviteStatus status);

    Invitation findByBoardAndReceiverAndStatus(TravelBoard board, User receiver, InviteStatus pending);
}

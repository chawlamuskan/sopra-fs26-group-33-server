package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;

@Repository("travelBoardRepository")
public interface TravelBoardRepository extends JpaRepository<TravelBoard, Long> {
	TravelBoard findByName(String name);
    List<TravelBoard> findByOwnerId(Long ownerId);
    List<TravelBoard> findByMembersId(Long userId);
    List<TravelBoard> findByOwnerIdOrMembersId(Long ownerId, Long userId);
    List<TravelBoard> findByPrivacy(PrivacyLevel privacy);
    TravelBoard findByInviteCode(String inviteCode);
}

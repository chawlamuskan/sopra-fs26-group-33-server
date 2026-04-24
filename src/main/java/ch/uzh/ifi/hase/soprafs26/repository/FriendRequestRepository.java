//package ch.uzh.ifi.hase.soprafs26.repository;
//
//import java.util.List;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
//import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
//import ch.uzh.ifi.hase.soprafs26.entity.User;
//
//@Repository("friendRequestRepository")
//public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
//    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);
//
//	FriendRequest findBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);
//	
//}
//
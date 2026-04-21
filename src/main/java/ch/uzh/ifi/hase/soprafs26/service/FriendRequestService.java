package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.FriendRequestRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;



@Service
@Transactional
public class FriendRequestService {

	private final Logger log = LoggerFactory.getLogger(FriendRequestService.class);
    
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendRequestService(@Qualifier("friendRequestRepository") FriendRequestRepository friendRequestRepository, UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
	}

	public List<FriendRequest> getFriendRequests() {
		return this.friendRequestRepository.findAll();
	}

	public FriendRequest sendFriendRequest(String token, Long receiverId) {
        User sender = userRepository.findByToken(token);
        if (sender == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"); 
        }

        if (receiverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver id must not be null");
        }
        
        User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));

        if (sender.getId().equals(receiver.getId())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself");
        }

        if (sender.getFriends().contains(receiver) || receiver.getFriends().contains(sender)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Users are already friends");
        }

        FriendRequest pending1 = friendRequestRepository.findBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING);

        FriendRequest pending2 = friendRequestRepository.findBySenderAndReceiverAndStatus(receiver, sender, FriendRequestStatus.PENDING);

        if (pending1 != null || pending2 != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending friend request already exists");
        }
        
        FriendRequest newFriendRequest = new FriendRequest();
        newFriendRequest.setStatus(FriendRequestStatus.PENDING);
        newFriendRequest.setSender(sender);
        newFriendRequest.setReceiver(receiver);

        newFriendRequest = friendRequestRepository.save(newFriendRequest);

        log.debug("Created Information for FriendRequest: {}", newFriendRequest);
        return newFriendRequest;
	}

    public List<FriendRequest> getPendingFriendRequests(String token) {
        User user = userRepository.findByToken(token);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequestStatus.PENDING);
    }

    public void acceptFriendRequest(Long friendRequestId, String token){
        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FriendRequest not found"));

        User user = userRepository.findByToken(token);

        if (!friendRequest.getReceiver().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - not your friendRequest");
        }

        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "FriendRequest was already answered");
        }

        User sender = friendRequest.getSender();
        User receiver = friendRequest.getReceiver();

        if (!sender.getFriends().contains(receiver)) {
            sender.getFriends().add(receiver);
        }

        if (!receiver.getFriends().contains(sender)) {
            receiver.getFriends().add(sender);
        }

        userRepository.save(sender);
        userRepository.save(receiver);

        friendRequest.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(friendRequest);
    }

    public void declineFriendRequest(Long friendRequestId, String token){
        FriendRequest friendRequest = friendRequestRepository.findById(friendRequestId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FriendRequest not found"));

        User user = userRepository.findByToken(token);

        if (!friendRequest.getReceiver().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized - not your friendRequest");
        }

        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "FriendRequest was already answered");
        }

        friendRequest.setStatus(FriendRequestStatus.DECLINED);
        friendRequestRepository.save(friendRequest);
    }

    public List<User> getFriends(String token) {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        return user.getFriends();
    }

    public void removeFriend(String token, Long friendId) {
        User user = userRepository.findByToken(token);

        User friend = userRepository.findById(friendId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend not found"));

        if (!user.getFriends().contains(friend)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Users are not friends");
        }

        user.getFriends().remove(friend);
        friend.getFriends().remove(user);

        userRepository.save(user);
        userRepository.save(friend);
    }

}

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

        FriendRequest accepted1 = friendRequestRepository.findBySenderAndReceiverAndStatus(
            sender, receiver, FriendRequestStatus.ACCEPTED);

        FriendRequest accepted2 = friendRequestRepository.findBySenderAndReceiverAndStatus(
            receiver, sender, FriendRequestStatus.ACCEPTED);
        
        if (accepted1 != null || accepted2 != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Users are already friends");
        }
        
        FriendRequest newFriendRequest = new FriendRequest();
        newFriendRequest.setStatus(FriendRequestStatus.PENDING);
        newFriendRequest.setSender(sender);
        newFriendRequest.setReceiver(receiver);

        newFriendRequest = friendRequestRepository.save(newFriendRequest);

        log.debug("Created Information for FriendRequest: {}", newFriendRequest);
        return newFriendRequest;
	}


}

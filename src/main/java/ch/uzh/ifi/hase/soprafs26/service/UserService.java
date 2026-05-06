package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardPlaceRepository;
import ch.uzh.ifi.hase.soprafs26.repository.SavedPlaceRepository;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final TravelBoardRepository travelBoardRepository;
	private final InvitationRepository invitationRepository;
	private final TravelBoardPlaceRepository travelBoardPlaceRepository;
	private final SavedPlaceRepository savedPlaceRepository;

	public UserService(
		@Qualifier("userRepository") UserRepository userRepository,
		TravelBoardRepository travelBoardRepository,
		InvitationRepository invitationRepository,
		TravelBoardPlaceRepository travelBoardPlaceRepository,
		SavedPlaceRepository savedPlaceRepository) {
		this.userRepository = userRepository;
		this.travelBoardRepository = travelBoardRepository;
		this.invitationRepository = invitationRepository;
		this.travelBoardPlaceRepository = travelBoardPlaceRepository;
		this.savedPlaceRepository = savedPlaceRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	// Register new user
	public User createUser(User newUser) {
		checkIfUserExists(newUser);	// check if username or name already exists
		validateEmail(newUser.getEmail());	// validate email format
		validatePassword(newUser.getPassword()); // validate password format

		newUser.setToken(UUID.randomUUID().toString()); // set default values
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setCreationDate(java.time.LocalDate.now());	// set creation date (added this to User.java)

		newUser = userRepository.save(newUser);	// save the user with all fields including password

		return newUser;
	}

	// Check valid password format during registration and password change
	// POST /users 400 BAD REQUEST
	private void validatePassword(String password) {
		if (password == null || password.length() < 8) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
				"Password must be at least 8 characters long");
		}
		if (!password.matches(".*[A-Z].*")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
				"Password must contain at least one uppercase letter");
		}
		if (!password.matches(".*[0-9].*")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
				"Password must contain at least one number");
		}
		if (!password.matches(".*[^a-zA-Z0-9].*")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
				"Password must contain at least one special character");
		}
	}

	// Check valid email format during registration
	// POST /users 400 BAD REQUEST
	private void validateEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
				"Email cannot be empty");
		}

		String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
		if (!email.matches(emailRegex)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
				"Invalid email format. Expected format: example@domain.com");
		}
	}

	// Login user (with username/email and password)
	public User loginUser(String username, String email, String password) {
		User user = null;
		if (username != null && !username.trim().isEmpty()) {
        	user = userRepository.findByUsername(username);
		} else if (email != null && !email.trim().isEmpty()) {
			user = userRepository.findByEmail(email);
		}
		if (user == null || !user.getPassword().equals(password)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		user.setStatus(UserStatus.ONLINE);
		user.setToken(UUID.randomUUID().toString());

		return userRepository.save(user);
	}

	// added for getuser id for speficif user page 
	public User getUserById(Long id) {
		return userRepository.findById(id)
			.orElseThrow(() ->
				new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
	}

	// Loogout user by token
	public void logoutByToken(String token) {
		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		user.setStatus(UserStatus.OFFLINE);	// set status to OFFLINE on logout
		user.setToken(null); // invalidate token on logout
		userRepository.save(user);
	}

	// Update user's password (only logged-in users can access their own profile)
    // This method does not require Authorization header because frontend ensures
    public void updatePassword(Long userId, String newPassword) {
        // Find the user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Validate password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be empty");
        }
		validatePassword(newPassword); 	// validate new password adheres to required format

        // Update password
        user.setPassword(newPassword);

        // Log out the user
        user.setStatus(UserStatus.OFFLINE);

        userRepository.save(user);
    }

	// Validate that the token exists and belongs to a real logged-in user
	public User validateToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided");
		}
		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
		}
		if (user.getStatus() == UserStatus.OFFLINE) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not logged in");
		}
		return user;
	}
	
	// Check uniqueness criteria of the username and email #43
	// POST /users 409 CONFLICT based on REST specifications
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		User userByEmail = userRepository.findByEmail(userToBeCreated.getEmail());

		if (userByUsername != null && userByEmail != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
				"Registration failed: Username & email already exist");
		} else if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
				"Registration failed: Username already exists");
		} else if (userByEmail != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, 
				"Registration failed: Email already exists");
		}
	}

	// Delete user account and all associated data
	public void deleteUser(Long userId) {
		// Find the user
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

		// Delete travel boards owned by this user
		List<TravelBoard> ownedBoards = travelBoardRepository.findByOwnerId(userId);
		for (TravelBoard board : ownedBoards) {
			// First delete all travel board places for this board
			List<TravelBoardPlace> places = travelBoardPlaceRepository.findAllByBoard(board);
			for (TravelBoardPlace place : places) {
				travelBoardPlaceRepository.delete(place);
			}
			
			// Delete all invitations for this board
			List<Invitation> boardInvitations = invitationRepository.findAll();
			for (Invitation invitation : boardInvitations) {
				if (invitation.getBoard() != null && invitation.getBoard().getId().equals(board.getId())) {
					invitationRepository.delete(invitation);
				}
			}
			
			// Now delete the board
			travelBoardRepository.delete(board);
		}

		// Remove user from member lists of travel boards (and delete if empty after removal)
		List<TravelBoard> memberBoards = travelBoardRepository.findByMembersId(userId);
		for (TravelBoard board : memberBoards) {
			board.getMembers().remove(user);
			travelBoardRepository.save(board);
		}

		// Delete saved places created by this user
		List<SavedPlace> savedPlaces = savedPlaceRepository.findAllByUser(user);
		for (SavedPlace place : savedPlaces) {
			savedPlaceRepository.delete(place);
		}

		// Delete travel board places created by this user
		List<TravelBoardPlace> userPlaces = travelBoardPlaceRepository.findAll();
		for (TravelBoardPlace place : userPlaces) {
			if (place.getUser() != null && place.getUser().getId().equals(userId)) {
				travelBoardPlaceRepository.delete(place);
			}
		}

		// Delete all invitations related to this user (as sender or receiver)
		List<Invitation> allInvitations = invitationRepository.findAll();
		for (Invitation invitation : allInvitations) {
			if ((invitation.getSender() != null && invitation.getSender().getId().equals(userId)) ||
				(invitation.getReceiver() != null && invitation.getReceiver().getId().equals(userId))) {
				invitationRepository.delete(invitation);
			}
		}

		// Delete the user (Preferences should cascade delete due to CascadeType.ALL)
		userRepository.delete(user);
	}

}

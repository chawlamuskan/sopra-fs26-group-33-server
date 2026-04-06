package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

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

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	// Register new user
	public User createUser(User newUser) {
		checkIfUserExists(newUser);	// check if username or name already exists
		validatePassword(newUser.getPassword()); // validate password format
		newUser.setToken(UUID.randomUUID().toString()); // set default values
		newUser.setStatus(UserStatus.ONLINE);
		newUser.setCreationDate(java.time.LocalDate.now());	// set creation date (added this to User.java)
		newUser = userRepository.save(newUser);	// save the user with all fields including password and bio

		log.debug("Created Information for User: {}", newUser);
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

	// had problem that after logging out with button the status stayed ONLINE - so need this 
	public void logoutUser(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
		user.setStatus(UserStatus.OFFLINE);
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
	public void validateToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No token provided");
		}
		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
		}
	}

	//public User getUserByToken(String token) {
	//	return userRepository.findByToken(token);
	//}

	
	// Check uniqueness criteria of the username and email #43
	// POST /users 409 CONFLICT based on REST specifications
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		User userByEmail = userRepository.findByEmail(userToBeCreated.getEmail());

		if (userByUsername != null && userByEmail != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
				"Registration failed: username & email already exist");
		} else if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
				"Registration failed: username already exists");
		} else if (userByEmail != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, 
				"Registration failed: email already exists");
		}
	}

	
}

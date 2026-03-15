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

	// mpdified this part  to handle password bio creationdate

	public User createUser(User newUser) {
    // check if username or name already exists
    checkIfUserExists(newUser);

    // set default values
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);

    // set creation date (added this to User.java)
    newUser.setCreationDate(java.time.LocalDate.now());

    // save the user with all fields including password and bio
    newUser = userRepository.save(newUser);

    log.debug("Created Information for User: {}", newUser);
    return newUser;
	}

	// added for user

	public User loginUser(String username, String password) {

    User user = userRepository.findByUsername(username);

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

	// for user story 3 i need to update the password 
	/**
     * Update the user's own password.
     * This method does not require Authorization header because frontend ensures
     * only logged-in users can access their own profile.
     */
    public void updatePassword(Long userId, String newPassword) {
        // Find the user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Validate password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password cannot be empty");
        }

        // Update password
        user.setPassword(newPassword);

        // Log out the user
        user.setStatus(UserStatus.OFFLINE);

        userRepository.save(user);
    }

	// ADDED: validate that the token exists and belongs to a real logged-in user
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

	
	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		User userByName = userRepository.findByName(userToBeCreated.getName());

		String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
		if (userByUsername != null && userByName != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					String.format(baseErrorMessage, "username and the name", "are"));
		} else if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "username", "is"));
		} else if (userByName != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, String.format(baseErrorMessage, "name", "is"));
		}
	}
}

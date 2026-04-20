package ch.uzh.ifi.hase.soprafs26.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.User;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Long> {
	User findByName(String name);
	User findByEmail(String email);
	User findByUsername(String username);
	User findByToken(String token); // for token validation during authorization
	List<User> findByUsernameContainingIgnoreCase(String username);
}

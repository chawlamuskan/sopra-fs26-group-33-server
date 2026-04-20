package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;

@Repository("preferencesRepository")
public interface PreferencesRepository extends JpaRepository<Preferences, Long> {
    Preferences findByUser(User user);
}

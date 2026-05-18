package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.ActivityLog;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ActivityLogRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class ActivityLogServiceIntegrationTest {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TravelBoardRepository travelBoardRepository;
    
    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private User testUser;
    private TravelBoard testBoard;

    @BeforeEach
    void setup() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setName("Test User");
        testUser.setToken("test-token");
        testUser.setStatus(UserStatus.ONLINE);
        testUser.setCreationDate(LocalDate.now());
        userRepository.save(testUser);

        testBoard = new TravelBoard();
        testBoard.setName("Paris Trip");
        testBoard.setOwner(testUser);
        testBoard.setPrivacy(PrivacyLevel.PRIVATE);
        testBoard.setDateCreated(LocalDate.now());
        travelBoardRepository.save(testBoard);
    }

    @Test
    // Test that a log is actually persisted to the database
    public void log_validInput_persistedToDatabase() {
        activityLogService.log(testBoard, testUser, "added Eiffel Tower");

        List<ActivityLog> logs = activityLogRepository.findAll();
        assertEquals(1, logs.size());
        assertEquals("added Eiffel Tower", logs.get(0).getAction());
        assertEquals(testUser.getId(), logs.get(0).getUser().getId());
        assertEquals(testBoard.getId(), logs.get(0).getBoard().getId());
    }

    @Test
    // Test that multiple logs are all persisted
    public void log_multipleLogs_allPersisted() {
        activityLogService.log(testBoard, testUser, "joined the board");
        activityLogService.log(testBoard, testUser, "added Eiffel Tower");
        activityLogService.log(testBoard, testUser, "removed Louvre");

        List<ActivityLog> logs = activityLogRepository.findAll();
        assertEquals(3, logs.size());
    }

    @Test
    public void log_savedLog_appearsInBoardActivityLogs() {
        activityLogService.log(testBoard, testUser, "added Eiffel Tower");

        entityManager.flush();
        entityManager.clear();

        TravelBoard fetchedBoard = travelBoardRepository.findById(testBoard.getId()).orElseThrow();
        assertEquals(1, fetchedBoard.getActivityLogs().size());
        assertEquals("added Eiffel Tower", fetchedBoard.getActivityLogs().get(0).getAction());
    }
}
package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.ActivityLog;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.ActivityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ActivityLogServiceTest
 * - tests that logs are created and saved correctly
 * - tests that the log contains the correct board, user, and action
 */
public class ActivityLogServiceTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    private User mockUser;
    private TravelBoard mockBoard;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        mockBoard = new TravelBoard();
        mockBoard.setId(1L);
        mockBoard.setName("Paris Trip");
    }

    @Test
    // Test that log is saved with correct board, user, and action
    public void log_validInput_savesLogWithCorrectFields() {
        activityLogService.log(mockBoard, mockUser, "added Eiffel Tower");

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository, times(1)).save(captor.capture());

        ActivityLog saved = captor.getValue();
        assertEquals(mockBoard, saved.getBoard());
        assertEquals(mockUser, saved.getUser());
        assertEquals("added Eiffel Tower", saved.getAction());
    }

    @Test
    // Test that log is saved when a place is removed
    public void log_removedAction_savesLogCorrectly() {
        activityLogService.log(mockBoard, mockUser, "removed Eiffel Tower");

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository, times(1)).save(captor.capture());

        ActivityLog saved = captor.getValue();
        assertEquals("removed Eiffel Tower", saved.getAction());
    }

    @Test
    // Test that log is saved when a user joins the board
    public void log_joinedAction_savesLogCorrectly() {
        activityLogService.log(mockBoard, mockUser, "joined the board");

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository, times(1)).save(captor.capture());

        ActivityLog saved = captor.getValue();
        assertEquals("joined the board", saved.getAction());
    }
    @Test
    // Test that repository save is called exactly once per log call
    public void log_calledMultipleTimes_savesEachLog() {
        activityLogService.log(mockBoard, mockUser, "added Eiffel Tower");
        activityLogService.log(mockBoard, mockUser, "removed Louvre");

        verify(activityLogRepository, times(2)).save(any(ActivityLog.class));
    }
}
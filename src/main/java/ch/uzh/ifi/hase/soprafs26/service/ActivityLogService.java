package ch.uzh.ifi.hase.soprafs26.service;
import org.springframework.stereotype.Service;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.ActivityLog;
import ch.uzh.ifi.hase.soprafs26.repository.ActivityLogRepository;


@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public void log(TravelBoard board, User user, String action) {
        ActivityLog log = new ActivityLog();
        log.setBoard(board);
        log.setUser(user);
        log.setAction(action);
        activityLogRepository.save(log);
    }
}
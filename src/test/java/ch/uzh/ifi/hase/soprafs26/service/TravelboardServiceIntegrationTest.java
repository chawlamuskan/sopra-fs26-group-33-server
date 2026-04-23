package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TravelBoardRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.PreferencesRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * Test class for the UserResource REST resource.
 *
 * @see TravelBoardService
 */
@WebAppConfiguration
@SpringBootTest
public class TravelboardServiceIntegrationTest {

	@Qualifier("userRepository")
	@Autowired
	private UserRepository userRepository;

    @Qualifier("preferencesRepository")
    @Autowired
    private PreferencesRepository preferencesRepository;

	@Qualifier("travelBoardRepository")
	@Autowired
	private TravelBoardRepository travelBoardRepository;

    @Qualifier("invitationRepository")
    @Autowired
    private InvitationRepository invitationRepository;

	@Autowired
	private TravelBoardService travelBoardService;

	@BeforeEach
	public void setup() {
        invitationRepository.deleteAll();
		travelBoardRepository.deleteAll();
        preferencesRepository.deleteAll();
        userRepository.deleteAll();
	}
    
    //#118
    @Test
    public void deleteTravelBoard_removesBoardFromDatabase() {
        // create user
        User user = new User();
        user.setName("owner");
        user.setUsername("owner123");
        user.setPassword("pw");
        user.setEmail("owner123@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("token123");
        user = userRepository.save(user);
    
        // simulate login token
        user.setToken("token123");
        userRepository.save(user);
    
        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Test Board");
        board.setOwner(user);
        board.setInviteCode("DEL123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
    
        board = travelBoardRepository.save(board);
    
        Long boardId = board.getId();
    
        // delete
        travelBoardService.deleteTravelBoard(boardId, "token123");
    
        // assert
        assertTrue(travelBoardRepository.findById(boardId).isEmpty());
    }

    //#119
    @Test
    public void renameTravelBoard_updatesBoardNameInDatabase() {
        // create user
        User user = new User();
        user.setName("owner");
        user.setUsername("owner123");
        user.setPassword("pw");
        user.setEmail("owner123@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("token123");
        user = userRepository.save(user);

        // simulate login token
        user.setToken("token123");
        userRepository.save(user);

        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Old Name");
        board.setOwner(user);
        board.setInviteCode("REN123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());

        board = travelBoardRepository.save(board);

        Long boardId = board.getId();

        // rename
        travelBoardService.renameTravelBoard(boardId, "token123", "New Name");

        // assert
        TravelBoard updated = travelBoardRepository.findById(boardId).orElseThrow();

        assertEquals("New Name", updated.getName());
    }

    //#153
    @Test
    public void joinTravelBoard_validCode_userAddedToMembers() {
        // create user
        User user = new User();
        user.setName("owner");
        user.setUsername("owner123");
        user.setPassword("pw");
        user.setEmail("owner123@test.ch");
        user.setCreationDate(LocalDate.now());
        user.setStatus(UserStatus.ONLINE);
        user.setToken("token123");
        user = userRepository.save(user);

        // create user to join
        User joiner = new User();
        joiner.setName("joiner");
        joiner.setUsername("joiner123");
        joiner.setPassword("pw");
        joiner.setEmail("joiner123@test.ch");
        joiner.setCreationDate(LocalDate.now());
        joiner.setStatus(UserStatus.ONLINE);
        joiner.setToken("token456");
        joiner = userRepository.save(joiner);

        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Old Name");
        board.setOwner(user);
        board.setInviteCode("ABC123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());

        board = travelBoardRepository.save(board);
        Long boardId = board.getId();

        // join add member
        travelBoardService.joinTravelBoardByInviteCode(joiner.getToken(), "ABC123");

        // assert
        assertTrue(travelBoardService.getTravelBoardsByUser(joiner.getToken())
                .stream() // verifies that the board the joiner joined is now included in their travel board list
                .anyMatch(b -> b.getId().equals(boardId))); // true if any board in that list has the expected ID
    }

    //#265
    @Test
    public void leaveTravelBoard_validMember_removesOnlyThatUsersMembership() {
        // create user: owner
        User owner = new User();
        owner.setName("owner");
        owner.setUsername("owner123");
        owner.setPassword("pw");
        owner.setEmail("owner123@test.ch");
        owner.setCreationDate(LocalDate.now());
        owner.setStatus(UserStatus.ONLINE);
        owner.setToken("ownertoken");
        owner = userRepository.save(owner);

        // create user: member1
        User member1 = new User();
        member1.setName("member1");
        member1.setUsername("member123");
        member1.setPassword("pw");
        member1.setEmail("member123@test.ch");
        member1.setCreationDate(LocalDate.now());
        member1.setStatus(UserStatus.ONLINE);
        member1.setToken("member-token-1");
        member1 = userRepository.save(member1);

        // create user: member2
        User member2 = new User();
        member2.setName("member2");
        member2.setUsername("member456");
        member2.setPassword("pw");
        member2.setEmail("member456@test.ch");
        member2.setCreationDate(LocalDate.now());
        member2.setStatus(UserStatus.ONLINE);
        member2.setToken("member-token-2");
        member2 = userRepository.save(member2);

        // create board
        TravelBoard board = new TravelBoard();
        board.setName("Group Trip");
        board.setOwner(owner);
        board.setInviteCode("LEAVE123");
        board.setPrivacy(PrivacyLevel.PUBLIC);
        board.setDateCreated(LocalDate.now());
        board.getMembers().add(member1);
        board.getMembers().add(member2);
        board = travelBoardRepository.save(board);
        Long boardId = board.getId();

        // member1 leave board
        travelBoardService.leaveTravelBoard(board.getId(), member1.getToken());

        // assert
        assertTrue(travelBoardRepository.findById(boardId).isPresent());
        assertFalse(travelBoardRepository.findByMembersId(member1.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(boardId)));
        assertTrue(travelBoardRepository.findByMembersId(member2.getId())
                .stream()
                .anyMatch(b -> b.getId().equals(boardId)));
    }
}
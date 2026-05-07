package ch.uzh.ifi.hase.soprafs26.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
public class TravelBoardRepositoryIntegrationTest {

	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private TravelBoardRepository travelBoardRepository;

    @Test
    public void findByName_existingBoard_success() {
        // given
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        TravelBoard board = createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC);

        entityManager.flush();

        // when
        TravelBoard foundBoard = travelBoardRepository.findByName("Test Board");

        // then
        assertEquals(board.getId(), foundBoard.getId());
        assertEquals(board.getName(), foundBoard.getName());
        assertEquals(owner.getId(), foundBoard.getOwner().getId());
        assertEquals(board.getInviteCode(), foundBoard.getInviteCode());
        assertEquals(board.getPrivacy(), foundBoard.getPrivacy());
    }

    @Test
    public void findByName_unknownName_returnsNull() {
        // given
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC);

        entityManager.flush();

        // when
        TravelBoard foundBoard = travelBoardRepository.findByName("Unknown Board");

        // then
        assertNull(foundBoard);
    }

    @Test
    public void findByOwnerId_existingBoards_success() {
        // given
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        User otherOwner = createTestUser("otherOwner", "otherOwner123", "othertoken123");

        TravelBoard board1 = createTestBoard("First Board", owner, "CODE123", PrivacyLevel.PUBLIC);
        TravelBoard board2 = createTestBoard("Second Board", owner, "CODE456", PrivacyLevel.PRIVATE);
        TravelBoard otherBoard = createTestBoard("Other Board", otherOwner, "CODE789", PrivacyLevel.PUBLIC);

        entityManager.flush();

        // when
        List<TravelBoard> foundBoards = travelBoardRepository.findByOwnerId(owner.getId());

        // then
        assertEquals(2, foundBoards.size());
        assertTrue(foundBoards.stream().anyMatch(board -> board.getId().equals(board1.getId())));
        assertTrue(foundBoards.stream().anyMatch(board -> board.getId().equals(board2.getId())));
        assertFalse(foundBoards.stream().anyMatch(board -> board.getId().equals(otherBoard.getId())));
    }

    @Test
    public void findByMembersId_existingMember_success() {
        // given
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        User searchedMember = createTestUser("member", "member123", "membertoken123");
        User otherMember = createTestUser("otherMember", "otherMember123", "othertoken123");

        TravelBoard boardWithSearchedMember = createTestBoard("Member Board", owner, "CODE123", PrivacyLevel.PUBLIC);
        TravelBoard boardWithOtherMember = createTestBoard("Other Board", owner, "CODE456", PrivacyLevel.PRIVATE);

        boardWithSearchedMember.getMembers().add(searchedMember);
        boardWithOtherMember.getMembers().add(otherMember);

        entityManager.persist(boardWithSearchedMember);
        entityManager.persist(boardWithOtherMember);
        entityManager.flush();

        // when
        List<TravelBoard> foundBoards = travelBoardRepository.findByMembersId(searchedMember.getId());

        // then
        assertEquals(1, foundBoards.size());
        assertTrue(foundBoards.stream().anyMatch(board -> board.getId().equals(boardWithSearchedMember.getId())));
        assertFalse(foundBoards.stream().anyMatch(board -> board.getId().equals(boardWithOtherMember.getId())));
    }

    @Test
    public void findByInviteCode_existingInviteCode_success() {
        // given
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        TravelBoard board = createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC);

        entityManager.flush();

        // when
        TravelBoard foundBoard = travelBoardRepository.findByInviteCode("CODE123");

        // then
        assertEquals(board.getId(), foundBoard.getId());
        assertEquals(board.getName(), foundBoard.getName());
        assertEquals(board.getInviteCode(), foundBoard.getInviteCode());
        assertEquals(owner.getId(), foundBoard.getOwner().getId());
    }

    @Test
    public void findByInviteCode_unknownInviteCode_returnsNull() {
        // given
        User owner = createTestUser("owner", "owner123", "ownertoken123");
        createTestBoard("Test Board", owner, "CODE123", PrivacyLevel.PUBLIC);

        entityManager.flush();

        // when
        TravelBoard foundBoard = travelBoardRepository.findByInviteCode("UNKNOWN123");

        // then
        assertNull(foundBoard);
    }

    private User createTestUser(String name, String username, String token) {
        User user = new User();
        user.setName(name);
        user.setUsername(username);
        user.setEmail(username + "@test.ch");
        user.setPassword("Password123!");
        user.setToken(token);
        user.setStatus(UserStatus.ONLINE);
        user.setCreationDate(LocalDate.now());

        entityManager.persist(user);
        return user;
    }

    private TravelBoard createTestBoard(String name, User owner, String inviteCode, PrivacyLevel privacy) {
        TravelBoard board = new TravelBoard();
        board.setName(name);
        board.setOwner(owner);
        board.setInviteCode(inviteCode);
        board.setPrivacy(privacy);
        board.setDateCreated(LocalDate.now());

        entityManager.persist(board);
        return board;
    }
}
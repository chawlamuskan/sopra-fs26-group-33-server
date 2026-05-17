package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.junit.jupiter.api.Test;

import ch.uzh.ifi.hase.soprafs26.constant.FriendRequestStatus;
import ch.uzh.ifi.hase.soprafs26.constant.InviteStatus;
import ch.uzh.ifi.hase.soprafs26.constant.PrivacyLevel;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InvitationGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
	@Test
	public void testCreateUser_fromUserPostDTO_toUser_success() {
		// create UserPostDTO
		UserPostDTO userPostDTO = new UserPostDTO();
		userPostDTO.setName("name");
		userPostDTO.setUsername("username");
		userPostDTO.setEmail("email@example.com");
		userPostDTO.setPassword("Password123!");

		// MAP -> Create user
		User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// check content
		assertEquals(userPostDTO.getName(), user.getName());
		assertEquals(userPostDTO.getUsername(), user.getUsername());
		assertEquals(userPostDTO.getEmail(), user.getEmail());
		assertEquals(userPostDTO.getPassword(), user.getPassword());
	}

	@Test
	public void testGetUser_fromUser_toUserGetDTO_success() {
		// create User
		User user = new User();
		user.setName("Firstname Lastname");
		user.setUsername("firstname@lastname");
		user.setEmail("test@example.com");
		user.setStatus(UserStatus.OFFLINE);
		user.setToken("1");

		// MAP -> Create UserGetDTO
		UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

		// check content
		assertEquals(user.getId(), userGetDTO.getId());
		assertEquals(user.getName(), userGetDTO.getName());
		assertEquals(user.getUsername(), userGetDTO.getUsername());
		assertEquals(user.getEmail(), userGetDTO.getEmail());
		assertEquals(user.getStatus(), userGetDTO.getStatus());
	}

	@Test
	public void testCreateTravelBoard_fromTravelBoardPostDTO_toTravelBoard_success() {
	    // create TravelBoardPostDTO
	    TravelBoardPostDTO travelBoardPostDTO = new TravelBoardPostDTO();
	    travelBoardPostDTO.setName("Test Board");
	    travelBoardPostDTO.setLocation("Test Location");
	    travelBoardPostDTO.setStartDate(LocalDate.of(2026, 6, 1));
	    travelBoardPostDTO.setEndDate(LocalDate.of(2026, 6, 14));
	    travelBoardPostDTO.setInviteCode("CODE123");
	    travelBoardPostDTO.setPrivacy(PrivacyLevel.PRIVATE);

	    // MAP -> Create TravelBoard
	    TravelBoard travelBoard = DTOMapper.INSTANCE.convertTravelBoardPostDTOtoEntity(travelBoardPostDTO);

	    // check content
	    assertEquals(travelBoardPostDTO.getName(), travelBoard.getName());
	    assertEquals(travelBoardPostDTO.getLocation(), travelBoard.getLocation());
	    assertEquals(travelBoardPostDTO.getStartDate(), travelBoard.getStartDate());
	    assertEquals(travelBoardPostDTO.getEndDate(), travelBoard.getEndDate());
	    assertEquals(travelBoardPostDTO.getInviteCode(), travelBoard.getInviteCode());
	    assertEquals(travelBoardPostDTO.getPrivacy(), travelBoard.getPrivacy());
	}

	@Test
	public void testGetTravelBoard_fromTravelBoard_toTravelBoardGetDTO_success() {
	    // create owner
	    User owner = new User();
	    owner.setId(1L);
	    owner.setUsername("owner123");

	    // create TravelBoard
	    TravelBoard travelBoard = new TravelBoard();
	    travelBoard.setId(10L);
	    travelBoard.setName("Test Board");
	    travelBoard.setLocation("Test Location");
	    travelBoard.setStartDate(LocalDate.of(2026, 6, 1));
	    travelBoard.setEndDate(LocalDate.of(2026, 6, 14));
	    travelBoard.setOwner(owner);
	    travelBoard.setInviteCode("CODE123");
	    travelBoard.setPrivacy(PrivacyLevel.PRIVATE);
	    travelBoard.setDateCreated(LocalDate.of(2026, 1, 1));

	    // MAP -> Create TravelBoardGetDTO
	    TravelBoardGetDTO travelBoardGetDTO = DTOMapper.INSTANCE.convertEntityToTravelBoardGetDTO(travelBoard);

	    // check content
	    assertEquals(travelBoard.getId(), travelBoardGetDTO.getId());
	    assertEquals(travelBoard.getName(), travelBoardGetDTO.getName());
	    assertEquals(travelBoard.getLocation(), travelBoardGetDTO.getLocation());
	    assertEquals(travelBoard.getStartDate(), travelBoardGetDTO.getStartDate());
	    assertEquals(travelBoard.getEndDate(), travelBoardGetDTO.getEndDate());
	    assertEquals(travelBoard.getOwner().getId(), travelBoardGetDTO.getOwnerId());
	    assertEquals(travelBoard.getInviteCode(), travelBoardGetDTO.getInviteCode());
	    assertEquals(travelBoard.getPrivacy(), travelBoardGetDTO.getPrivacy());
	    assertEquals(travelBoard.getDateCreated(), travelBoardGetDTO.getDateCreated());
	}

	@Test
	public void testGetInvitation_fromInvitation_toInvitationGetDTO_success() {
	    // create sender
	    User sender = new User();
	    sender.setId(1L);
	    sender.setUsername("sender123");

	    // create receiver
	    User receiver = new User();
	    receiver.setId(2L);
	    receiver.setUsername("receiver123");

	    // create board
	    TravelBoard board = new TravelBoard();
	    board.setId(10L);
	    board.setName("Test Board");

	    // create Invitation
	    Invitation invitation = new Invitation();
	    invitation.setId(100L);
	    invitation.setBoard(board);
	    invitation.setSender(sender);
	    invitation.setReceiver(receiver);
	    invitation.setStatus(InviteStatus.PENDING);

	    // MAP -> Create InvitationGetDTO
	    InvitationGetDTO invitationGetDTO = DTOMapper.INSTANCE.convertEntityToInvitationGetDTO(invitation);

	    // check content
	    assertEquals(invitation.getId(), invitationGetDTO.getId());
	    assertEquals(board.getId(), invitationGetDTO.getBoardId());
	    assertEquals(sender.getId(), invitationGetDTO.getSenderId());
	    assertEquals(receiver.getId(), invitationGetDTO.getReceiverId());
	    assertEquals(invitation.getStatus(), invitationGetDTO.getStatus());
	    assertEquals(board.getName(), invitationGetDTO.getBoardName());
	    assertEquals(sender.getUsername(), invitationGetDTO.getSenderUsername());
	}

	@Test
	public void testGetFriendRequest_fromFriendRequest_toFriendRequestGetDTO_success() {
	    // create sender
	    User sender = new User();
	    sender.setId(1L);
	    sender.setUsername("sender123");
	
	    // create receiver
	    User receiver = new User();
	    receiver.setId(2L);
	    receiver.setUsername("receiver123");
	
	    // create FriendRequest
	    FriendRequest friendRequest = new FriendRequest();
	    friendRequest.setId(100L);
	    friendRequest.setSender(sender);
	    friendRequest.setReceiver(receiver);
	    friendRequest.setStatus(FriendRequestStatus.PENDING);
	
	    // MAP -> Create FriendRequestGetDTO
	    FriendRequestGetDTO friendRequestGetDTO = DTOMapper.INSTANCE.convertEntityToFriendRequestGetDTO(friendRequest);
	
	    // check content
	    assertEquals(friendRequest.getId(), friendRequestGetDTO.getId());
	    assertEquals(sender.getId(), friendRequestGetDTO.getSenderId());
	    assertEquals(receiver.getId(), friendRequestGetDTO.getReceiverId());
	    assertEquals(friendRequest.getStatus(), friendRequestGetDTO.getStatus());
	    assertEquals(sender.getUsername(), friendRequestGetDTO.getSenderUsername());
	}

}
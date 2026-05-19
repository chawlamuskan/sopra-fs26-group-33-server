package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.ActivityLog;
import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ActivityLogDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PreferencesGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PreferencesPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlaceGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InvitationGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPlaceGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs26.rest.dto.JoinRequestGetDTO;
import java.util.List;

@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	// ==================== User Mappings ====================
	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "password", target = "password")
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "token", target = "token")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "creationDate", target = "creationDate")
	UserGetDTO convertEntityToUserGetDTO(User user);

	// ==================== TravelBoard Mappings ====================
	@Mapping(source = "name", target = "name")
	@Mapping(source = "location", target = "location")
	@Mapping(source = "startDate", target = "startDate")
	@Mapping(source = "endDate", target = "endDate")
	@Mapping(source = "inviteCode", target = "inviteCode")
	@Mapping(source = "privacy", target = "privacy")
	@Mapping(source = "latMin", target = "latMin")
	@Mapping(source = "latMax", target = "latMax")
	@Mapping(source = "lngMin", target = "lngMin")
	@Mapping(source = "lngMax", target = "lngMax")
	@Mapping(source = "countryCode", target = "countryCode")
	TravelBoard convertTravelBoardPostDTOtoEntity(TravelBoardPostDTO travelBoardPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "location", target = "location")
	@Mapping(source = "startDate", target = "startDate")
	@Mapping(source = "endDate", target = "endDate")
	@Mapping(source = "owner.id", target = "ownerId")
	@Mapping(source = "inviteCode", target = "inviteCode")
	@Mapping(source = "privacy", target = "privacy")
	@Mapping(source = "dateCreated", target = "dateCreated")
	@Mapping(source = "latMin", target = "latMin")
	@Mapping(source = "latMax", target = "latMax")
	@Mapping(source = "lngMin", target = "lngMin")
	@Mapping(source = "lngMax", target = "lngMax")
	@Mapping(target = "memberIds", ignore = true)
	@Mapping(source = "activityLogs", target = "activityLogs")
	@Mapping(source = "countryCode", target = "countryCode")
	TravelBoardGetDTO convertEntityToTravelBoardGetDTO(TravelBoard travelBoard);

	// ==================== ActivityLog Mappings ====================
	@Mapping(target = "userId", expression = "java(log.getUser() != null ? log.getUser().getId() : null)")
	ActivityLogDTO convertEntityToActivityLogDTO(ActivityLog log);

	List<ActivityLogDTO> convertEntityToActivityLogDTO(List<ActivityLog> logs);

	// ==================== Preferences Mappings ====================
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "profilePicture", target = "profilePicture")
	@Mapping(source = "visitedCountries", target = "visitedCountries")
	@Mapping(source = "wishlistCountries", target = "wishlistCountries")
	@Mapping(source = "friends", target = "friends")
	Preferences convertPreferencesPostDTOtoEntity(PreferencesPostDTO preferencesPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "profilePicture", target = "profilePicture")
	@Mapping(source = "visitedCountries", target = "visitedCountries")
	@Mapping(source = "wishlistCountries", target = "wishlistCountries")
	@Mapping(source = "friends", target = "friends")
	PreferencesGetDTO convertEntityToPreferencesGetDTO(Preferences preferences);

	// ==================== Invitation Mappings ====================
	@Mapping(source = "id", target = "id")
	@Mapping(source = "board.id", target = "boardId")
	@Mapping(source = "sender.id", target = "senderId")
	@Mapping(source = "receiver.id", target = "receiverId")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "board.name", target = "boardName")
	@Mapping(source = "sender.username", target = "senderUsername")
    InvitationGetDTO convertEntityToInvitationGetDTO(Invitation createdInvitation);

	// ==================== Friend Request Mappings ====================
	@Mapping(source = "id", target = "id")
	@Mapping(source = "sender.id", target = "senderId")
	@Mapping(source = "receiver.id", target = "receiverId")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "sender.username", target = "senderUsername")
    FriendRequestGetDTO convertEntityToFriendRequestGetDTO(FriendRequest createdFriendRequest);

	// ==================== Join Request Mappings ====================
	@Mapping(source = "id", target = "id")
	@Mapping(source = "board.id", target = "boardId")
	@Mapping(source = "sender.id", target = "senderId")
	@Mapping(source = "sender.username", target = "senderUsername")
	@Mapping(source = "board.name", target = "boardName")
	@Mapping(source = "status", target = "status")
	JoinRequestGetDTO convertEntityToJoinRequestGetDTO(JoinRequest joinRequest);

	// ==================== Saved Places Mappings ====================
	@Mapping(source = "externalPlaceId", target = "externalPlaceId")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "address", target = "address")
	@Mapping(source = "rating", target = "rating")
	@Mapping(source = "photoReference", target = "photoReference")
	@Mapping(source = "lat", target = "lat")
	@Mapping(source = "lng", target = "lng")
	@Mapping(source = "types", target = "types")
	@Mapping(source = "city", target = "city")
	SavedPlace convertSavedPlacePostDTOToEntity(SavedPlacePostDTO savedPlacePostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "externalPlaceId", target = "externalPlaceId")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "address", target = "address")
	@Mapping(source = "rating", target = "rating")
	@Mapping(source = "photoReference", target = "photoReference")
	@Mapping(source = "lat", target = "lat")
	@Mapping(source = "lng", target = "lng")
	@Mapping(source = "types", target = "types")
	@Mapping(source = "city", target = "city")
	SavedPlaceGetDTO convertEntityToSavedPlaceGetDTO(SavedPlace savedPlace);

	// ==================== Travel Board Places Mappings ====================
	@Mapping(source = "externalPlaceId", target = "externalPlaceId")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "address", target = "address")
	@Mapping(source = "rating", target = "rating")
	@Mapping(source = "photoReference", target = "photoReference")
	@Mapping(source = "lat", target = "lat")
	@Mapping(source = "lng", target = "lng")
	@Mapping(source = "types", target = "types")
	TravelBoardPlace convertTravelBoardPlacePostDTOToEntity(TravelBoardPlacePostDTO travelBoardPlacePostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "externalPlaceId", target = "externalPlaceId")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "address", target = "address")
	@Mapping(source = "rating", target = "rating")
	@Mapping(source = "photoReference", target = "photoReference")
	@Mapping(source = "lat", target = "lat")
	@Mapping(source = "lng", target = "lng")
	@Mapping(source = "types", target = "types")
	@Mapping(source = "user.id", target = "addedByUserId")
	@Mapping(source = "city", target = "city")
	TravelBoardPlaceGetDTO convertEntityToTravelBoardPlaceGetDTO(TravelBoardPlace travelBoardPlace);
}
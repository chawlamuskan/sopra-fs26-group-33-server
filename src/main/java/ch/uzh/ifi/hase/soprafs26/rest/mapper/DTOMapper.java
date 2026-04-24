package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.FriendRequest;
import ch.uzh.ifi.hase.soprafs26.entity.Invitation;
// import ch.uzh.ifi.hase.soprafs26.entity.Place;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.TravelBoardPlace;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Preferences;
import ch.uzh.ifi.hase.soprafs26.entity.SavedPlace;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PreferencesGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.PreferencesPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlaceGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.SavedPlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.FriendRequestGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InvitationGetDTO;
// import ch.uzh.ifi.hase.soprafs26.rest.dto.PlacePostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPlaceGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPlacePostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically transform/map the internal representation of an entity 
 * (e.g., the User) to the external/API representation (e.g. UserGetDTO for getting, UserPostDTO for creating) and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for creating information (POST).
 */

@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);


	// ==================== User Mappings ====================
	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "password", target = "password")					
	// No creationDate, token or status, they will be set in the Service when creating user
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "token", target = "token")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "creationDate", target = "creationDate")
	UserGetDTO convertEntityToUserGetDTO(User user);
	// Do not expose data like password or token to client 

	// ==================== TravelBoard Mappings ====================
	@Mapping(source = "name", target = "name")
	@Mapping(source = "location", target = "location")
	@Mapping(source = "startDate", target = "startDate")
	@Mapping(source = "endDate", target = "endDate")
	@Mapping(source = "inviteCode", target = "inviteCode")
	@Mapping(source = "privacy", target = "privacy")
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
	@Mapping(target = "memberIds", ignore = true)
    TravelBoardGetDTO convertEntityToTravelBoardGetDTO(TravelBoard travelBoard);

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

	// ==================== Places Mappings ====================
	// @Mapping(source = "name", target = "name")
	// @Mapping(source = "latitude", target = "latitude")
	// @Mapping(source = "longitude", target = "longitude")
    // Place convertPlacePostDTOtoEntity(PlacePostDTO placePostDTO);

	// ==================== Friend Request Mappings ====================
	@Mapping(source = "id", target = "id")
	@Mapping(source = "sender.id", target = "senderId")
	@Mapping(source = "receiver.id", target = "receiverId")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "sender.username", target = "senderUsername")
    FriendRequestGetDTO convertEntityToFriendRequestGetDTO(FriendRequest createdFriendRequest);

	// ==================== Saved Places Mappings ====================
	@Mapping(source = "externalPlaceId", target = "externalPlaceId")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "address", target = "address")
	@Mapping(source = "rating", target = "rating")
	@Mapping(source = "photoReference", target = "photoReference")
	@Mapping(source = "lat", target = "lat")
	@Mapping(source = "lng", target = "lng")
	@Mapping(source = "types", target = "types")
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
	TravelBoardPlaceGetDTO convertEntityToTravelBoardPlaceGetDTO(TravelBoardPlace travelBoardPlace);

}

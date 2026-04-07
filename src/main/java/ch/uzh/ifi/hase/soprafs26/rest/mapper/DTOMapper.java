package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.TravelBoard;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TravelBoardPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

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

	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "password", target = "password")  		 
	@Mapping(source = "bio", target = "bio")					
	// No creationDate, token or status, they will be set in the Service when creating user
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "email", target = "email")
	@Mapping(source = "token", target = "token")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "bio", target = "bio")
	@Mapping(source = "creationDate", target = "creationDate")
	UserGetDTO convertEntityToUserGetDTO(User user);
	// Do not expose data like password or token to client 

	@Mapping(source = "name", target = "name")
	@Mapping(source = "startDate", target = "startDate")
	@Mapping(source = "endDate", target = "endDate")
	@Mapping(source = "privacy", target = "privacy")
    TravelBoard convertTravelBoardPostDTOtoEntity(TravelBoardPostDTO travelBoardPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "startDate", target = "startDate")
	@Mapping(source = "endDate", target = "endDate")
	@Mapping(source = "owner.id", target = "ownerId")
	@Mapping(source = "inviteCode", target = "inviteCode")
	@Mapping(source = "privacy", target = "privacy")
	@Mapping(source = "dateCreated", target = "dateCreated")
    TravelBoardGetDTO convertEntityToTravelBoardGetDTO(TravelBoard travelBoard);
}

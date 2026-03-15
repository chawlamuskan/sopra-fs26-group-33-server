package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "password", target = "password")  		// new add password 
	@Mapping(source = "bio", target = "bio")					// new add bio - no creationdate here, also not token and status, because of using MapStruct
																// they will be set in the service part when creating new user
																// they are not here, bc i only receive the data which the user types, doesnt type date  
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "id", target = "id")
	@Mapping(source = "name", target = "name")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "token", target = "token")
	@Mapping(source = "status", target = "status")
	@Mapping(source = "bio", target = "bio")					// new add bio
	@Mapping(source = "creationDate", target = "creationDate") // new add creation date
	UserGetDTO convertEntityToUserGetDTO(User user);
	// for get , dont expose data like password or token to client 
}

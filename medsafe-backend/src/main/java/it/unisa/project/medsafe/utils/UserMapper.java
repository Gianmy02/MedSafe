package it.unisa.project.medsafe.utils;

import it.unisa.project.medsafe.dto.UserDTO;
import it.unisa.project.medsafe.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper per la conversione tra Entity User e UserDTO.
 */
@Mapper(componentModel = "spring")
public abstract class UserMapper {

    /**
     * Converte un'entity User in un UserDTO
     */
    public abstract UserDTO userToUserDTO(User entity);

    /**
     * Converte un UserDTO in un'entity User.
     * Non imposta id e createdAt (gestiti dal database).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    public abstract User userDTOToUser(UserDTO dto);

    /**
     * Converte una lista di User in una lista di UserDTO
     */
    public abstract List<UserDTO> usersToUsersDTO(List<User> users);
}

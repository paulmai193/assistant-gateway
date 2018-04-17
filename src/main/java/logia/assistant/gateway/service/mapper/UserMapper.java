package logia.assistant.gateway.service.mapper;

import logia.assistant.gateway.domain.Authority;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.service.dto.UserDTO;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 * Mapper for the entity User and its DTO called UserDTO.
 * 
 * Normal mappers are generated using MapStruct, this one is hand-coded as MapStruct
 * support is still in beta, and requires a manual step with an IDE.
 *
 * @author Dai Mai
 */
@Service
public class UserMapper {

    @Inject
    private CredentialRepository credentialRepository;

    /**
     * User to user DTO.
     *
     * @param user the user
     * @return the user DTO
     */
    public UserDTO userToUserDTO(User user) {
        UserDTO userDTO = new UserDTO(user);
        if (this.credentialRepository.findWithUserByUserId(user.getId()).stream()
                .anyMatch(credential -> credential.isActivated())) {
            userDTO.setActivated(true);
        }
        else {
            userDTO.setActivated(false);
        }
        return userDTO;
    }

    /**
     * Users to user DT os.
     *
     * @param users the users
     * @return the list
     */
    public List<UserDTO> usersToUserDTOs(List<User> users) {
        return users.stream().filter(Objects::nonNull).map(this::userToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * User DTO to user.
     *
     * @param userDTO the user DTO
     * @return the user
     */
    public User userDTOToUser(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        else {
            User user = new User();
            user.setUuid(userDTO.getId());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setImageUrl(userDTO.getImageUrl());
            user.setLangKey(userDTO.getLangKey());
            Set<Authority> authorities = this.authoritiesFromStrings(userDTO.getAuthorities());
            if (authorities != null) {
                user.setAuthorities(authorities);
            }
            return user;
        }
    }

    /**
     * User DT os to users.
     *
     * @param userDTOs the user DT os
     * @return the list
     */
    public List<User> userDTOsToUsers(List<UserDTO> userDTOs) {
        return userDTOs.stream().filter(Objects::nonNull).map(this::userDTOToUser)
                .collect(Collectors.toList());
    }

    /**
     * User from id.
     *
     * @param id the id
     * @return the user
     */
    public User userFromUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        User user = new User();
        user.setUuid(uuid);
        return user;
    }

    /**
     * Authorities from strings.
     *
     * @param strings the strings
     * @return the sets the
     */
    public Set<Authority> authoritiesFromStrings(Set<String> strings) {
        return strings.stream().map(string -> {
            Authority auth = new Authority();
            auth.setName(string);
            return auth;
        }).collect(Collectors.toSet());
    }
}

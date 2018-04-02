package logia.assistant.gateway.service.mapper;

import logia.assistant.gateway.domain.*;
import logia.assistant.gateway.service.dto.CredentialDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Credential and its DTO CredentialDTO.
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CredentialMapper extends EntityMapper<CredentialDTO, Credential> {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "userLogin")
    CredentialDTO toDto(Credential credential);

    @Mapping(source = "userId", target = "user")
    Credential toEntity(CredentialDTO credentialDTO);

    default Credential fromId(Long id) {
        if (id == null) {
            return null;
        }
        Credential credential = new Credential();
        credential.setId(id);
        return credential;
    }
}

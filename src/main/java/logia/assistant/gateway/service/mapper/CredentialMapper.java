package logia.assistant.gateway.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.service.dto.CredentialDTO;

/**
 * Mapper for the entity Credential and its DTO CredentialDTO.
 *
 * @author Dai Mai
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CredentialMapper extends EntityMapper<CredentialDTO, Credential> {

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.mapper.EntityMapper#toDto(java.lang.Object)
     */
    @Mapping(source = "user.id", target = "userId")
    CredentialDTO toDto(Credential credential);

    /* (non-Javadoc)
     * @see logia.assistant.gateway.service.mapper.EntityMapper#toEntity(java.lang.Object)
     */
    @Mapping(source = "userId", target = "user")
    Credential toEntity(CredentialDTO credentialDTO);

    /**
     * From id.
     *
     * @param id the id
     * @return the credential
     */
    default Credential fromId(Long id) {
        if (id == null) {
            return null;
        }
        Credential credential = new Credential();
        credential.setId(id);
        return credential;
    }
}

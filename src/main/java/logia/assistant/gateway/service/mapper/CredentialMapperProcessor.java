package logia.assistant.gateway.service.mapper;

import java.util.Objects;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.service.dto.CredentialDTO;

/**
 * The Class CredentialMapperProcessor.
 *
 * @author Dai Mai
 */
@Component
public class CredentialMapperProcessor {
    
    /** The user repository. */
    private final UserRepository userRepository;
    
    /**
     * Instantiates a new credential mapper processor.
     *
     * @param userRepository the user repository
     */
    public CredentialMapperProcessor(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    /**
     * Sets the user.
     *
     * @param source the source
     * @param target the target
     */
    @AfterMapping()
    public void setUser(CredentialDTO source, @MappingTarget Credential target) {
        if (Objects.isNull(source) || Objects.isNull(target)) {
            return;
        }
        User user = this.userRepository.getOneByUuid(source.getUserId());
        target.setUser(user);
    }
    
}

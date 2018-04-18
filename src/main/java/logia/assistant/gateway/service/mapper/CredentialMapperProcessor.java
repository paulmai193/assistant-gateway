package logia.assistant.gateway.service.mapper;

import java.util.Objects;

import javax.inject.Inject;

import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.service.dto.CredentialDTO;

@Component
public class CredentialMapperProcessor {
    
    @Inject
    private UserRepository userRepository;
    
    @AfterMapping()
    public void setUser(CredentialDTO source, @MappingTarget Credential target) {
        if (Objects.isNull(source) || Objects.isNull(target)) {
            return;
        }
        User user = this.userRepository.getOne(source.getUserId());
        target.setUser(user);
    }
    
}

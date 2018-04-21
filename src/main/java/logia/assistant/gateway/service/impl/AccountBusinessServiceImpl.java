package logia.assistant.gateway.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.service.AccountBusinessService;
import logia.assistant.gateway.web.rest.errors.LoginAlreadyUsedException;

@Service
@Transactional
public final class AccountBusinessServiceImpl implements AccountBusinessService {

    /** The cache manager. */
    private final CacheManager               cacheManager;
    
    @Override
    public Credential updateByUserId(Long userId, String login) {
        Credential credential;
        Optional<Credential> existingCredential = this.findOneWithUserByLogin(login);
        if (existingCredential.isPresent()
                && (!existingCredential.get().getUser().getId().equals(userId))) {
            // another user already have this credential
            throw new LoginAlreadyUsedException();
        }
        List<Credential> currentCredentials = this.findByUserId(userId);
        Optional<Credential> optCredential = currentCredentials.stream()
                .filter(currentCredential -> currentCredential.getLogin().equals(login))
                .findFirst();
        if (!optCredential.isPresent()) {
            credential = Credential.clone(currentCredentials.get(0));
            credential.primary(false).login(login);
            credential = this.saveOrUpdate(credential, true);
            this.cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                    .evict(currentCredentials.get(0).getLogin());

            // TODO send validation email
        }
        else {
            credential = optCredential.get();
        }
        return credential;
    }

    @Override
    public Credential delete(String login) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Credential> activateRegistration(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Credential> requestPasswordReset(String mail) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Credential> findOneByResetKey(String resetKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeNotActivatedUsers() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Optional<Credential> findOneWithUserByLogin(String userLogin) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Credential> findByUserId(Long userId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Page<Credential> findAllByLoginNot(Pageable pageable, String login) {
        // TODO Auto-generated method stub
        return null;
    }

    private Credential saveOrUpdate(Credential credential, boolean persistent) {
        if (persistent) {
            credential = this.credentialRepository.saveAndFlush(credential);
        }
        else {
            credential = this.credentialRepository.save(credential);
        }
        this.credentialSearchRepository.save(credential);
        cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                .evict(credential.getLogin());
        return credential;
    }

}

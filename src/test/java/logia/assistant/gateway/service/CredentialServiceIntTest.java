package logia.assistant.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.AssistantGatewayApp;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.service.impl.CredentialServiceImpl;

/**
 * The Class CredentialServiceIntTest.
 *
 * @author Dai Mai
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
@Transactional
public class CredentialServiceIntTest extends AbstractUserServiceInitTest {

    /** The credential service. */
    @Autowired
    private CredentialServiceImpl credentialService; 
    
    /**
     * Inits the.
     */
    @Before
    public void init() {
        user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setFirstName("john");
        user.setLastName("doe");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");
        credential = new Credential().login("johndoe@localhost").activated(true).user(user);
    }
    
    /**
     * Assert that user must exist to reset password.
     */
    @Test
    @Transactional
    public void assertThatUserMustExistToResetPassword() {
        user = userRepository.saveAndFlush(user);
        credentialRepository.saveAndFlush(credential);
        
        Optional<Credential> maybeCredential = credentialService.requestPasswordReset("invalid.login@localhost");
        assertThat(maybeCredential).isNotPresent();

        maybeCredential = credentialService.requestPasswordReset(credential.getLogin());
        assertThat(maybeCredential).isPresent();
        assertThat(maybeCredential.orElse(null).getLogin()).isEqualTo(credential.getLogin());
        assertThat(maybeCredential.orElse(null).getResetDate()).isNotNull();
        assertThat(maybeCredential.orElse(null).getResetKey()).isNotNull();
    }
    
    /**
     * Assert that only activated user can request password reset.
     */
    @Test
    @Transactional
    public void assertThatOnlyActivatedUserCanRequestPasswordReset() {
        credential.setActivated(false);
        credentialRepository.saveAndFlush(credential);

        Optional<Credential> maybeCredential = credentialService.requestPasswordReset(credential.getLogin());
        assertThat(maybeCredential).isNotPresent();
        credentialRepository.delete(credential);
    }

    /**
     * Test find not activated users by creation date before.
     */
    @Test
    @Transactional
    public void testFindNotActivatedUsersByCreationDateBefore() {
        Instant now = Instant.now();
        credential.setActivated(false);
        Credential dbCredential = credentialRepository.saveAndFlush(credential);
        dbCredential.setCreatedDate(now.minus(4, ChronoUnit.DAYS));
        credentialRepository.saveAndFlush(credential);
        List<Credential> credentials = credentialRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS));
        assertThat(credentials).isNotEmpty();
        credentialService.removeNotActivatedUsers();
        credentials = credentialRepository.findAllByActivatedIsFalseAndCreatedDateBefore(now.minus(3, ChronoUnit.DAYS));
        assertThat(credentials).isEmpty();
    }

    /**
     * Test remove not activated users.
     */
    @Test
    @Transactional
    public void testRemoveNotActivatedUsers() {
        credential.setActivated(false);
        credentialRepository.saveAndFlush(credential);
        // Let the audit first set the creation date but then update it
        credential.setCreatedDate(Instant.now().minus(30, ChronoUnit.DAYS));
        credentialRepository.saveAndFlush(credential);

        assertThat(credentialRepository.findOneWithUserByLogin("johndoe")).isPresent();
        credentialService.removeNotActivatedUsers();
        assertThat(credentialRepository.findOneWithUserByLogin("johndoe")).isNotPresent();
    }

}

package logia.assistant.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import logia.assistant.gateway.service.util.RandomUtil;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
@Transactional
public class AccountBusinessServiceIntTest extends AbstractUserServiceInitTest {

    @Autowired
    private AccountBusinessService accountBusinessService;
    
    @Before
    public void setUp() throws Exception {
        user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setFirstName("john");
        user.setLastName("doe");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");
        credential = new Credential().login("johndoe@localhost").activated(true).primary(true).user(user);
    }
    
    /**
     * Assert that user must exist to reset password.
     */
    @Test
    @Transactional
    public void assertThatUserMustExistToResetPassword() {
        user = userRepository.saveAndFlush(user);
        credentialRepository.saveAndFlush(credential);
        
        Optional<Credential> maybeCredential = accountBusinessService.requestPasswordReset("invalid.login@localhost");
        assertThat(maybeCredential).isNotPresent();

        maybeCredential = accountBusinessService.requestPasswordReset(credential.getLogin());
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
        user = userRepository.saveAndFlush(user);
        credential.setActivated(false);
        credentialRepository.saveAndFlush(credential);

        Optional<Credential> maybeCredential = accountBusinessService.requestPasswordReset(credential.getLogin());
        assertThat(maybeCredential).isNotPresent();
        credentialRepository.delete(credential);
    }

    /**
     * Assert that reset key must not be older than 24 hours.
     */
    @Test
    @Transactional
    public void assertThatResetKeyMustNotBeOlderThan24Hours() {
        userRepository.saveAndFlush(user);
        
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        credential.activated(true).resetDate(daysAgo).resetKey(resetKey).primary(true).user(user);
        credentialRepository.saveAndFlush(credential);

        Optional<User> maybeUser = accountBusinessService.completePasswordReset("johndoe2", credential.getResetKey());
        assertThat(maybeUser).isNotPresent();
        
        credentialRepository.delete(credential);
    }

    /**
     * Assert that reset key must be valid.
     */
    @Test
    @Transactional
    public void assertThatResetKeyMustBeValid() {
        userRepository.saveAndFlush(user);
        
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        credential.activated(true).resetDate(daysAgo).resetKey("1234").primary(true).user(user);
        credentialRepository.saveAndFlush(credential);

        Optional<User> maybeUser = accountBusinessService.completePasswordReset("johndoe2", credential.getResetKey());
        assertThat(maybeUser).isNotPresent();
        
        credentialRepository.delete(credential);
    }

    /**
     * Assert that user can reset password.
     */
    @Test
    @Transactional
    public void assertThatUserCanResetPassword() {
        userRepository.saveAndFlush(user);
        
        String oldPassword = user.getPassword();
        Instant daysAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        credential.activated(true).resetDate(daysAgo).resetKey(resetKey).primary(true).user(user);
        credentialRepository.saveAndFlush(credential);

        Optional<User> maybeUser = accountBusinessService.completePasswordReset("johndoe2", credential.getResetKey());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getPassword()).isNotEqualTo(oldPassword);
        
        Optional<Credential> maybeCredential = credentialRepository.findOneWithUserByLogin(credential.getLogin());
        assertThat(maybeCredential.orElse(null).getResetDate()).isNull();
        assertThat(maybeCredential.orElse(null).getResetKey()).isNull();

        credentialRepository.delete(credential);
    }

}

package logia.assistant.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.AssistantGatewayApp;
import logia.assistant.gateway.config.Constants;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.service.util.RandomUtil;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
@Transactional
public class UserServiceIntTest extends AbstractUserServiceInitTest {

    /** The user service. */
    @Autowired
    private UserService userService;

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
     * Assert that reset key must not be older than 24 hours.
     */
    @Test
    @Transactional
    public void assertThatResetKeyMustNotBeOlderThan24Hours() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        credential.activated(true).resetDate(daysAgo).resetKey(resetKey);
        credentialRepository.saveAndFlush(credential);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", credential.getResetKey());
        assertThat(maybeUser).isNotPresent();
        
        credentialRepository.delete(credential);
    }

    /**
     * Assert that reset key must be valid.
     */
    @Test
    @Transactional
    public void assertThatResetKeyMustBeValid() {
        Instant daysAgo = Instant.now().minus(25, ChronoUnit.HOURS);
        credential.activated(true).resetDate(daysAgo).resetKey("1234");
        credentialRepository.saveAndFlush(credential);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", credential.getResetKey());
        assertThat(maybeUser).isNotPresent();
        
        credentialRepository.delete(credential);
    }

    /**
     * Assert that user can reset password.
     */
    @Test
    @Transactional
    public void assertThatUserCanResetPassword() {
        String oldPassword = user.getPassword();
        Instant daysAgo = Instant.now().minus(2, ChronoUnit.HOURS);
        String resetKey = RandomUtil.generateResetKey();
        credential.activated(true).resetDate(daysAgo).resetKey(resetKey);
        credentialRepository.saveAndFlush(credential);

        Optional<User> maybeUser = userService.completePasswordReset("johndoe2", credential.getResetKey());
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.orElse(null).getPassword()).isNotEqualTo(oldPassword);
        
        Optional<Credential> maybeCredential = credentialRepository.findOneByLogin(credential.getLogin());
        assertThat(maybeCredential.orElse(null).getResetDate()).isNull();
        assertThat(maybeCredential.orElse(null).getResetKey()).isNull();

        credentialRepository.delete(credential);
    }

    /**
     * Assert that anonymous user is not get.
     */
    @Test
    @Transactional
    public void assertThatAnonymousUserIsNotGet() {
        credential.setLogin(Constants.ANONYMOUS_USER);
        if (!credentialRepository.findOneByLogin(Constants.ANONYMOUS_USER).isPresent()) {
            credential = credentialRepository.saveAndFlush(credential);
        }
        final PageRequest pageable = new PageRequest(0, (int) userRepository.count());
        final Page<UserDTO> allManagedUsers = userService.getAllManagedUsers(pageable);
        assertThat(allManagedUsers.getContent().stream()
            .noneMatch(user -> Constants.ANONYMOUS_USER.equals(user.getLogin())))
            .isTrue();
    }

    /**
     * Test remove non credential users.
     */
    @Test
    @Transactional
    public void testRemoveNonCredentialUsers() {
        userRepository.saveAndFlush(user);
        // Let the audit first set the creation date but then update it
        user.setCreatedDate(Instant.now().minus(30, ChronoUnit.DAYS));
        userRepository.saveAndFlush(user);
        
        long currentTotalUsers = userRepository.count();
        userService.removeNonCredentialUsers();
        assertThat(userRepository.count()).isEqualByComparingTo(currentTotalUsers - 1);
    }

}

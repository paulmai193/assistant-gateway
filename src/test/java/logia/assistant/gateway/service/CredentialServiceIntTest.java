package logia.assistant.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Ignore;
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
@Ignore
public class CredentialServiceIntTest extends AbstractUserServiceInitTest {

    /** The credential service. */
    @Autowired
    private CredentialServiceImpl credentialService;

    /**
     * Inits the.
     */
    @Before
    public void init() {
        user = new User().password(RandomStringUtils.random(60)).firstName("john").lastName("doe")
                .imageUrl("http://placehold.it/50x50").langKey("en").activated(true);
        credential = new Credential().login("johndoe@localhost").primary(true).user(user);
    }

}

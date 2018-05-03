package logia.assistant.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.AssistantGatewayApp;
import logia.assistant.gateway.config.Constants;
import logia.assistant.gateway.domain.Authority;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.AuthorityRepository;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.service.AccountBusinessService;
import logia.assistant.gateway.service.MailService;
import logia.assistant.gateway.service.UserService;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.web.rest.errors.ExceptionTranslator;
import logia.assistant.gateway.web.rest.vm.KeyAndPasswordVM;
import logia.assistant.gateway.web.rest.vm.ManagedUserVM;
import logia.assistant.share.gateway.securiry.jwt.AuthoritiesConstants;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see AccountResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
public class AccountResourceIntTest {

    /** The user repository. */
    @Autowired
    private UserRepository         userRepository;

    /** The authority repository. */
    @Autowired
    private AuthorityRepository    authorityRepository;

    /** The user service. */
    @Autowired
    private UserService            userService;

    /** The password encoder. */
    @Autowired
    private PasswordEncoder        passwordEncoder;

    /** The http message converters. */
    @Autowired
    private HttpMessageConverter[] httpMessageConverters;

    /** The exception translator. */
    @Autowired
    private ExceptionTranslator    exceptionTranslator;

    /** The mock user service. */
    @Mock
    private UserService            mockUserService;

    /** The mock mail service. */
    @Mock
    private MailService            mockMailService;

    /** The rest mvc. */
    private MockMvc                restMvc;

    /** The rest user mock mvc. */
    private MockMvc                restUserMockMvc;

    /** The credential repository. */
    @Autowired
    private CredentialRepository   credentialRepository;

    @Autowired
    private AccountBusinessService accountBusinessService;
    
    @Mock
    private AccountBusinessService mockAccountBusinessService;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendActivationEmail(anyObject());
        AccountResource accountResource = new AccountResource(accountBusinessService, userService);
        AccountResource accountUserMockResource = new AccountResource(mockAccountBusinessService, mockUserService);
        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource)
                .setMessageConverters(httpMessageConverters)
                .setControllerAdvice(exceptionTranslator).build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource)
                .setControllerAdvice(exceptionTranslator).build();
    }

    /**
     * Test non authenticated user.
     *
     * @throws Exception the exception
     */
    @Test
    public void testNonAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(""));
    }

    /**
     * Test authenticated user.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAuthenticatedUser() throws Exception {
        restUserMockMvc.perform(get("/api/authenticate").with(request -> {
            request.setRemoteUser("test");
            return request;
        }).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().string("test"));
    }

    /**
     * Test get existing account.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.ADMIN);
        authorities.add(authority);

        User user = new User();
        user.setFirstName("john");
        user.setLastName("doe");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");
        user.setAuthorities(authorities);
        when(mockUserService.getUserWithAuthorities()).thenReturn(Optional.of(user));

        restUserMockMvc.perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
//                .andExpect(jsonPath("$.login").value("test"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("doe"))
                .andExpect(jsonPath("$.imageUrl").value("http://placehold.it/50x50"))
                .andExpect(jsonPath("$.langKey").value("en"))
                .andExpect(jsonPath("$.authorities").value(AuthoritiesConstants.ADMIN));
    }

    /**
     * Test get unknown account.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(Optional.empty());

        restUserMockMvc.perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Test register valid.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("joe@localhost");
        validUser.setPassword("password");
        validUser.setFirstName("Joe");
        validUser.setLastName("Shmoe");
        validUser.setActivated(true);
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        assertThat(credentialRepository.findOneWithUserByLogin("joe").isPresent()).isFalse();

        restMvc.perform(post("/api/register").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
                .andExpect(status().isCreated());

        Credential testCredential = credentialRepository.findOneWithUserByLogin("joe@localhost").orElse(null);
        assertThat(testCredential).isNotNull();
        assertThat(testCredential.getUser().getUuid()).isNotBlank();
    }

    /**
     * Test register invalid login.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("funky-log!n");// <-- invalid
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc
                .perform(post("/api/register").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
                .andExpect(status().isBadRequest());

        assertThat(credentialRepository.findOneByLoginIgnoreCase("funky@example.com").isPresent())
                .isFalse();
    }

    /**
     * Test register invalid password.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("123");// password with only 3 digits
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc
                .perform(post("/api/register").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
                .andExpect(status().isBadRequest());

        assertThat(credentialRepository.findOneWithUserByLogin("bob").isPresent()).isFalse();
    }

    /**
     * Test register null password.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRegisterNullPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword(null);// invalid null password
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc
                .perform(post("/api/register").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
                .andExpect(status().isBadRequest());

        assertThat(credentialRepository.findOneWithUserByLogin("bob").isPresent()).isFalse();
    }

    /**
     * Test register duplicate login.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // Good
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("alice");
        validUser.setPassword("password");
        validUser.setFirstName("Alice");
        validUser.setLastName("Something");
        validUser.setActivated(true);
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Duplicate login, different email
        ManagedUserVM duplicatedUser = new ManagedUserVM();
        duplicatedUser.setLogin(validUser.getLogin());
        duplicatedUser.setPassword(validUser.getPassword());
        duplicatedUser.setFirstName(validUser.getFirstName());
        duplicatedUser.setLastName(validUser.getLastName());
        duplicatedUser.setActivated(validUser.isActivated());
        duplicatedUser.setImageUrl(validUser.getImageUrl());
        duplicatedUser.setLangKey(validUser.getLangKey());
        duplicatedUser.setCreatedBy(validUser.getCreatedBy());
        duplicatedUser.setCreatedDate(validUser.getCreatedDate());
        duplicatedUser.setLastModifiedBy(validUser.getLastModifiedBy());
        duplicatedUser.setLastModifiedDate(validUser.getLastModifiedDate());
        duplicatedUser.setAuthorities(new HashSet<>(validUser.getAuthorities()));

        // Good user
        restMvc.perform(post("/api/register").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
                .andExpect(status().isCreated());

        // Duplicate login
        restMvc.perform(post("/api/register").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
                .andExpect(status().is4xxClientError());

        assertThat(credentialRepository.findOneByLoginIgnoreCase("alicejr@example.com").isPresent())
                .isFalse();
    }

    /**
     * Test register admin is ignored.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("badguy");
        validUser.setPassword("password");
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setActivated(true);
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restMvc.perform(post("/api/register").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
                .andExpect(status().isCreated());

        Optional<Credential> credDup = credentialRepository.findOneWithUserByLogin("badguy");
        assertThat(credDup.isPresent()).isTrue();
        assertThat(credDup.get().getUser().getAuthorities()).hasSize(1)
                .containsExactly(authorityRepository.findOne(AuthoritiesConstants.USER));
    }

    /**
     * Test activate account.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testActivateAccount() throws Exception {
        final String activationKey = "some activation key";
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().activated(false).activationKey(activationKey).primary(true)
                .login("activate-account").user(user);
        credential = credentialRepository.saveAndFlush(credential);

        restMvc.perform(get("/api/activate?key={activationKey}", activationKey))
                .andExpect(status().isOk());

        credential = credentialRepository.findOneWithUserByLogin(credential.getLogin())
                .orElse(null);
        assertThat(credential.isActivated()).isTrue();
    }

    /**
     * Test activate account with wrong key.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testActivateAccountWithWrongKey() throws Exception {
        restMvc.perform(get("/api/activate?key=wrongActivationKey"))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Test save account.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    @WithMockUser("save-account")
    public void testSaveAccount() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);
        user = UserResourceIntTest.setUuidForUser(user);
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().activated(true).login("save-account").primary(true).user(user);
        credential = credentialRepository.saveAndFlush(credential);

        UserDTO userDTO = new UserDTO();
        userDTO.setLogin("not-used"); // This test must be unsave
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN)); // This test must
                                                                                   // be unsave

        restMvc.perform(put("/api/account").contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userDTO))).andExpect(status().isOk());

        User updatedUser = userRepository.findOneWithAuthoritiesById(credential.getUser().getId())
                .orElse(null);
        assertThat(updatedUser.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(updatedUser.getLangKey()).isEqualTo(userDTO.getLangKey());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.getImageUrl()).isEqualTo(userDTO.getImageUrl());
        assertThat(updatedUser.getAuthorities()).isEmpty();
    }

    /**
     * Test change password.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    @WithMockUser("change-password")
    public void testChangePassword() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("change-password").activated(true).primary(true).user(user);
        credential = credentialRepository.saveAndFlush(credential);

        restMvc.perform(post("/api/account/change-password").content("new password"))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findOneWithAuthoritiesById(credential.getUser().getId())
                .orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();
    }

    /**
     * Test change password too small.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    @WithMockUser("change-password-too-small")
    public void testChangePasswordTooSmall() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("change-password-too-small").activated(true).primary(true).user(user);
        credential = credentialRepository.saveAndFlush(credential);

        restMvc.perform(post("/api/account/change-password").content("new"))
                .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOne(credential.getUser().getId());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    /**
     * Test change password too long.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    @WithMockUser("change-password-too-long")
    public void testChangePasswordTooLong() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("change-password-too-long").activated(true).primary(true).user(user);
        credential = credentialRepository.saveAndFlush(credential);

        restMvc.perform(post("/api/account/change-password").content(RandomStringUtils.random(101)))
                .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOne(credential.getUser().getId());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    /**
     * Test change password empty.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    @WithMockUser("change-password-empty")
    public void testChangePasswordEmpty() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("change-password-empty").activated(true).primary(true).user(user);
        credential = credentialRepository.saveAndFlush(credential);

        restMvc.perform(post("/api/account/change-password").content(RandomStringUtils.random(0)))
                .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOne(credential.getUser().getId());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    /**
     * Test request password reset.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRequestPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("password-reset@example.com").activated(true).primary(true)
                .user(user);
        credential = credentialRepository.saveAndFlush(credential);

        restMvc.perform(
                post("/api/account/reset-password/init").content("password-reset@example.com"))
                .andExpect(status().isOk());
    }

    /**
     * Test request password reset upper case email.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testRequestPasswordResetUpperCaseEmail() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("password-reset@example.com").activated(true).primary(true)
                .user(user);
        credential = credentialRepository.saveAndFlush(credential);

        restMvc.perform(
                post("/api/account/reset-password/init").content("password-reset@EXAMPLE.COM"))
                .andExpect(status().isOk());
    }

    /**
     * Test request password reset wrong email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRequestPasswordResetWrongEmail() throws Exception {
        restMvc.perform(post("/api/account/reset-password/init")
                .content("password-reset-wrong-email@example.com"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test finish password reset.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testFinishPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("finish-password-reset@example.com")
                .resetDate(Instant.now().plusSeconds(60)).resetKey("reset key").activated(true).primary(false).user(user);
        credential = credentialRepository.saveAndFlush(credential);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(credential.getResetKey());
        keyAndPassword.setNewPassword("new password");

        restMvc.perform(post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findOne(credential.getUser().getId());
        assertThat(
                passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword()))
                        .isTrue();
    }

    /**
     * Test finish password reset and activated automatically.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testFinishPasswordResetAndActivatedAutomatically() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("finish-password-reset@example.com")
                .resetDate(Instant.now().plusSeconds(60)).resetKey("reset key").activated(false).primary(true)
                .user(user);
        credential = credentialRepository.saveAndFlush(credential);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(credential.getResetKey());
        keyAndPassword.setNewPassword("new password");

        restMvc.perform(post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findOne(credential.getUser().getId());
        assertThat(
                passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword()))
                        .isTrue();
        Credential updateCredential = credentialRepository.findOne(credential.getId());
        assertThat(updateCredential.isActivated()).isTrue();
    }

    /**
     * Test finish password reset too big.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testFinishPasswordResetTooBig() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user = userRepository.saveAndFlush(user);

        Credential credential = new Credential().login("finish-password-reset-too-big@example.com")
                .resetDate(Instant.now().plusSeconds(60)).resetKey("reset key")
                .activated(true).primary(true)
                .user(user);
        credential = credentialRepository.saveAndFlush(credential);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("this is the very big reset key");
        keyAndPassword.setNewPassword("foo");

        restMvc.perform(post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
                .andExpect(status().isBadRequest());

        User updatedUser = userRepository.findOne(credential.getUser().getId());
        assertThat(
                passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword()))
                        .isFalse();
    }

    /**
     * Test finish password reset wrong key.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        restMvc.perform(post("/api/account/reset-password/finish")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
                .andExpect(status().isInternalServerError());
    }
}

package logia.assistant.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import logia.assistant.gateway.AssistantGatewayApp;
import logia.assistant.gateway.domain.Authority;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.repository.CredentialRepository;
import logia.assistant.gateway.repository.UserRepository;
import logia.assistant.gateway.repository.search.UserSearchRepository;
import logia.assistant.gateway.service.MailService;
import logia.assistant.gateway.service.UserService;
import logia.assistant.gateway.service.dto.UserDTO;
import logia.assistant.gateway.service.impl.CredentialServiceImpl;
import logia.assistant.gateway.service.mapper.UserMapper;
import logia.assistant.gateway.web.rest.errors.ExceptionTranslator;
import logia.assistant.gateway.web.rest.vm.ManagedUserVM;
import logia.assistant.share.gateway.securiry.jwt.AuthoritiesConstants;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
public class UserResourceIntTest {

    /** The Constant DEFAULT_LOGIN. */
    public static final String                    DEFAULT_LOGIN     = "johndoe";

    /** The Constant UPDATED_LOGIN. */
    public static final String                    UPDATED_LOGIN     = "jhipster";

    /** The Constant DEFAULT_ID. */
    public static final Long                      DEFAULT_ID        = 1L;

    public static final String                    DEFAULT_UUID      = "1";

    /** The Constant DEFAULT_PASSWORD. */
    public static final String                    DEFAULT_PASSWORD  = "passjohndoe";

    /** The Constant UPDATED_PASSWORD. */
    public static final String                    UPDATED_PASSWORD  = "passjhipster";

    /** The Constant DEFAULT_FIRSTNAME. */
    public static final String                    DEFAULT_FIRSTNAME = "john";

    /** The Constant UPDATED_FIRSTNAME. */
    public static final String                    UPDATED_FIRSTNAME = "jhipsterFirstName";

    /** The Constant DEFAULT_LASTNAME. */
    public static final String                    DEFAULT_LASTNAME  = "doe";

    /** The Constant UPDATED_LASTNAME. */
    public static final String                    UPDATED_LASTNAME  = "jhipsterLastName";

    /** The Constant DEFAULT_IMAGEURL. */
    public static final String                    DEFAULT_IMAGEURL  = "http://placehold.it/50x50";

    /** The Constant UPDATED_IMAGEURL. */
    public static final String                    UPDATED_IMAGEURL  = "http://placehold.it/40x40";

    /** The Constant DEFAULT_LANGKEY. */
    public static final String                    DEFAULT_LANGKEY   = "en";

    /** The Constant UPDATED_LANGKEY. */
    public static final String                    UPDATED_LANGKEY   = "fr";

    /** The user repository. */
    @Autowired
    private UserRepository                        userRepository;

    /** The user search repository. */
    @Autowired
    private UserSearchRepository                  userSearchRepository;

    /** The credential repostitory. */
    @Autowired
    private CredentialRepository                  credentialRepostitory;

    /** The mail service. */
    @Autowired
    private MailService                           mailService;

    /** The user service. */
    @Autowired
    private UserService                           userService;

    /** The credential service. */
    @Autowired
    private CredentialServiceImpl                 credentialService;

    /** The user mapper. */
    @Autowired
    private UserMapper                            userMapper;

    /** The jackson message converter. */
    @Autowired
    private MappingJackson2HttpMessageConverter   jacksonMessageConverter;

    /** The pageable argument resolver. */
    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    /** The exception translator. */
    @Autowired
    private ExceptionTranslator                   exceptionTranslator;

    /** The em. */
    @Autowired
    private EntityManager                         em;

    /** The cache manager. */
    @Autowired
    private CacheManager                          cacheManager;

    /** The rest user mock mvc. */
    private MockMvc                               restUserMockMvc;

    /** The user. */
    private User                                  user;

    /** The credential. */
    private Credential                            credential;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        cacheManager.getCache(UserRepository.USERS_BY_UUID_CACHE).clear();
        cacheManager.getCache(UserRepository.USERS_BY_FIRST_NAME_CACHE).clear();
        cacheManager.getCache(UserRepository.USERS_BY_LAST_NAME_CACHE).clear();
        UserResource userResource = new UserResource(userService, userSearchRepository);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create a User.
     * 
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     *
     * @param em the em
     * @return the user
     */
    public static User createEntity(EntityManager em) {
        User user = new User();
        user.setPassword(RandomStringUtils.random(60));
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setImageUrl(DEFAULT_IMAGEURL);
        user.setLangKey(DEFAULT_LANGKEY);
        return user;
    }

    /**
     * Inits the test.
     */
    @Before
    public void initTest() {
        user = createEntity(em);
    }

    public void clearTest() {
        userRepository.deleteAll();
        cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE).clear();
    }

    /**
     * Creates the user.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        // Create the User
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc
                .perform(post("/api/users").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
                .andExpect(status().isCreated());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate + 1);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(testUser.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(testUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(testUser.getUuid()).isNotBlank();
        Optional<Credential> maybeCredential = credentialRepostitory.findOneWithUserByLogin(DEFAULT_LOGIN);
        assertThat(maybeCredential).isNotEmpty();
    }

    /**
     * Creates the user with existing id.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void createUserWithExistingUuid() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(DEFAULT_UUID); // exist UUID
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc
                .perform(post("/api/users").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);
    }

    /**
     * Creates the user with existing login.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        CredentialResourceIntTest.createEntity(em, user);
        userSearchRepository.save(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(CredentialResourceIntTest.DEFAULT_LOGIN);// this login should already
                                                                        // be used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(DEFAULT_IMAGEURL);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Create the User
        restUserMockMvc
                .perform(post("/api/users").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
                .andExpect(status().isBadRequest());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeCreate);

        clearTest();
    }

    /**
     * Gets the all users.
     *
     * @return the all users
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void getAllUsers() throws Exception {
        // Initialize the database
        CredentialResourceIntTest.createEntity(em, user);
        userSearchRepository.save(user);

        // Get all the users
        restUserMockMvc.perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.[*].login")
                        .value(hasItem(CredentialResourceIntTest.DEFAULT_LOGIN)))
                .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
                .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
                .andExpect(jsonPath("$.[*].imageUrl").value(hasItem(DEFAULT_IMAGEURL)))
                .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));

        clearTest();
    }

    /**
     * Gets the user.
     *
     * @return the user
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void getUser() throws Exception {
        // Initialize the database
        CredentialResourceIntTest.createEntity(em, user);
        userSearchRepository.save(user);

        assertThat(cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                .get(CredentialResourceIntTest.DEFAULT_LOGIN)).isNull();

        // Get the user
        restUserMockMvc.perform(get("/api/users/{login}", CredentialResourceIntTest.DEFAULT_LOGIN))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
                .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
                .andExpect(jsonPath("$.imageUrl").value(DEFAULT_IMAGEURL))
                .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));

        assertThat(cacheManager.getCache(CredentialRepository.CREDENTIALS_BY_LOGIN_CACHE)
                .get(CredentialResourceIntTest.DEFAULT_LOGIN)).isNotNull();

        clearTest();
    }

    /**
     * Gets the non existing user.
     *
     * @return the non existing user
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown")).andExpect(status().isNotFound());
    }

    /**
     * Update user.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void updateUser() throws Exception {
        // Initialize the database
        CredentialResourceIntTest.createEntity(em, user);
        userSearchRepository.save(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findOne(user.getId());

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getUuid());
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(UPDATED_IMAGEURL);
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc
                .perform(put("/api/users").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
                .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);

        clearTest();
    }

    /**
     * Update user login.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void updateUserLogin() throws Exception {
        // Initialize the database
        CredentialResourceIntTest.createEntity(em, user);
        userSearchRepository.save(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        User updatedUser = userRepository.findOne(user.getId());

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getUuid());
        managedUserVM.setLogin(UPDATED_LOGIN);
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(UPDATED_IMAGEURL);
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc
                .perform(put("/api/users").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
                .andExpect(status().isOk());

        // Validate the User in the database
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeUpdate);
        User testUser = userList.get(userList.size() - 1);
        assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
        assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
        assertThat(testUser.getImageUrl()).isEqualTo(UPDATED_IMAGEURL);
        assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);

        Optional<Credential> testCredential = credentialRepostitory
                .findOneWithUserBylogin(UPDATED_LOGIN);
        assertThat(testCredential).isNotEmpty();
        Optional<Credential> emtpyCredential = credentialRepostitory
                .findOneWithUserBylogin(DEFAULT_LOGIN);
        assertThat(emtpyCredential).isEmpty();

        clearTest();
    }

    /**
     * Update user existing login.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void updateUserExistingLogin() throws Exception {
        // Initialize the database
        CredentialResourceIntTest.createEntity(em, user);
        userSearchRepository.save(user);

        User anotherUser = new User();
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setImageUrl("");
        anotherUser.setLangKey("en");
        anotherUser = userRepository.saveAndFlush(anotherUser);
        userSearchRepository.save(anotherUser);

        Credential anotherCredential = new Credential().login("jhipster").activated(true)
                .primary(true).user(anotherUser);
        credentialRepostitory.saveAndFlush(anotherCredential);

        // Update the user
        User updatedUser = userRepository.findOne(user.getId());

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getUuid());
        managedUserVM.setLogin("jhipster");// this login should already be used by anotherUser
        managedUserVM.setPassword(updatedUser.getPassword());
        managedUserVM.setFirstName(updatedUser.getFirstName());
        managedUserVM.setLastName(updatedUser.getLastName());
        managedUserVM.setActivated(true);
        managedUserVM.setImageUrl(updatedUser.getImageUrl());
        managedUserVM.setLangKey(updatedUser.getLangKey());
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        restUserMockMvc
                .perform(put("/api/users").contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
                .andExpect(status().isBadRequest());

        clearTest();
    }

    /**
     * Delete user.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void deleteUser() throws Exception {
        // Initialize the database
        CredentialResourceIntTest.createEntity(em, user);
        userSearchRepository.save(user);
        int databaseSizeBeforeDelete = userRepository.findAll().size();

        // Delete the user
        restUserMockMvc.perform(
                delete("/api/users/{uuid}", user.getUuid()).accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        assertThat(cacheManager.getCache(UserRepository.USERS_BY_UUID_CACHE).get(user.getUuid()))
                .isNull();
        assertThat(cacheManager.getCache(UserRepository.USERS_BY_FIRST_NAME_CACHE)
                .get(user.getFirstName())).isNull();
        assertThat(cacheManager.getCache(UserRepository.USERS_BY_LAST_NAME_CACHE)
                .get(user.getLastName())).isNull();

        // Validate the database is empty
        List<User> userList = userRepository.findAll();
        assertThat(userList).hasSize(databaseSizeBeforeDelete - 1);

        clearTest();
    }

    /**
     * Gets the all authorities.
     *
     * @return the all authorities
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void getAllAuthorities() throws Exception {
        restUserMockMvc
                .perform(get("/api/users/authorities").accept(TestUtil.APPLICATION_JSON_UTF8)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(containsInAnyOrder(AuthoritiesConstants.USER,
                        AuthoritiesConstants.SYSTEM, AuthoritiesConstants.ADMIN)));
    }

    /**
     * Test user equals.
     *
     * @throws Exception the exception
     */
    @Test
    @Transactional
    public void testUserEquals() throws Exception {
        TestUtil.equalsVerifier(User.class);
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(user1.getId());
        assertThat(user1).isEqualTo(user2);
        user2.setId(2L);
        assertThat(user1).isNotEqualTo(user2);
        user1.setId(null);
        assertThat(user1).isNotEqualTo(user2);
    }

    /**
     * Test user from id.
     */
    @Test
    public void testUserFromId() {
        assertThat(userMapper.userFromUuid(DEFAULT_UUID).getUuid()).isEqualTo(DEFAULT_UUID);
        assertThat(userMapper.userFromUuid(null)).isNull();
    }

    /**
     * Test user DT oto user.
     */
    @Test
    public void testUserDTOtoUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(DEFAULT_UUID);
        userDTO.setLogin(DEFAULT_LOGIN);
        userDTO.setFirstName(DEFAULT_FIRSTNAME);
        userDTO.setLastName(DEFAULT_LASTNAME);
        userDTO.setActivated(true);
        userDTO.setImageUrl(DEFAULT_IMAGEURL);
        userDTO.setLangKey(DEFAULT_LANGKEY);
        userDTO.setCreatedBy(DEFAULT_LOGIN);
        userDTO.setLastModifiedBy(DEFAULT_LOGIN);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        User user = userMapper.userDTOToUser(userDTO);
        assertThat(user.getUuid()).isEqualTo(DEFAULT_UUID);
        assertThat(user.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(user.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(user.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(user.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(user.getCreatedBy()).isNull();
        assertThat(user.getCreatedDate()).isNotNull();
        assertThat(user.getLastModifiedBy()).isNull();
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(user.getAuthorities()).extracting("name")
                .containsExactly(AuthoritiesConstants.USER);
    }

    /**
     * Test user to user DTO.
     */
    @Test
    public void testUserToUserDTO() {
        user.setId(DEFAULT_ID);
        user.setUuid(DEFAULT_UUID);
        user.setCreatedBy(DEFAULT_LOGIN);
        user.setCreatedDate(Instant.now());
        user.setLastModifiedBy(DEFAULT_LOGIN);
        user.setLastModifiedDate(Instant.now());
        Set<Authority> authorities = new HashSet<>();
        Authority authority = new Authority();
        authority.setName(AuthoritiesConstants.USER);
        authorities.add(authority);
        user.setAuthorities(authorities);

        UserDTO userDTO = userMapper.userToUserDTO(user);

        assertThat(userDTO.getId()).isEqualTo(DEFAULT_UUID);
        assertThat(userDTO.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(userDTO.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(userDTO.isActivated()).isEqualTo(true);
        assertThat(userDTO.getImageUrl()).isEqualTo(DEFAULT_IMAGEURL);
        assertThat(userDTO.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(userDTO.getCreatedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getCreatedDate()).isEqualTo(user.getCreatedDate());
        assertThat(userDTO.getLastModifiedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getLastModifiedDate()).isEqualTo(user.getLastModifiedDate());
        assertThat(userDTO.getAuthorities()).containsExactly(AuthoritiesConstants.USER);
        assertThat(userDTO.toString()).isNotNull();
    }

    /**
     * Test authority equals.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAuthorityEquals() throws Exception {
        Authority authorityA = new Authority();
        assertThat(authorityA).isEqualTo(authorityA);
        assertThat(authorityA).isNotEqualTo(null);
        assertThat(authorityA).isNotEqualTo(new Object());
        assertThat(authorityA.hashCode()).isEqualTo(0);
        assertThat(authorityA.toString()).isNotNull();

        Authority authorityB = new Authority();
        assertThat(authorityA).isEqualTo(authorityB);

        authorityB.setName(AuthoritiesConstants.ADMIN);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityA.setName(AuthoritiesConstants.USER);
        assertThat(authorityA).isNotEqualTo(authorityB);

        authorityB.setName(AuthoritiesConstants.USER);
        assertThat(authorityA).isEqualTo(authorityB);
        assertThat(authorityA.hashCode()).isEqualTo(authorityB.hashCode());
    }
}

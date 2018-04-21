package logia.assistant.gateway.cucumber.stepdefs;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import logia.assistant.gateway.repository.search.UserSearchRepository;
import logia.assistant.gateway.service.AccountBusinessService;
import logia.assistant.gateway.service.UserService;
import logia.assistant.gateway.web.rest.UserResource;
import logia.assistant.gateway.web.rest.errors.ExceptionTranslator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The Class UserStepDefs.
 *
 * @author Dai Mai
 */
public class UserStepDefs extends StepDefs {

    /** The rest user mock mvc. */
    private MockMvc              restUserMockMvc;

    /** The user resource. */
    @Autowired
    private UserResource         userResource;

    /** The user service. */
    @Autowired
    private UserService          userService;

    /** The user search repository. */
    @Autowired
    private UserSearchRepository userSearchRepository;
    
    /** The jackson message converter. */
    @Autowired
    private MappingJackson2HttpMessageConverter   jacksonMessageConverter;

    /** The pageable argument resolver. */
    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    /** The exception translator. */
    @Autowired
    private ExceptionTranslator                   exceptionTranslator;

    /** The account business service. */
    @Autowired
    private AccountBusinessService accountBusinessService;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        userResource = new UserResource(accountBusinessService, userService, userSearchRepository);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource)
                .setCustomArgumentResolvers(pageableArgumentResolver)
                .setControllerAdvice(exceptionTranslator)
                .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * I search user admin.
     *
     * @param userId the user id
     * @throws Throwable the throwable
     */
    @When("^I search user '(.*)'$")
    public void i_search_user_admin(String userId) throws Throwable {
        actions = restUserMockMvc
                .perform(get("/api/users/" + userId).accept(MediaType.APPLICATION_JSON));
    }

    /**
     * The user is found.
     *
     * @throws Throwable the throwable
     */
    @Then("^the user is found$")
    public void the_user_is_found() throws Throwable {
        actions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
    }

    /**
     * His last name is.
     *
     * @param lastName the last name
     * @throws Throwable the throwable
     */
    @Then("^his last name is '(.*)'$")
    public void his_last_name_is(String lastName) throws Throwable {
        actions.andExpect(jsonPath("$.lastName").value(lastName));
    }

}

package logia.assistant.gateway.cucumber.stepdefs;

import cucumber.api.java.Before;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import logia.assistant.gateway.web.rest.UserResource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The Class UserStepDefs.
 *
 * @author Dai Mai
 */
public class UserStepDefs extends StepDefs {

    /** The user resource. */
    @Autowired
    private UserResource userResource;

    /** The rest user mock mvc. */
    private MockMvc restUserMockMvc;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

    /**
     * I search user admin.
     *
     * @param userId the user id
     * @throws Throwable the throwable
     */
    @When("^I search user '(.*)'$")
    public void i_search_user_admin(String userId) throws Throwable {
        actions = restUserMockMvc.perform(get("/api/users/" + userId)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * The user is found.
     *
     * @throws Throwable the throwable
     */
    @Then("^the user is found$")
    public void the_user_is_found() throws Throwable {
        actions
            .andExpect(status().isOk())
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

package logia.assistant.gateway.cucumber.stepdefs;

import logia.assistant.gateway.AssistantGatewayApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * The Class StepDefs.
 *
 * @author Dai Mai
 */
@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = AssistantGatewayApp.class)
public abstract class StepDefs {

    /** The actions. */
    protected ResultActions actions;

}

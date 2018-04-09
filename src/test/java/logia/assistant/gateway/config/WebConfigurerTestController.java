package logia.assistant.gateway.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class WebConfigurerTestController.
 *
 * @author Dai Mai
 */
@RestController
public class WebConfigurerTestController {

    /**
     * Test cors on api path.
     */
    @GetMapping("/api/test-cors")
    public void testCorsOnApiPath() {
    }

    /**
     * Test cors on other path.
     */
    @GetMapping("/test/test-cors")
    public void testCorsOnOtherPath() {
    }
}

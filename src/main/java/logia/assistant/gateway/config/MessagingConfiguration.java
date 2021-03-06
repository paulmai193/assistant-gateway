package logia.assistant.gateway.config;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;

/**
 * Configures Spring Cloud Stream support.
 * 
 * See http://docs.spring.io/spring-cloud-stream/docs/current/reference/htmlsingle/
 * for more information.
 *
 * @author Dai Mai
 */
@EnableBinding(value = {Source.class})
public class MessagingConfiguration {

}

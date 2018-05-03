package logia.assistant.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;

import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.google.common.collect.ImmutableMap;

import io.github.jhipster.config.JHipsterProperties;
import logia.assistant.gateway.AssistantGatewayApp;
import logia.assistant.gateway.config.Constants;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;
import logia.assistant.gateway.service.util.RandomUtil;
import logia.assistant.gateway.service.validator.ValidatorService;

/**
 * The Class MailServiceIntTest.
 *
 * @author Dai Mai
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AssistantGatewayApp.class)
public class MailServiceIntTest {

    /** The j hipster properties. */
    @Autowired
    private JHipsterProperties   jHipsterProperties;

    /** The message source. */
    @Autowired
    private MessageSource        messageSource;

    /** The template engine. */
    @Autowired
    private SpringTemplateEngine templateEngine;

    /** The java mail sender. */
    @Spy
    private JavaMailSenderImpl   javaMailSender;

    /** The message captor. */
    @Captor
    private ArgumentCaptor       messageCaptor;

    /** The mail service. */
    private MailService          mailService;
    
    @Autowired
    private ValidatorService validatorService;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        mailService = new MailService(jHipsterProperties, javaMailSender, messageSource,
                templateEngine);
    }

    /**
     * Test send email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("john.doe@example.com");
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent()).isInstanceOf(String.class);
        assertThat(message.getContent().toString()).isEqualTo("testContent");
        assertThat(message.getDataHandler().getContentType())
                .isEqualTo("text/plain; charset=UTF-8");
    }

    /**
     * Test send html email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendHtmlEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, true);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("john.doe@example.com");
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent()).isInstanceOf(String.class);
        assertThat(message.getContent().toString()).isEqualTo("testContent");
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    /**
     * Test send multipart email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendMultipartEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", true, false);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        MimeMultipart mp = (MimeMultipart) message.getContent();
        MimeBodyPart part = (MimeBodyPart) ((MimeMultipart) mp.getBodyPart(0).getContent())
                .getBodyPart(0);
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        part.writeTo(aos);
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("john.doe@example.com");
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent()).isInstanceOf(Multipart.class);
        assertThat(aos.toString()).isEqualTo("\r\ntestContent");
        assertThat(part.getDataHandler().getContentType()).isEqualTo("text/plain; charset=UTF-8");
    }

    /**
     * Test send multipart html email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendMultipartHtmlEmail() throws Exception {
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", true, true);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        MimeMultipart mp = (MimeMultipart) message.getContent();
        MimeBodyPart part = (MimeBodyPart) ((MimeMultipart) mp.getBodyPart(0).getContent())
                .getBodyPart(0);
        ByteArrayOutputStream aos = new ByteArrayOutputStream();
        part.writeTo(aos);
        assertThat(message.getSubject()).isEqualTo("testSubject");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("john.doe@example.com");
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent()).isInstanceOf(Multipart.class);
        assertThat(aos.toString()).isEqualTo("\r\ntestContent");
        assertThat(part.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    /**
     * Test send email from template.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendEmailFromTemplate() throws Exception {
        User user = new User();
        user.setLangKey("en");
        user.setFirstName("john");
        user.setLastName("doe");
        Credential credential = new Credential().login("john.doe@example.com").user(user);
        ImmutableMap<String, Object> templateArgs = ImmutableMap.<String, Object> builder()
                .put("user", user).build();
        mailService.sendEmailFromTemplate(user.getLangKey(), credential.getLogin(), "testEmail",
                "email.test.title", templateArgs);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        assertThat(message.getSubject()).isEqualTo("test title");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(credential.getLogin());
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent().toString())
                .isEqualTo("<html>test title, http://127.0.0.1:8080, john</html>\n");
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    /**
     * Test send activation email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendActivationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        Credential credential = new Credential().login("john.doe@example.com").user(user);
        mailService.sendActivationEmail(credential);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(credential.getLogin());
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    /**
     * Test creation email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreationEmail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        Credential credential = new Credential().login("john.doe@example.com").user(user)
                .resetKey(RandomUtil.generateResetKey());
        mailService.sendCreationEmail(credential);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(credential.getLogin());
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    /**
     * Test send password reset mail.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendPasswordResetMail() throws Exception {
        User user = new User();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        Credential credential = new Credential().login("john.doe@example.com").user(user)
                .resetKey(RandomUtil.generateResetKey());
        mailService.sendPasswordResetMail(credential);
        verify(javaMailSender).send((MimeMessage) messageCaptor.capture());
        MimeMessage message = (MimeMessage) messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(credential.getLogin());
        assertThat(message.getFrom()[0].toString()).isEqualTo("test@localhost");
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    /**
     * Test send email with exception.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSendEmailWithException() throws Exception {
        doThrow(MailSendException.class).when(javaMailSender).send(any(MimeMessage.class));
        mailService.sendEmail("john.doe@example.com", "testSubject", "testContent", false, false);
    }
    
    /**
     * Test validate email.
     *
     * @throws Exception the exception
     */
    @Test
    public void testValidateEmail() throws Exception {
        assertThat(this.validatorService.isEmail("john.doe@example.com")).isTrue();
        assertThat(this.validatorService.isEmail("john.doe@localhost")).isTrue();
        assertThat(this.validatorService.isEmail("john.doe@127.0.0.1")).isTrue();
        
        assertThat(this.validatorService.isEmail("john.doe@")).isFalse();
        assertThat(this.validatorService.isEmail("@example.com")).isFalse();
        assertThat(this.validatorService.isEmail("john.doe")).isFalse();
        assertThat(this.validatorService.isEmail("john.doe@127.1.0.1.1.1.1.1.1.1")).isFalse(); // must fail
    }

}

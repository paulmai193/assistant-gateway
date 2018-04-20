package logia.assistant.gateway.service;

import java.util.Locale;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;

import com.google.common.collect.ImmutableMap;

import io.github.jhipster.config.JHipsterProperties;
import logia.assistant.gateway.domain.Credential;
import logia.assistant.gateway.domain.User;

/**
 * Service for sending emails.
 * <p>
 * We use the @Async annotation to send emails asynchronously.
 *
 * @author Dai Mai
 */
@Service
public class MailService {

    /** The log. */
    private final Logger               log      = LoggerFactory.getLogger(MailService.class);

    /** The Constant BASE_URL. */
    private static final String        BASE_URL = "baseUrl";

    /** The j hipster properties. */
    private final JHipsterProperties   jHipsterProperties;

    /** The java mail sender. */
    private final JavaMailSender       javaMailSender;

    /** The message source. */
    private final MessageSource        messageSource;

    /** The template engine. */
    private final SpringTemplateEngine templateEngine;

    /**
     * Instantiates a new mail service.
     *
     * @param jHipsterProperties the j hipster properties
     * @param javaMailSender the java mail sender
     * @param messageSource the message source
     * @param templateEngine the template engine
     */
    public MailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender,
            MessageSource messageSource, SpringTemplateEngine templateEngine) {

        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
    }

    /**
     * Send email.
     *
     * @param to the to
     * @param subject the subject
     * @param content the content
     * @param isMultipart the is multipart
     * @param isHtml the is html
     */
    @Async
    public void sendEmail(String to, String subject, String content, boolean isMultipart,
            boolean isHtml) {
        log.debug(
                "Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
                isMultipart, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart,
                    CharEncoding.UTF_8);
            message.setTo(to);
            message.setFrom(jHipsterProperties.getMail().getFrom());
            message.setSubject(subject);
            message.setText(content, isHtml);
            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        }
        catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Email could not be sent to user '{}'", to, e);
            }
            else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }

    /**
     * Send email from template.
     *
     * @param language the language
     * @param email the email
     * @param templateName the template name
     * @param titleKey the title key
     * @param templateArgs the template args
     */
    @Async
    public void sendEmailFromTemplate(String language, String email, String templateName,
            String titleKey, ImmutableMap<String, Object> templateArgs) {
        Locale locale = Locale.forLanguageTag(language);
        Context context = new Context(locale);
        context.setVariables(templateArgs);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(email, subject, content, false, true);

    }

    /**
     * Send activation email.
     *
     * @param user the user
     * @param email the email
     */
    @Async
    public void sendActivationEmail(Credential credential) {
        log.debug("Sending activation email to '{}'", credential.getLogin());
        User user = credential.getUser();
        ImmutableMap<String, Object> templateArgs = ImmutableMap.<String, Object> builder()
                .put("credential", credential).build();
        sendEmailFromTemplate(user.getLangKey(), credential.getLogin(), "activationEmail",
                "email.activation.title", templateArgs);
    }

    /**
     * Send creation email.
     *
     * @param user the user
     * @param email the email
     */
    @Async
    public void sendCreationEmail(Credential credential) {
        User user = credential.getUser();
        log.debug("Sending creation email to '{}'", credential.getLogin());
        ImmutableMap<String, Object> templateArgs = ImmutableMap.<String, Object> builder()
                .put("user", user).put("resetKey", credential.getResetKey()).build();
        sendEmailFromTemplate(user.getLangKey(), credential.getLogin(), "creationEmail",
                "email.activation.title", templateArgs);
    }

    /**
     * Send password reset mail.
     *
     * @param user the user
     * @param email the email
     */
    @Async
    public void sendPasswordResetMail(Credential credential) {
        User user = credential.getUser();
        log.debug("Sending password reset email to '{}'", credential.getLogin());
        ImmutableMap<String, Object> templateArgs = ImmutableMap.<String, Object> builder()
                .put("user", user).put("resetKey", credential.getResetKey()).build();
        sendEmailFromTemplate(user.getLangKey(), credential.getLogin(), "passwordResetEmail",
                "email.reset.title", templateArgs);
    }
}

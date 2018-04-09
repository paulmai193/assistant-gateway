package logia.assistant.gateway.security.social;

import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.SignInAdapter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import io.github.jhipster.config.JHipsterProperties;
import logia.assistant.share.gateway.securiry.jwt.TokenProvider;

/**
 * The Class CustomSignInAdapter.
 *
 * @author Dai Mai
 */
public class CustomSignInAdapter implements SignInAdapter {

    /** The log. */
    @SuppressWarnings("unused")
    private final Logger log = LoggerFactory.getLogger(CustomSignInAdapter.class);

    /** The user details service. */
    private final UserDetailsService userDetailsService;

    /** The j hipster properties. */
    private final JHipsterProperties jHipsterProperties;

    /** The token provider. */
    private final TokenProvider tokenProvider;


    /**
     * Instantiates a new custom sign in adapter.
     *
     * @param userDetailsService the user details service
     * @param jHipsterProperties the j hipster properties
     * @param tokenProvider the token provider
     */
    public CustomSignInAdapter(UserDetailsService userDetailsService, JHipsterProperties jHipsterProperties,
            TokenProvider tokenProvider) {
        this.userDetailsService = userDetailsService;
        this.jHipsterProperties = jHipsterProperties;
        this.tokenProvider = tokenProvider;
    }

    /* (non-Javadoc)
     * @see org.springframework.social.connect.web.SignInAdapter#signIn(java.lang.String, org.springframework.social.connect.Connection, org.springframework.web.context.request.NativeWebRequest)
     */
    @Override
    public String signIn(String userId, Connection<?> connection, NativeWebRequest request){
        try {
            UserDetails user = userDetailsService.loadUserByUsername(userId);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            String jwt = tokenProvider.createToken(authenticationToken, false);
            ServletWebRequest servletWebRequest = (ServletWebRequest) request;
            servletWebRequest.getResponse().addCookie(getSocialAuthenticationCookie(jwt));
        } catch (AuthenticationException ae) {
            log.error("Social authentication error");
            log.trace("Authentication exception trace: {}", ae);
        }
        return jHipsterProperties.getSocial().getRedirectAfterSignIn();
    }

    /**
     * Gets the social authentication cookie.
     *
     * @param token the token
     * @return the social authentication cookie
     */
    private Cookie getSocialAuthenticationCookie(String token) {
        Cookie socialAuthCookie = new Cookie("social-authentication", token);
        socialAuthCookie.setPath("/");
        socialAuthCookie.setMaxAge(10);
        return socialAuthCookie;
    }
}

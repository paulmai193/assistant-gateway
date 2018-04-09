package logia.assistant.gateway.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import logia.assistant.share.gateway.securiry.jwt.AuthoritiesConstants;
import logia.assistant.share.gateway.config.GatewayProperties;
import logia.assistant.share.gateway.securiry.jwt.TokenProvider;

/**
 * The Class TokenProviderTest.
 *
 * @author Dai Mai
 */
public class TokenProviderTest {

    /** The secret key. */
    private final String secretKey = "e5c9ee274ae87bc031adda32e27fa98b9290da83";
    
    /** The one minute. */
    private final long ONE_MINUTE = 60000;
    
    /** The properties. */
    private GatewayProperties properties;
    
    /** The token provider. */
    private TokenProvider tokenProvider;

    /**
     * Setup.
     */
    @Before
    public void setup() {
        properties = Mockito.mock(GatewayProperties.class);
        tokenProvider = new TokenProvider(properties);
        ReflectionTestUtils.setField(tokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", ONE_MINUTE);
    }

    /**
     * Test return false when JW thas invalid signature.
     */
    @Test
    public void testReturnFalseWhenJWThasInvalidSignature() {
        boolean isTokenValid = tokenProvider.validateToken(createTokenWithDifferentSignature());

        assertThat(isTokenValid).isEqualTo(false);
    }

    /**
     * Test return false when JW tis malformed.
     */
    @Test
    public void testReturnFalseWhenJWTisMalformed() {
        Authentication authentication = createAuthentication();
        String token = tokenProvider.createToken(authentication, false);
        String invalidToken = token.substring(1);
        boolean isTokenValid = tokenProvider.validateToken(invalidToken);

        assertThat(isTokenValid).isEqualTo(false);
    }

    /**
     * Test return false when JW tis expired.
     */
    @Test
    public void testReturnFalseWhenJWTisExpired() {
        ReflectionTestUtils.setField(tokenProvider, "tokenValidityInMilliseconds", -ONE_MINUTE);

        Authentication authentication = createAuthentication();
        String token = tokenProvider.createToken(authentication, false);

        boolean isTokenValid = tokenProvider.validateToken(token);

        assertThat(isTokenValid).isEqualTo(false);
    }

    /**
     * Test return false when JW tis unsupported.
     */
    @Test
    public void testReturnFalseWhenJWTisUnsupported() {
        String unsupportedToken = createUnsupportedToken();

        boolean isTokenValid = tokenProvider.validateToken(unsupportedToken);

        assertThat(isTokenValid).isEqualTo(false);
    }

    /**
     * Test return false when JW tis invalid.
     */
    @Test
    public void testReturnFalseWhenJWTisInvalid() {
        boolean isTokenValid = tokenProvider.validateToken("");

        assertThat(isTokenValid).isEqualTo(false);
    }

    /**
     * Creates the authentication.
     *
     * @return the authentication
     */
    private Authentication createAuthentication() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
        return new UsernamePasswordAuthenticationToken("anonymous", "anonymous", authorities);
    }

    /**
     * Creates the unsupported token.
     *
     * @return the string
     */
    private String createUnsupportedToken() {
        return Jwts.builder()
            .setPayload("payload")
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }

    /**
     * Creates the token with different signature.
     *
     * @return the string
     */
    private String createTokenWithDifferentSignature() {
        return Jwts.builder()
            .setSubject("anonymous")
            .signWith(SignatureAlgorithm.HS512, "e5c9ee274ae87bc031adda32e27fa98b9290da90")
            .setExpiration(new Date(new Date().getTime() + ONE_MINUTE))
            .compact();
    }
}

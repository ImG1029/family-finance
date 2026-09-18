package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.domain.user.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {
    private final String jwtSecret = "test-secret-key-with-more-than-32-chars";
    private final long jwtExpirationMS = 300000L;

    TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(jwtSecret, 300000L);
    }

    @Test
    void generateToken_ReturnsAccessToken() {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@email.com");
        mockUser.setName("Test User");

        String token = tokenService.generateToken(mockUser);
        Instant expectedExpiration = Instant.now().plus(jwtExpirationMS, ChronoUnit.MILLIS);

        Claims payload = Jwts.parser().verifyWith(
                Keys.hmacShaKeyFor(jwtSecret.getBytes())
        ).build().parseSignedClaims(token).getPayload();

        assertEquals("family-finance-api", payload.getIssuer());
        assertEquals(mockUser.getId().toString(), payload.getSubject());
        assertEquals(mockUser.getEmail(), payload.get("email"));
        assertEquals(mockUser.getName(), payload.get("name"));

        Assertions.assertThat(payload.getExpiration().toInstant())
                .isCloseTo(expectedExpiration, Assertions.within(1, ChronoUnit.SECONDS));
    }

    @Test
    void validateToken_ChecksAndReturnsAccessToken() {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@email.com");
        mockUser.setName("Test User");

        String validToken = tokenService.generateToken(mockUser);

        String subject = tokenService.validateTokenAndGetSubject(validToken);

        assertNotNull(subject);
        assertEquals(mockUser.getId().toString(), subject);
    }

    @Test
    void validateToken_WhenTokenExpired_ReturnsNull() {
        String expiredToken = Jwts.builder()
                .issuer("family-finance-api")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now().minus(10, ChronoUnit.MINUTES)))
                .expiration(Date.from(Instant.now().minus(5, ChronoUnit.MINUTES)))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .compact();

        String subject = tokenService.validateTokenAndGetSubject(expiredToken);

        assertNull(subject);
    }

    @Test
    void ValidateToken_WhenSignatureInvalid_ReturnsNull() {
        String fakeJwtSecret = "fake-test-secret-key-with-more-than-32-chars";

        String forgedToken = Jwts.builder()
                .issuer("family-finance-api")
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plus(jwtExpirationMS, ChronoUnit.MINUTES)))
                .signWith(Keys.hmacShaKeyFor(fakeJwtSecret.getBytes()))
                .compact();

        String subject = tokenService.validateTokenAndGetSubject(forgedToken);

        assertNull(subject);
    }

    @Test
    void ValidateToken_WhenTokenMalformedOrEdited_ReturnsNull() {
        String fakeJwt = "this-is.a-fake.jwt-token";

        String subject = tokenService.validateTokenAndGetSubject(fakeJwt);

        assertNull(subject);
    }
}

package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.domain.user.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
public class TokenService {
    private final String jwtSecret;
    private final long jwtExpirationMS;

    public TokenService(
            @Value("${app.jwt.secret}")String jwtSecret,
            @Value("${app.jwt.expiration-ms}") long jwtExpirationMS) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMS = jwtExpirationMS;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user) {
        Instant now = Instant.now();

        return Jwts.builder()
                .issuer("family-finance-api")
                .subject(user.getId().toString())
                .claims(Map.of(
                        "name", user.getName(),
                        "email", user.getEmail()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtExpirationMS, ChronoUnit.MILLIS)))
                .signWith(getSigningKey())
                .compact();
    }

    public String validateTokenAndGetSubject(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getJwtExpirationMS() {
        return jwtExpirationMS;
    }
}

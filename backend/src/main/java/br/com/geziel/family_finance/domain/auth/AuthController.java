package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.domain.auth.dto.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/authentication")
public class AuthController {
    private final AuthService authService;
    private final long refreshExpirationDays;

    public AuthController(
            AuthService authService,
            @Value("${app.refresh-token.expiration-days}") long refreshExpirationDays) {
        this.authService = authService;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/authentication")
                .maxAge(refreshExpirationDays * 60 * 60 * 24)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        AuthResponseDTO authResponse = authService.login(dto);

        ResponseCookie cookie = createRefreshTokenCookie(authResponse.refreshToken());

        AccessTokenResponseDTO body = new AccessTokenResponseDTO(
                authResponse.accessToken(),
                authResponse.tokenType(),
                authResponse.expiresInSeconds()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {
        AuthResponseDTO authResponse = authService.register(dto);

        ResponseCookie cookie = createRefreshTokenCookie(authResponse.refreshToken());

        AccessTokenResponseDTO body = new AccessTokenResponseDTO(
                authResponse.accessToken(),
                authResponse.tokenType(),
                authResponse.expiresInSeconds()
        );
        return ResponseEntity.status(201)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDTO> refresh(@CookieValue(name = "refreshToken") String refreshToken) {
        AuthResponseDTO authResponse = authService.refreshToken(UUID.fromString(refreshToken));

        ResponseCookie cookie = createRefreshTokenCookie(authResponse.refreshToken());

        AccessTokenResponseDTO body = new AccessTokenResponseDTO(
                authResponse.accessToken(),
                authResponse.tokenType(),
                authResponse.expiresInSeconds()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken") String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(UUID.fromString(refreshToken));
        }

        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/api/authentication")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}

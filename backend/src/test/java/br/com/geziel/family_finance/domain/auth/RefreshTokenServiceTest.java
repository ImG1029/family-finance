package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {
    @Mock
    RefreshTokenRepository refreshTokenRepository;

    RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, 7L);
    }


// ------------------------------------------------------------------------------------------------------------------ //
//      CREATE REFRESH TOKEN
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void createRefreshToken_SavesAndReturnsRefreshToken() {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(returnsFirstArg());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(mockUser);

        assertNotNull(refreshToken);
        assertNotNull(refreshToken.getToken());
        assertEquals(mockUser, refreshToken.getUser());
        assertFalse(refreshToken.isRevoked());

        Instant expectedExpiration = Instant.now().plus(7L, ChronoUnit.DAYS);
        Assertions.assertThat(refreshToken.getExpiresAt())
                        .isCloseTo(expectedExpiration, Assertions.within(1, ChronoUnit.SECONDS));

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(tokenCaptor.capture());

        RefreshToken savedToken = tokenCaptor.getValue();
        assertEquals(refreshToken.getToken(), savedToken.getToken());
        assertEquals(mockUser, savedToken.getUser());
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      VERIFY VALIDITY
// ------------------------------------------------------------------------------------------------------------------ //


    @Test
    void verifyValidity_ChecksAndReturnsRefreshToken() {
        UUID token = UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plus(7L, ChronoUnit.DAYS));

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.of(refreshToken));

        RefreshToken validatedToken = refreshTokenService.verifyValidity(token);

        assertNotNull(validatedToken);
        assertNotNull(validatedToken.getToken());
        assertFalse(validatedToken.isRevoked());

        verify(refreshTokenRepository, times(1)).findById(token);

        assertEquals(token, refreshToken.getToken());
    }

    @Test
    void verifyValidity_WhenRefreshTokenInvalid_ThrowsException() {
        UUID token = UUID.randomUUID();

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> refreshTokenService.verifyValidity(token)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenRepository, times(1)).findById(token);
    }

    @Test
    void verifyValidity_WhenRefreshTokenRevoked_ThrowsException() {
        UUID token = UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setRevoked(true);

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.of(refreshToken));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> refreshTokenService.verifyValidity(token)
        );

        assertEquals("Refresh token already revoked", exception.getMessage());

        verify(refreshTokenRepository, times(1)).findById(token);
    }

    @Test
    void verifyValidity_WhenRefreshTokenExpired_ThrowsException() {
        UUID token = UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(Instant.now().minus(7L, ChronoUnit.DAYS));

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.of(refreshToken));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> refreshTokenService.verifyValidity(token)
        );

        assertEquals("Refresh token expired", exception.getMessage());

        verify(refreshTokenRepository, times(1)).findById(token);
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      REVOKE
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void revokeToken_ChangesStatusToRevokedAndSaves() {
        UUID token = UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setRevoked(false);

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.of(refreshToken));

        refreshTokenService.revokeToken(refreshToken.getToken());

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(1)).save(tokenCaptor.capture());

        RefreshToken revokedToken = tokenCaptor.getValue();
        assertTrue(revokedToken.isRevoked());
    }

    @Test
    void revokeToken_WhenInvalidRefreshToken_ThrowsException() {
        UUID token = UUID.randomUUID();

        when(refreshTokenRepository.findById(token)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> refreshTokenService.revokeToken(token)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenRepository, times(1)).findById(token);
    }
}

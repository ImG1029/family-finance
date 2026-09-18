package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.domain.auth.dto.AuthResponseDTO;
import br.com.geziel.family_finance.domain.auth.dto.LoginRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RefreshRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RegisterRequestDTO;
import br.com.geziel.family_finance.domain.user.User;
import br.com.geziel.family_finance.domain.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

// ------------------------------------------------------------------------------------------------------------------ //
//      LOGIN
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void login_WhenValidCredentials_ReturnsTokens() {
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "Senha123");

        User mockUser = new User();
        mockUser.setEmail("test@email.com");

        Authentication mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        when(tokenService.generateToken(mockUser)).thenReturn("mocked.jwt.token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(UUID.randomUUID());
        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn(mockRefreshToken);

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.accessToken());
        assertEquals(mockRefreshToken.getToken().toString(), response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(request.email(), response.user().email());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, times(1)).generateToken(mockUser);
        verify(refreshTokenService, times(1)).createRefreshToken(mockUser);
    }

    @Test
    void login_WhenInvalidPassword_ThrowsException() {
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "Senha errada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid Credentials"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class, () -> authService.login(request)
        );

        assertEquals("Invalid Credentials", exception.getMessage());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenService);
        verifyNoInteractions(refreshTokenService);
    }

    @Test
    void login_WhenInvalidEmail_ThrowsException() {
        LoginRequestDTO request = new LoginRequestDTO("wrong@email.com", "Senha123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid Credentials"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class, () -> authService.login(request)
        );

        assertEquals("Invalid Credentials", exception.getMessage());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenService);
        verifyNoInteractions(refreshTokenService);
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      REGISTER
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void register_WhenValidNewUser_ReturnsTokens() {
        RegisterRequestDTO request = new RegisterRequestDTO("new-test@email.com", "New Test User", "Senha123");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed_password");

        when(userRepository.save(any(User.class))).thenAnswer(returnsFirstArg());
        when(tokenService.generateToken(any(User.class))).thenReturn("mocked.jwt.token");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(UUID.randomUUID());
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(mockRefreshToken);

        AuthResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.accessToken());
        assertEquals(mockRefreshToken.getToken().toString(), response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(request.email(), response.user().email());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("New Test User", capturedUser.getName());
        assertEquals("new-test@email.com", capturedUser.getEmail());
        assertEquals("hashed_password", capturedUser.getPassword());
        assertNotNull(capturedUser.getCreatedAt());

        verify(tokenService, times(1)).generateToken(any(User.class));
        verify(refreshTokenService, times(1)).createRefreshToken(any(User.class));
    }

    @Test
    void register_WhenEmailAlreadyExists_ThrowsException() {
        RegisterRequestDTO request = new RegisterRequestDTO("new-test@email.com", "New Test User", "Senha123");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> authService.register(request)
        );

        assertEquals("Email already used", exception.getMessage());

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(tokenService);
        verifyNoInteractions(refreshTokenService);
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      REFRESH
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void refresh_WhenTokenValid_ReturnsTokens() {
        RefreshRequestDTO request = new RefreshRequestDTO(UUID.randomUUID());

        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        RefreshToken oldMockRefreshToken = new RefreshToken();
        oldMockRefreshToken.setToken(request.refreshToken());
        oldMockRefreshToken.setUser(mockUser);
        oldMockRefreshToken.setRevoked(false);

        when(refreshTokenService.verifyValidity(request.refreshToken())).thenReturn(oldMockRefreshToken);

        when(tokenService.generateToken(mockUser)).thenReturn("mocked.jwt.token");

        RefreshToken newMockRefreshToken = new RefreshToken();
        newMockRefreshToken.setToken(UUID.randomUUID());

        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn(newMockRefreshToken);

        AuthResponseDTO response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.accessToken());
        assertEquals(newMockRefreshToken.getToken().toString(), response.refreshToken());
        assertEquals("Bearer", response.tokenType());

        assertTrue(oldMockRefreshToken.isRevoked());

        verify(refreshTokenService, times(1)).verifyValidity(request.refreshToken());
        verify(tokenService, times(1)).generateToken(mockUser);
        verify(refreshTokenService, times(1)).createRefreshToken(mockUser);
    }

    @Test
    void refresh_WhenTokenInvalid_ThrowsException() {
        RefreshRequestDTO request = new RefreshRequestDTO(UUID.randomUUID());

        when(refreshTokenService.verifyValidity(request.refreshToken()))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> authService.refreshToken(request)
        );

        assertEquals("Invalid refresh token", exception.getMessage());

        verify(refreshTokenService, times(1)).verifyValidity(request.refreshToken());

        verifyNoInteractions(tokenService);
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void refresh_WhenTokenRevoked_throwsException() {
        RefreshRequestDTO request = new RefreshRequestDTO(UUID.randomUUID());

        when(refreshTokenService.verifyValidity(request.refreshToken()))
                .thenThrow(new IllegalStateException("Refresh token already revoked"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> authService.refreshToken(request)
        );

        assertEquals("Refresh token already revoked", exception.getMessage());

        verify(refreshTokenService, times(1)).verifyValidity(request.refreshToken());

        verifyNoInteractions(tokenService);
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void refresh_WhenTokenExpired_throwsException() {
        RefreshRequestDTO request = new RefreshRequestDTO(UUID.randomUUID());

        when(refreshTokenService.verifyValidity(request.refreshToken()))
                .thenThrow(new IllegalStateException("Refresh token expired"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> authService.refreshToken(request)
        );

        assertEquals("Refresh token expired", exception.getMessage());

        verify(refreshTokenService, times(1)).verifyValidity(request.refreshToken());

        verifyNoInteractions(tokenService);
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      LOGOUT
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void logout_CallsRevokeToken() {
        RefreshRequestDTO request = new RefreshRequestDTO(UUID.randomUUID());

        authService.logout(request);

        verify(refreshTokenService, times(1)).revokeToken(request.refreshToken());
    }

}

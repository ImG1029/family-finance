package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.domain.auth.dto.AuthResponseDTO;
import br.com.geziel.family_finance.domain.auth.dto.LoginRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RefreshRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RegisterRequestDTO;
import br.com.geziel.family_finance.domain.user.User;
import br.com.geziel.family_finance.domain.user.UserRepository;
import br.com.geziel.family_finance.domain.user.dto.UserSummaryDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, TokenService tokenService, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        User user = (User) authentication.getPrincipal();
        UserSummaryDTO summary = new UserSummaryDTO(user.getId(), user.getName(), user.getEmail());

        String accessToken = tokenService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken().toString(),
                "Bearer",
                summary,
                tokenService.getJwtExpirationMS()
        );
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already used");
        }

        User newUser = new User();
        newUser.setName(dto.name());
        newUser.setEmail(dto.email());
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.setCreatedAt(LocalDateTime.now());

        User user = userRepository.save(newUser);
        UserSummaryDTO summary = new UserSummaryDTO(user.getId(), user.getName(), user.getEmail());

        String accessToken = tokenService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken().toString(),
                "Bearer",
                summary,
                tokenService.getJwtExpirationMS()
        );
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshRequestDTO dto) {
        RefreshToken validToken = refreshTokenService.verifyValidity(dto.refreshToken());
        User user = validToken.getUser();

        validToken.setRevoked(true);

        String accessToken = tokenService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        UserSummaryDTO summary = new UserSummaryDTO(user.getId(), user.getName(), user.getEmail());

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken().toString(),
                "Bearer",
                summary,
                tokenService.getJwtExpirationMS()
        );
    }

    public void logout(RefreshRequestDTO dto) {
        refreshTokenService.revokeToken(dto.refreshToken());
    }
}

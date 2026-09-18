package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.domain.auth.dto.AuthResponseDTO;
import br.com.geziel.family_finance.domain.auth.dto.LoginRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RefreshRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RegisterRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/authentication")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        AuthResponseDTO token = authService.login(dto);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid RegisterRequestDTO dto) {
        AuthResponseDTO token = authService.register(dto);
        return ResponseEntity.status(201).body(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@RequestBody @Valid RefreshRequestDTO dto) {
        return ResponseEntity.ok(authService.refreshToken(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequestDTO dto) {
        authService.logout(dto);
        return ResponseEntity.noContent().build();
    }
}

package br.com.geziel.family_finance.domain.user;

import br.com.geziel.family_finance.domain.user.dto.UserSummaryDTO;
import br.com.geziel.family_finance.domain.user.dto.UserUpdateDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDTO> getUser(@AuthenticationPrincipal User loggedUser) {
        UserSummaryDTO body = userService.getUser(loggedUser);

        return ResponseEntity.ok()
                .body(body);
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> updateUser(@AuthenticationPrincipal User loggedUser, @RequestBody @Valid UserUpdateDTO dto) {
        userService.updateUser(loggedUser, dto);

        return ResponseEntity.noContent()
                .build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateUser(@AuthenticationPrincipal User loggedUser) {
        userService.deactivateUser(loggedUser);

        return ResponseEntity.noContent()
                .build();
    }
}

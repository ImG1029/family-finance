package br.com.geziel.family_finance.domain.user;

import br.com.geziel.family_finance.domain.auth.TokenService;
import br.com.geziel.family_finance.domain.user.dto.UserSummaryDTO;
import br.com.geziel.family_finance.domain.user.dto.UserUpdateDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserSummaryDTO getUser(User loggedUser) {
        return new UserSummaryDTO(loggedUser.getId(), loggedUser.getName(), loggedUser.getEmail());
    }

    public void updateUser(User loggedUser, UserUpdateDTO dto) {
        User realUser = userRepository.findById(loggedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        realUser.setName(dto.name());
        realUser.setUpdatedAt(Instant.now());

        userRepository.save(realUser);
    }

    public void deactivateUser(User loggedUser) {
        User user = userRepository.findById(loggedUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setActive(false);
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}

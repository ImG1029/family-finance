package br.com.geziel.family_finance.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        String email = "test@email.com";
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(email);
        mockUser.setPassword("Senha123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("Senha123", userDetails.getPassword());

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ThrowsException() {
        String email = "non-existent@email.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(email)
        );

        System.out.println(exception.getMessage());

        assertEquals("User not found: " + email, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
    }
}
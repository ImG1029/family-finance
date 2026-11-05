package br.com.geziel.family_finance.domain.user;

import br.com.geziel.family_finance.domain.user.dto.UserSummaryDTO;
import br.com.geziel.family_finance.domain.user.dto.UserUpdateDTO;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUser_WhenUserExists_ReturnsUserSummary() {
        UUID userId = UUID.randomUUID();
        String name = "Test";
        String email = "test@email.com";

        User loggedUser = new User();
        loggedUser.setId(userId);
        loggedUser.setName(name);
        loggedUser.setEmail(email);

        UserSummaryDTO summary = userService.getUser(loggedUser);

        assertEquals(userId, summary.id());
        assertEquals(name, summary.name());
        assertEquals(email, summary.email());

        verifyNoInteractions(userRepository);
    }

    @Test
    void updateUser_NoReturn() {
        UUID userId = UUID.randomUUID();
        String name = "Test";
        String email = "test@email.com";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(name);
        mockUser.setEmail(email);

        User loggedUser = new User();
        loggedUser.setId(userId);

        when(userRepository.findById(loggedUser.getId())).thenReturn(Optional.of(mockUser));

        String newName = "New name";
        UserUpdateDTO dto = new UserUpdateDTO(newName);

        userService.updateUser(loggedUser, dto);

        assertEquals(newName, mockUser.getName());
        assertNotEquals(name, mockUser.getName());
        assertEquals(email, mockUser.getEmail());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ThrowsException() {
        UUID userId = UUID.randomUUID();
        String name = "Test";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(name);

        User unexistingUser = new User();
        unexistingUser.setId(userId);

        when(userRepository.findById(unexistingUser.getId())).thenReturn(Optional.empty());

        String newName = "New name";
        UserUpdateDTO dto = new UserUpdateDTO(newName);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> userService.updateUser(unexistingUser, dto)
        );

        assertEquals("User not found", exception.getMessage());

        assertNotEquals(newName, mockUser.getName());
        assertEquals(name, mockUser.getName());

        verify(userRepository, times(1)).findById(unexistingUser.getId());
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void deactivateUser_NoReturn() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setActive(true);

        User loggedUser = new User();
        loggedUser.setId(userId);

        when(userRepository.findById(loggedUser.getId())).thenReturn(Optional.of(mockUser));

        userService.deactivateUser(loggedUser);

        assertFalse(mockUser.isEnabled());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(mockUser);
    }

    @Test
    void deactivateUser_WhenUserDoesNotExist_ThrowsException() {
        UUID userId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setActive(true);

        User unexistingUser = new User();
        unexistingUser.setId(userId);

        when(userRepository.findById(unexistingUser.getId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> userService.deactivateUser(unexistingUser)
        );

        assertEquals("User not found", exception.getMessage());

        assertTrue(mockUser.isEnabled());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(0)).save(mockUser);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ReturnsUserDetails() {
        String email = "test@email.com";
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(email);
        mockUser.setPassword("Senha123");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = userService.loadUserByUsername(email);

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
                UsernameNotFoundException.class, () -> userService.loadUserByUsername(email)
        );

        assertEquals("User not found: " + email, exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
    }
}
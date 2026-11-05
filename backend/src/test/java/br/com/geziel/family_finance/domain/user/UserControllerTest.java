package br.com.geziel.family_finance.domain.user;

import br.com.geziel.family_finance.BaseIntegrationTest;
import br.com.geziel.family_finance.domain.auth.TokenService;
import br.com.geziel.family_finance.domain.user.dto.UserUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private TokenService tokenService;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        createTestUser();

        testUser = userRepository.findByEmail("test@email.com")
                .orElseThrow(() -> new RuntimeException("Test User not found"));

        accessToken = tokenService.generateToken(testUser);
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      GET USER
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void getUser_WhenAuthenticated_ReturnsUserSummary() throws Exception {
        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.name").value(testUser.getName()));
    }

    @Test
    void getUser_WhenNotAuthenticated_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isForbidden());
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      UPDATE USER
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void updateUser_WhenAuthenticated_ReturnsNoContent() throws Exception {
        String newName = "New name";
        UserUpdateDTO dto = new UserUpdateDTO(newName);

        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(newName, updatedUser.getName());
    }

    @Test
    void updateUser_WhenNotAuthenticated_ReturnsForbidden() throws Exception {
        String newName = "New name";
        UserUpdateDTO dto = new UserUpdateDTO(newName);

        mockMvc.perform(patch("/api/user/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_WhenNameIsBlank_ReturnsBadRequest() throws Exception {
        UserUpdateDTO dto = new UserUpdateDTO("");

        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void updateUser_WhenPayloadIsMalformed_ReturnsBadRequest() throws Exception {
        String malformedJson = "{ \"name\": ";

        mockMvc.perform(patch("/api/user/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      DEACTIVATE USER
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void deactivateUser_WhenAuthenticated_ReturnsNoContentAndDeactivates() throws Exception {
        mockMvc.perform(delete("/api/user/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        User deactivatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertFalse(deactivatedUser.isEnabled());
    }

    @Test
    void deactivateUser_WhenNotAuthenticated_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/user/me"))
                .andExpect(status().isForbidden());
    }
}
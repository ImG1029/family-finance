package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.BaseIntegrationTest;
import br.com.geziel.family_finance.domain.auth.dto.LoginRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RefreshRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseIntegrationTest {


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        createTestUser();
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      LOGIN
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void login_WithValidCredentials_ReturnsTokens() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "Password123");

        mockMvc.perform(post("/api/authentication/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void login_WithWrongPassword_ReturnsUnauthorized() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "Wrong password");

        mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithWrongEmail_ReturnsUnauthorized() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("invalid@email.com", "Password123");

        mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      REGISTER
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void register_WithValidData_ReturnsOkAndTokens() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "new-test@email.com", "New Test User", "Password123"
        );

        mockMvc.perform(post("/api/authentication/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").isNotEmpty());

        assertTrue(userRepository.existsByEmail(request.email()));
    }

    @Test
    void register_WithExistingEmail_ReturnsBadRequest() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "test@email.com", "New Test User", "Password123"
        );

        mockMvc.perform(post("/api/authentication/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertTrue(userRepository.existsByEmail(request.email()));
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      REFRESH
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void refresh_WithValidToken_ReturnsOk() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@email.com", "Password123");

        String loginResponse = mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String validRefreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asString();

        RefreshRequestDTO refreshRequest = new RefreshRequestDTO(UUID.fromString(validRefreshToken));

        mockMvc.perform(post("/api/authentication/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").value(org.hamcrest.Matchers.not(validRefreshToken)));

    }

    @Test
    void refresh_WithInvalid_ReturnsBadRequest() throws Exception {
        RefreshRequestDTO refreshRequest = new RefreshRequestDTO(UUID.randomUUID());

        mockMvc.perform(post("/api/authentication/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      LOGOUT
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void logout_ReturnsNoContent() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@email.com", "Password123");

        String loginResponse = mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String validRefreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asString();

        RefreshRequestDTO request = new RefreshRequestDTO(UUID.fromString(validRefreshToken));

        mockMvc.perform(post("/api/authentication/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

}
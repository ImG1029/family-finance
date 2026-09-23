package br.com.geziel.family_finance.domain.auth;

import br.com.geziel.family_finance.BaseIntegrationTest;
import br.com.geziel.family_finance.domain.auth.dto.LoginRequestDTO;
import br.com.geziel.family_finance.domain.auth.dto.RegisterRequestDTO;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void login_WithValidCredentials_ReturnsTokensAndCookie() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "Password123");

        mockMvc.perform(post("/api/authentication/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true));
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

        Cookie refreshCookie = mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("refreshToken");

        assertNotNull(refreshCookie);

        mockMvc.perform(post("/api/authentication/refresh")
                        .cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(cookie().value("refreshToken", org.hamcrest.Matchers.not(refreshCookie.getValue())));

    }

    @Test
    void refresh_WithMissingCookie_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/authentication/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

// ------------------------------------------------------------------------------------------------------------------ //
//      LOGOUT
// ------------------------------------------------------------------------------------------------------------------ //

    @Test
    void logout_ReturnsNoContent() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO("test@email.com", "Password123");

        Cookie loginResponse = mockMvc.perform(post("/api/authentication/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getCookie("refreshToken");

        Cookie cookie = new Cookie("refreshToken", loginResponse.getValue());

        mockMvc.perform(post("/api/authentication/logout")
                        .cookie(cookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().maxAge("refreshToken", 0));
    }

}
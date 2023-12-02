package org.coder229.authserver.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coder229.authserver.config.ServiceConfig;
import org.coder229.authserver.model.*;
import org.coder229.authserver.persistence.Token;
import org.coder229.authserver.persistence.TokenRepository;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.coder229.authserver.utilities.TestUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TestUtility testUtility;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    public ServiceConfig serviceConfig;

    @Nested
    class LoginTests {
        private String username;
        private String password = "password";
        private User user;

        @BeforeEach
        void setup() {
            username = "username" + new Random().nextInt(1000);
            user = testUtility.createUser(username, "password", List.of(UserRole.USER));
        }

        @Test
        void shouldLoginAndReturnValidToken() throws Exception {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(serviceConfig.getJwtSecret()))
                    .build();
            LoginRequest loginRequest = new LoginRequest(username, password);

            MvcResult response = mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String body = response.getResponse().getContentAsString();
            LoginResponse loginResponse = objectMapper.readValue(body, LoginResponse.class);

            assertThat(loginResponse.accessToken()).isNotNull();
            DecodedJWT decoded = verifier.verify(loginResponse.accessToken());
            assertThat(decoded.getSubject()).isEqualTo(username);
        }

        @Test
        void shouldFailForNonUser() throws Exception {
            LoginRequest loginRequest = new LoginRequest("unknown", password);
            String body = objectMapper.writeValueAsString(loginRequest);

            mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailForWrongPassword() throws Exception {
            LoginRequest loginRequest = new LoginRequest(username, "wrong");
            String body = objectMapper.writeValueAsString(loginRequest);

            mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldFailForBadRequest() throws Exception {
            String body = "abc";

            mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class RefreshTests {
        @Test
        void refresh() throws Exception {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(serviceConfig.getJwtSecret()))
                    .build();
            String username = "username" + new Random().nextInt(1000);
            String password = "password";
            RegisterRequest registerRequest = new RegisterRequest(username, password);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk());

            LoginRequest loginRequest = new LoginRequest(username, password);
            MvcResult response = mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String body = response.getResponse().getContentAsString();
            LoginResponse loginResponse = objectMapper.readValue(body, LoginResponse.class);

            RefreshRequest refreshRequest = new RefreshRequest(loginResponse.id(), loginResponse.refreshToken());
            response = mockMvc.perform(post("/api/v1/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            body = response.getResponse().getContentAsString();
            RefreshResponse refreshResponse = objectMapper.readValue(body, RefreshResponse.class);

            assertThat(refreshResponse.accessToken()).isNotNull();
            DecodedJWT decoded = verifier.verify(refreshResponse.accessToken());
            assertThat(decoded.getSubject()).isEqualTo(username);
        }

        @Test
        void refreshWithBadToken() throws Exception {
            String username = "username" + new Random().nextInt(1000);
            User user = testUtility.createUser(username, "password", List.of(UserRole.USER));
            Token token = testUtility.createToken(user, TokenType.REFRESH, Instant.now().minusSeconds(1));

            RefreshRequest refreshRequest = new RefreshRequest(user.getId(), token.getValue());
            mockMvc.perform(post("/api/v1/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}
package org.coder229.authserver.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coder229.authserver.model.LoginRequest;
import org.coder229.authserver.model.LoginResponse;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    private static final Logger LOG = Logger.getLogger(AuthControllerTest.class.getSimpleName());

    private String username = UUID.randomUUID().toString();
    private String password = "password";
    private User user;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Value("${authservice.secret}")
    public String SECRET;
    @Value("${authservice.salt}")
    public String SALT;

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw("password", SALT));
        user.setEnabled(true);
        user.setVerified(true);
        this.user = userRepository.save(user);
    }

    @Test
    public void shouldLoginAndReturnValidToken() throws Exception {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET))
                .build();
        LoginRequest loginRequest = new LoginRequest(username, password);
        String body = objectMapper.writeValueAsString(loginRequest);

        MvcResult response = this.mockMvc.perform(
                        post("/api/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(response.getResponse().getContentAsString(), LoginResponse.class);

        assertThat(loginResponse).isNotNull();
        LOG.info("AccessToken: " + loginResponse.accessToken());
        assertThat(loginResponse.accessToken()).isNotNull();
        DecodedJWT decoded = verifier.verify(loginResponse.accessToken());
        assertThat(decoded.getSubject()).isEqualTo(username);
    }

    @Test
    public void shouldFailForNonUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest("unknown", password);
        String body = objectMapper.writeValueAsString(loginRequest);

        this.mockMvc.perform(
                        post("/api/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(body))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldFailForWrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, "wrong");
        String body = objectMapper.writeValueAsString(loginRequest);

        this.mockMvc.perform(
                        post("/api/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(body))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldFailForBadRequest() throws Exception {
        String body = "abc";

        this.mockMvc.perform(
                        post("/api/v1/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(body))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
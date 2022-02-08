package org.coder229.authserver.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coder229.authserver.entities.User;
import org.coder229.authserver.entities.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    private String username = UUID.randomUUID().toString();
    private String password = "password";
    private User user;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebTestClient webTestClient;

    @Value("${authservice.secret}")
    public String SECRET;

    @Value("${authservice.salt}")
    public String SALT;

    @BeforeEach
    public void setup() {
        user = userRepository.save(new User(username, BCrypt.hashpw("password", SALT), true));
    }

    @AfterEach
    public void tearDown() {
        userRepository.delete(user);
    }

    @Test
    public void shouldLoginAndReturnValidToken() throws Exception {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET))
                .build();
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername(username);
        loginForm.setPassword(password);
        String body = objectMapper.writeValueAsString(loginForm);

        FluxExchangeResult<String> response = webTestClient
                .post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), String.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(String.class);

        String jwt = response.getResponseBody().blockFirst();
        assertThat(jwt).isNotNull();
        DecodedJWT decoded = verifier.verify(jwt);
        assertThat(decoded.getSubject()).isEqualTo(username);
    }

    @Test
    public void shouldFailForNonUser() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("unknown");
        loginForm.setPassword(password);
        String body = objectMapper.writeValueAsString(loginForm);

        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), String.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void shouldFailForWrongPassword() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername(username);
        loginForm.setPassword("wrong");
        String body = objectMapper.writeValueAsString(loginForm);

        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), String.class)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    public void shouldFailForBadRequest() {
        String body = "abc";

        webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(body), String.class)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
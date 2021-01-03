package org.coder229.authserver.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coder229.authserver.entities.User;
import org.coder229.authserver.entities.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.springframework.boot.test.context.SpringBootTest.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper objectMapper = new ObjectMapper();

    private String username = UUID.randomUUID().toString();
    private String password = "password";
    private User user;

    @Autowired
    private UserRepository userRepository;

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
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername(username);
        loginForm.setPassword(password);
        String body = objectMapper.writeValueAsString(loginForm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        String url = "http://localhost:" + port + "/auth/login";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        String jwt = response.getBody();
        System.out.println(jwt);

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET))
                .build();

        DecodedJWT decoded = verifier.verify(jwt);
        assertThat(decoded.getSubject(), equalTo(username));
    }

    @Test
    public void shouldFailForNonUser() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername("unknown");
        loginForm.setPassword(password);
        String body = objectMapper.writeValueAsString(loginForm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        String url = "http://localhost:" + port + "/auth/login";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void shouldFailForWrongPassword() throws Exception {
        LoginForm loginForm = new LoginForm();
        loginForm.setUsername(username);
        loginForm.setPassword("wrong");
        String body = objectMapper.writeValueAsString(loginForm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        String url = "http://localhost:" + port + "/auth/login";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }
}